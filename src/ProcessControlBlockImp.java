import java.util.LinkedList;

/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class ProcessControlBlockImp implements ProcessControlBlock {

    private static int count = 0;
    private int pid;
    private String programName;
    private LinkedList<Instruction> instructions;
    private int programCounter;
    private State state;

    public ProcessControlBlockImp(String programName){
        pid = count;
        count++;
        this.programName = programName;
        instructions = new LinkedList<Instruction>();
        programCounter = 0;
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
        return instructions.get(programCounter);
    }

    @Override
    public void nextInstruction() {
        programCounter++;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }
}
