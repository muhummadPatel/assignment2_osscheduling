/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class ProcessControlBlockImp implements ProcessControlBlock {

    private static int count = 0;
    private int pid;
    private String programName;
    private Instruction instruction;
    private State state;

    public ProcessControlBlockImp(String programName){
        pid = count;
        count++;
        this.programName = programName;
    }

    @Override
    public int getPID() {
        return 0;
    }

    @Override
    public String getProgramName() {
        return null;
    }

    @Override
    public Instruction getInstruction() {
        return null;
    }

    @Override
    public void nextInstruction() {

    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void setState(State state) {

    }
}
