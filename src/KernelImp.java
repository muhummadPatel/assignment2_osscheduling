import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * muhummad        15/03/19.
 */
public class KernelImp implements Kernel {

    int timeslice;

    //<device id, queue of pids waiting for device with device id>
    private ArrayList<IODeviceImp> devices = new ArrayList<IODeviceImp>();
    private Queue<ProcessControlBlock> ready = new LinkedList<ProcessControlBlock>();


    public KernelImp(int timeslice){
        this.timeslice = timeslice;
    }

    @Override
    public void interrupt(int interruptType, Object... varargs) {
        interruptTrace(interruptType, varargs);

        switch(interruptType){
            case InterruptHandler.TIME_OUT:
//                if (ready.isEmpty()) {
//                    // Given current process another slice.
//                    ProcessControlBlock current = Simulator.cpu.getCurrentProcess();
//                    Simulator.cpu.contextSwitch(current);
//                    Simulator.timer.scheduleInterrupt(timeslice, current);
//                }else {
//                    ProcessControlBlock switchedIn = ready.poll();
//                    ProcessControlBlock switchedOut = Simulator.cpu.contextSwitch(switchedIn);
//                    ready.add(switchedOut);
//                    Simulator.timer.scheduleInterrupt(timeslice, switchedIn);
//                }
                interrupt_time_out();
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;
            case InterruptHandler.WAKE_UP:
                interrupt_wake_up((Integer) (varargs[0]), (Integer) (varargs[1]));
                break;
        }

        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);

