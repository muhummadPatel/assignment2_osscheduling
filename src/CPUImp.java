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
        return null;
    }

    @Override
    public int execute(int timeUnits) {
        Instruction currentInstruction = currentProcess.getInstruction();
        if(currentInstruction instanceof CPUInstruction){
            int remaining = Math.abs(((CPUInstruction) currentInstruction).execute(timeUnits));
            if(remaining < 0){
                //completed
                if(currentProcess.hasNextInstruction()){
                    //has next
                    currentProcess.nextInstruction();
                    IOInstruction ioInstruction = (IOInstruction)currentProcess.getInstruction();
                    Simulator.kernel.syscall(SystemCall.IO_REQUEST, ioInstruction.getDeviceID(), ioInstruction.getDuration());
                }else{
                    //no next
                    Simulator.kernel.syscall(SystemCall.TERMINATE_PROCESS);
                }
            }else{
                //cant complete
            }
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
