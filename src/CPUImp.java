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

        int unusedTimeUnits = 0;

       if(currentProcess == null){
           //load next process if available
           ProcessControlBlock next = Simulator.kernel.nextProcess();
           if(next != null){
               //something to execute, so switch it in
               //TODO: if timing is off, try just doing this manually
               Simulator.cpu.contextSwitch(next);
           }
       }

        if(currentProcess != null) {
            //we have a process to execute
            System.out.println("EXECUTE " + currentProcess.getProgramName());

            int remainder = ((CPUInstruction) currentProcess.getInstruction()).execute(timeUnits);
            System.out.println("Unused time = " + remainder);

            if (remainder >= 0) {
                //completed so move to next instruction
                if (currentProcess.hasNextInstruction()) {
                    currentProcess.nextInstruction();
                    //has next
                    IOInstruction ioInstruction = (IOInstruction) (currentProcess.getInstruction());
                    Simulator.kernel.syscall(SystemCall.IO_REQUEST, ioInstruction.getDeviceID(), ioInstruction.getDuration());
                } else {
                    //no next
                    Simulator.kernel.syscall(SystemCall.TERMINATE_PROCESS);
                }

                unusedTimeUnits = remainder;
            }else{
                unusedTimeUnits = 0;
            }

        }else{
            System.out.println("nothing to execute.");
            //TODO: increment idle time here?
            unusedTimeUnits = timeUnits;
        }

        Simulator.timer.advanceUserTime(timeUnits - unusedTimeUnits);
        return unusedTimeUnits;
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
