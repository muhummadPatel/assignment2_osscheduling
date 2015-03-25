/**
 * Implementation of the PCU interface. This class simulates the behaviour of a CPU. it allows the kernel to switch
 * processes on and off (using the contextSwitch method) and to execute the current process.
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

    //simulates execution of the current process for the given number of timeunits
    @Override
    public int execute(int timeUnits) {

        int unusedTimeUnits = 0;

        //if there is nothing currently on the cpu
       if(currentProcess == null){
           //try to get the next scheduled process
           ProcessControlBlock next = Simulator.kernel.nextProcess();

           if(next != null){
               //there is something scheduled, so load it on
               Simulator.cpu.contextSwitch(next);
           }
       }

        if(currentProcess != null) {
            //we have a process to execute, so execute it

            //execute the current instruction
            int remainder = ((CPUInstruction) currentProcess.getInstruction()).execute(timeUnits);

            if (remainder >= 0) {
                //instruction completed so move to next instruction, if available

                //increase userTime by amount spent doing the instruction
                Simulator.timer.advanceUserTime(timeUnits - remainder);

                if (currentProcess.hasNextInstruction()) {
                    currentProcess.nextInstruction();
                    //next instruction exists, so do it
                    IOInstruction ioInstruction = (IOInstruction) (currentProcess.getInstruction());
                    Simulator.kernel.syscall(SystemCall.IO_REQUEST, ioInstruction.getDeviceID(), ioInstruction.getDuration());
                } else {
                    //no next instruction available, so terminate the process
                    Simulator.kernel.syscall(SystemCall.TERMINATE_PROCESS);
                }

                unusedTimeUnits = remainder;
            }else{
                //instruction did not complete, so move userTime ahead by entire allocation
                Simulator.timer.advanceUserTime(timeUnits);
                unusedTimeUnits = 0;
            }
        }else{
            unusedTimeUnits = timeUnits;
        }

        return unusedTimeUnits;
    }

    //simulates a context switch (swapping in/out processes)
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

    //returns true if the CPU is doing nothing
    @Override
    public boolean isIdle() {
        return (currentProcess == null);
    }
}
