import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Implementation of the Kernel class. This class simulates the behaviour of the kernel. It schedules processes and
 * handles interrupts and system calls. Also responsible for pre-empting processes of necessary, managing the
 * readyQueue, and managing the devices.
 */
public class KernelImp implements Kernel {

    //timeSlice to be used for the scheduling algorithm
    int timeslice;

    //deviceList containing all available devices
    private ArrayList<IODeviceImp> devices = new ArrayList<IODeviceImp>();

    //readyQueue used for scheduling. Contains all the processes waiting to be processed.
    private Queue<ProcessControlBlock> ready = new LinkedList<ProcessControlBlock>();

    //constructor method. Needs to initialise the timeslice for use in scheduling
    public KernelImp(int timeslice){
        this.timeslice = timeslice;
    }

    //handle interrupts
    @Override
    public void interrupt(int interruptType, Object... varargs) {
        interruptTrace(interruptType, varargs);

        //determine type of interrupt and fire off the correct interrupt handler method
        switch(interruptType){
            case InterruptHandler.TIME_OUT:
                interrupt_time_out();
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;

            case InterruptHandler.WAKE_UP:
                interrupt_wake_up((Integer) (varargs[0]), (Integer) (varargs[1]));
                break;
        }

        //interrupts cost SC time units
        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);
        System.out.printf("Time: %010d Kernel exit\n", Simulator.timer.getSystemTime());
    }

    //Handle a TIMEOUT interrupt by removing current process from CPU and switching in the next ready process
    private void interrupt_time_out(){
        ProcessControlBlock current = Simulator.cpu.getCurrentProcess();
        if(current != null){
            ready.add(current);

            ProcessControlBlock switchedIn = ready.poll();

            Simulator.cpu.contextSwitch(switchedIn);
            if(switchedIn != null) {
                //schedule an interrupt for the process we just switched in
                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, switchedIn);
            }
        }
    }

    //handle a WAKEUP interrupt by removing process from device queue and adding it to ready queue
    private void interrupt_wake_up(int deviceId, int pid){
        //ref to the relevant deviceQueue
        LinkedList<ProcessControlBlock> relQueue = new LinkedList<ProcessControlBlock>();

        //find the device
        for(IODeviceImp iod: devices){
            if(iod.getID() == deviceId){
                relQueue = iod.deviceQueue;
                break;
            }
        }

        //find the relevant process in that deviceQeueue
        int pos = -1;
        for(int i = 0; i < relQueue.size(); i++){
            ProcessControlBlock pcb = relQueue.get(i);
            if(pcb.getPID() == pid){
                pos = i;
                break;
            }
        }

        //if we found the process, then remove it from the deviceQueue and add it to the readyQueue
        if(pos != -1){
            ProcessControlBlock relProcess = relQueue.remove(pos);
            relProcess.nextInstruction();
            ready.add(relProcess);

            if(Simulator.cpu.isIdle()){
                //if theres nothing on the cpu atm, we can put the process on the cpu
                ProcessControlBlock switchedIn = ready.poll();
                Simulator.cpu.contextSwitch(switchedIn);
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);

                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, switchedIn);
            }
        }
    }

    //handle syscalls sent to the kernel
    @Override
    public int syscall(int number, Object... varargs) {
        sysCallTrace(number, varargs);

        //determine which syscall was requested and fire off the relevant syscall method
        switch(number){
            case MAKE_DEVICE:
                sys_make_device(Integer.parseInt((String) varargs[0]), (String) varargs[1]);
                break;

            case EXECVE:
                sys_execve((String)varargs[0]);
                break;

            case IO_REQUEST:
                sys_io_request((Integer)(varargs[0]), (Integer)(varargs[1]));
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;

            case TERMINATE_PROCESS:
                sys_terminate_process();
                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
                break;

            default:
                return 1;
        }

        //syscalls cost SC time units
        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);
        System.out.printf("Time: %010d Kernel exit\n", Simulator.timer.getSystemTime());

        return 0;
    }


    //syscall method for MAKEDEVICE
    private void sys_make_device(int deviceId, String deviceType){
        //create device and add it to the devices queue
        IODeviceImp dev = new IODeviceImp(deviceId, deviceType);
        devices.add(dev);
    }

    //syscall method for EXECVE
    private void sys_execve(String programFilename){
        try {
            //read in the program file and create a pcb
            Scanner program = new Scanner(new FileReader(programFilename));
            ProcessControlBlockImp pcb = new ProcessControlBlockImp(programFilename);

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

            //add newly created pcb to the readyQueue
            ready.add(pcb);

            //if the CPU is idle, we can put the process on
            if(Simulator.cpu.isIdle()){
                ProcessControlBlock next = ready.poll();
                Simulator.cpu.contextSwitch(next);

                //Schedule an interrupt for the newly scheduled process
                int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
                Simulator.timer.scheduleInterrupt(interruptTime, next);

                Simulator.timer.advanceKernelTime(Simulator.dispatchOverhead);
            }
        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError loading program: " + programFilename);
        }
    }

    //syscall method for IOREQUEST
    private void sys_io_request(int deviceId, int duration){
        ProcessControlBlock curr = Simulator.cpu.getCurrentProcess();

        //request IO, remove previously scheduled timeoutEvt, schedule a wakeupevt, and move next process onto cpu
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

    //syscall method for TERMINATEPROCESS
    private void sys_terminate_process(){
        //remove timeoutEvt scheduled for the process
        ProcessControlBlock curr = Simulator.cpu.getCurrentProcess();
        Simulator.eventQueue.removeTimeoutEvent(curr.getPID());

        //Move the next ready process onto the CPU
        ProcessControlBlock next = ready.poll();
        Simulator.cpu.contextSwitch(next);

        if(next != null){
            //schedule an interrupt for the process we just put on the cpu
            int interruptTime = (int)Simulator.timer.getSystemTime() + SystemTimer.SYSCALL_COST + Simulator.dispatchOverhead + timeslice;
            Simulator.timer.scheduleInterrupt(interruptTime, next);
        }
    }

    //pull the next process off the ready queue (used by CPU to pull whatever is scheduled for execution)
    public ProcessControlBlock nextProcess(){
        return ready.poll();
    }

    //method to trace syscall execution via printlining
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

    //method to trace interrupt handling via printlining
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
