import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * muhummad        15/03/19.
 */
public class KernelImp implements Kernel {

    private int timeslice;

    //<device id, queue of pids waiting for device with device id>
    private HashMap<Integer, Queue<ProcessControlBlock>> deviceQueues = new HashMap<Integer, Queue<ProcessControlBlock>>();
    private Queue<ProcessControlBlock> ready = new LinkedList<ProcessControlBlock>();


    public KernelImp(int timeslice){
        this.timeslice = timeslice;
    }

    @Override
    public void interrupt(int interruptType, Object... varargs) {
        switch(interruptType){
            case InterruptHandler.TIME_OUT:
                ProcessControlBlock switchedIn = ready.poll();
                ProcessControlBlock switchedOut = Simulator.cpu.contextSwitch(switchedIn);
                ready.add(switchedOut);
                Simulator.timer.scheduleInterrupt(timeslice, switchedIn.getPID());
                break;
            case InterruptHandler.WAKE_UP:
                int deviceId = Integer.parseInt((String)varargs[0]);
                int pid = Integer.parseInt((String)varargs[1]);

                LinkedList<ProcessControlBlock> deviceQueue = (LinkedList<ProcessControlBlock>)deviceQueues.get(deviceId);
                int pos = -1;
                for(int i = 0; i < deviceQueue.size(); i++){
                    ProcessControlBlock pcb = deviceQueue.get(i);
                    if(pcb.getPID() == pid){
                        pos = i;
                        break;
                    }
                }

                //TODO: some error checking?
                if(pos != -1){
                    ProcessControlBlock removed = deviceQueue.remove(pos);
                    removed.nextInstruction();
                    ready.add(removed);
                }

                break;
        }
    }

    @Override
    public int syscall(int number, Object... varargs) {
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
                break;
            case TERMINATE_PROCESS:
                //TODO: TERMINATE PROCESS
                break;
            default:
                return 1;
        }

        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);
        return 0;
    }

    //TODO: make these return int?
    private void sys_make_device(int deviceId, String deviceType){
        IODeviceImp dev = new IODeviceImp(deviceId, deviceType);
        deviceQueues.put(dev.getID(), new LinkedList<ProcessControlBlock>());

        System.out.println(dev);
        System.out.println(deviceQueues.size());
    }

    private void sys_execve(String programFilename){
        try {
            Scanner program = new Scanner(new FileReader(programFilename));
            ProcessControlBlockImp pcb = new ProcessControlBlockImp(programFilename);

            System.out.println("\nLOADING PROG: " + programFilename);
            while(program.hasNext()){

                String[] data = program.nextLine().trim().split("\\s+");

                String instructionType = data[0];

                if(instructionType.equalsIgnoreCase("CPU")){
                    int instructionDuration = Integer.parseInt(data[1]);
                    pcb.addInstruction(new CPUInstruction(instructionDuration));
                }else if(instructionType.equalsIgnoreCase("I/O") || data[1].equalsIgnoreCase("IO")){
                    int instructionDuration = Integer.parseInt(data[1]);
                    int deviceId = Integer.parseInt(data[2]);
                    pcb.addInstruction(new IOInstruction(instructionDuration, deviceId));
                }
            }
            ready.add(pcb);
            System.out.println("Added " + pcb.getNumInstructions() + " instructions to it. rQsiz: " + ready.size());

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError loading program: " + programFilename);
        }
    }
}
