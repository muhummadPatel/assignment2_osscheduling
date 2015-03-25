/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class CPUImp implements CPU {

    ProcessControlBlock currentProcess;
    int numContextSwitches = 0;

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
               Simulator.cpu.contextSwitch(next);
           }
       }

        if(currentProcess != null) {
            //we have a process to execute


            int remainder = ((CPUInstruction) currentProcess.getInstruction()).execute(timeUnits);


            if (remainder >= 0) {
                //completed so move to next instruction

                Simulator.timer.advanceUserTime(timeUnits - remainder);

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
                //not completed, so move usertime ahead by entire allocation
                Simulator.timer.advanceUserTime(timeUnits);
                unusedTimeUnits = 0;
            }

        }else{


            unusedTimeUnits = timeUnits;
        }


        return unusedTimeUnits;
    }

    @Override
    public ProcessControlBlock contextSwitch(ProcessControlBlock process) {
        numContextSwitches++;

        ProcessControlBlock switchedOut = this.currentProcess;
        currentProcess = process;

        String out = (switchedOut == null)? "{Idle}" : switchedOut.toString();
        String in = (currentProcess == null)? "{Idle}" : currentProcess.toString();
        System.out.printf("Time: %010d Context Switch(%s, %s)\n", Simulator.timer.getSystemTime(), out, in);
        return switchedOut;
    }

    @Override
    public boolean isIdle() {
        return (currentProcess == null);
    }
}
