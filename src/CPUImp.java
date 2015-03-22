/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class CPUImp implements CPU {

    ProcessControlBlock currentProcess;

    public CPUImp(){
        currentProcess = null;
    }

    @Override
    public ProcessControlBlock getCurrentProcess() {
        return currentProcess;
    }

    @Override
    public int execute(int timeUnits) {
       if(currentProcess == null){
           //load next process if available
           ProcessControlBlock next = Simulator.kernel.nextProcess();
           if(next == null){
               //nothing in ready queue
               return timeUnits;
           }else{
               //something to execute
               Simulator.cpu.contextSwitch(next);
               //Simulator.timer.scheduleInterrupt(Simulator.kernel.timeslice, next);

           }
       }

        if(currentProcess != null) {
            System.out.println("EXECUTE " + currentProcess.getProgramName());

            Instruction currentInstruction = currentProcess.getInstruction();
            int remaining = ((CPUInstruction) currentInstruction).execute(timeUnits);
            if (remaining >= 0) {
                //completed
                if (currentProcess.hasNextInstruction()) {
                    currentProcess.nextInstruction();
                    //has next
                    IOInstruction ioInstruction = (IOInstruction) (currentProcess.getInstruction());
                    Simulator.kernel.syscall(SystemCall.IO_REQUEST, ioInstruction.getDeviceID(), ioInstruction.getDuration());
                } else {
                    //no next
                    Simulator.kernel.syscall(SystemCall.TERMINATE_PROCESS);
                }
            }
            return timeUnits - Math.abs(remaining);
        }else{
            System.out.println("nothing to execute.");
        }
        return timeUnits;
    }

    @Override
    public ProcessControlBlock contextSwitch(ProcessControlBlock process) {
        ProcessControlBlock switchedOut = this.currentProcess;
        currentProcess = process;

        return switchedOut;
    }

    @Override
    public boolean isIdle() {
        return (currentProcess == null);
    }
}
