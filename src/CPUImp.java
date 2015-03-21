/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class CPUImp implements CPU {

    ProcessControlBlock currentProcess;

    public CPUImp(){
        //currentProcess = null;
    }

    @Override
    public ProcessControlBlock getCurrentProcess() {
        return null;
    }

    @Override
    public int execute(int timeUnits) {
        return 0;
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
