/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class CPUImp implements CPU {

    ProcessControlBlockImp currentProcess;

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
        return null;
    }

    @Override
    public boolean isIdle() {
        return (currentProcess == null);
    }
}