        System.out.printf("Time: %010d Kernel exit\n", Simulator.timer.getSystemTime());
    }

    private void interrupt_time_out(){
        ProcessControlBlock current = Simulator.cpu.getCurrentProcess();
        if(current != null){
            ready.add(current);

            ProcessControlBlock switchedIn = ready.poll();

            Simulator.cpu.contextSwitch(switchedIn);
            if(switchedIn != null) {
                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, switchedIn);
            }
        }
    }

    private void interrupt_wake_up(int deviceId, int pid){
        LinkedList<ProcessControlBlock> relQueue = new LinkedList<ProcessControlBlock>();
        for(IODeviceImp iod: devices){

            if(iod.getID() == deviceId){
                relQueue = iod.deviceQueue;
            }
        }

        int pos = -1;
        for(int i = 0; i < relQueue.size(); i++){
            ProcessControlBlock pcb = relQueue.get(i);
            if(pcb.getPID() == pid){
                pos = i;
                break;
            }
        }

        if(pos != -1){
            ProcessControlBlock relProcess = relQueue.remove(pos);
            relProcess.nextInstruction();
            ready.add(relProcess);

            if(Simulator.cpu.isIdle()){
                ProcessControlBlock switchedIn = ready.poll();
                Simulator.cpu.contextSwitch(switchedIn);
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);

                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, switchedIn);
            }
        }
    }


    @Override
    public int syscall(int number, Object... varargs) {
        sysCallTrace(number, varargs);

        switch(number){
            case MAKE_DEVICE:
                sys_make_device(Integer.parseInt((String) varargs[0]), (String) varargs[1]);
                break;
            case EXECVE:
                //TODO: EXECVE
                sys_execve((String)varargs[0]);

                break;
            case IO_REQUEST:
                //TODO: IO_REQUEST
                //TODO: myprints System.out.println("syscall IO REQ");
                sys_io_request((Integer)(varargs[0]), (Integer)(varargs[1]));
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;
            case TERMINATE_PROCESS:
                //TODO: TERMINATE PROCESS
                //TODO: myprints System.out.println("syscall TERMINATE PROC");
                sys_terminate_process();
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;
            default:
                return 1;
        }

        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);

        System.out.printf("Time: %010d Kernel exit\n", Simulator.timer.getSystemTime());
        return 0;
    }

    //TODO: make these return int?
    private void sys_make_device(int deviceId, String deviceType){
        IODeviceImp dev = new IODeviceImp(deviceId, deviceType);
        devices.add(dev);

        //TODO: myprints System.out.println(dev);
        //TODO: myprints System.out.println(devices.size());
    }

    private void sys_execve(String programFilename){
        try {
            Scanner program = new Scanner(new FileReader(programFilename));
            ProcessControlBlockImp pcb = new ProcessControlBlockImp(programFilename);

            //TODO: myprints System.out.println("\nLOADING PROG: " + programFilename);
            while(program.hasNext()){

                String[] data = program.nextLine().trim().split("\\s+");

                String instructionType = data[0];

                if(instructionType.equalsIgnoreCase("CPU")){
                    int instructionDuration = Integer.parseInt(data[1]);
                    pcb.addInstruction(new CPUInstruction(instructionDuration));
                }else if(instructionType.equalsIgnoreCase("I/O") || instructionType.equalsIgnoreCase("IO")){
                    int instructionDuration = Integer.parseInt(data[1]);
                    int deviceId = Integer.parseInt(data[2]);
                    pcb.addInstruction(new IOInstruction(instructionDuration, deviceId));
                }
            }

            ready.add(pcb);
            //TODO: myprints System.out.println("Added " + pcb.getNumInstructions() + " instructions to it. rQsiz: " + ready.size());
            if(Simulator.cpu.isIdle()){
                ProcessControlBlock next = ready.poll();
                Simulator.cpu.contextSwitch(next);


                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, next);

                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
            }
        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError loading program: " + programFilename);
        }
    }

    private void sys_io_request(int deviceId, int duration){
        ProcessControlBlock curr = Simulator.cpu.getCurrentProcess();

        for(IODevice iod: devices) {
            if(iod.getID() == deviceId){
                long completeTime = iod.requestIO(duration, curr);
                Simulator.eventQueue.removeTimeoutEvent(curr.getPID());
                Simulator.eventQueue.add(new WakeUpEvent(completeTime, iod, curr));
                break;
            }
        }

        ProcessControlBlock next = ready.poll();
        Simulator.cpu.contextSwitch(next);
        if(next != null){
            int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
            Simulator.timer.scheduleInterrupt(interruptTime, next);
        }
    }

    private void sys_terminate_process(){
        ProcessControlBlock curr = Simulator.cpu.getCurrentProcess();
        Simulator.eventQueue.removeTimeoutEvent(curr.getPID());

        ProcessControlBlock next = ready.poll();
        Simulator.cpu.contextSwitch(next);
        if(next != null){

            int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
            Simulator.timer.scheduleInterrupt(interruptTime, next);
        }
    }

    public ProcessControlBlock nextProcess(){
        return ready.poll();
    }

    public void addToReady(ProcessControlBlock pcb) {
        ready.add(pcb);
    }

    private void sysCallTrace(int number, Object... varargs) {
        String details=null;
        switch (number) {
            case MAKE_DEVICE:
                details=String.format("MAKE_DEVICE, %s,\"%s\"", varargs[0], varargs[1]);
                break;
            case EXECVE:
                details=String.format("EXECVE, \"%s\"", varargs[0]);
                break;
            case IO_REQUEST:
                details=String.format("IO_REQUEST, %s, %s", varargs[0], varargs[1]);
                break;
            case TERMINATE_PROCESS:
                details="TERMINATE_PROCESS";
                break;
            default:
                details="ERROR_UNKNOWN_NUMBER";
        }
        System.out.printf("Time: %010d SysCall(%s)\n", Simulator.timer.getSystemTime(), details);
    }

    private void interruptTrace(int interruptType, Object... varargs) {
        String details = null;
        switch (interruptType) {
            case TIME_OUT:
                details=String.format("TIME_OUT, %s", varargs[0]);
                break;
            case WAKE_UP:
                details=String.format("WAKE_UP, %s, %s", varargs[0], varargs[1]);
                break;
            default:
                details="ERROR_UNKNOWN_NUMBER";
        }
        System.out.printf("Time: %010d Interrupt(%s)\n", Simulator.timer.getSystemTime(), details);
    }
}
