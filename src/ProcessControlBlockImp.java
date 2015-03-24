import java.util.LinkedList;

/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class ProcessControlBlockImp implements ProcessControlBlock {

    private static int count = 1;
    private int pid;
    private String programName;
    private LinkedList<Instruction> instructions;
    private int programCounter;
    private State state;

    public ProcessControlBlockImp(String programName){
        this.pid = count;
        this.programName = programName;
        this.instructions = new LinkedList<Instruction>();
        this.programCounter = 0;

        count++;
    }

    @Override
    public int getPID() {
        return pid;
    }

    @Override
    public String getProgramName() {
        return programName;
    }

    @Override
    public Instruction getInstruction() {
        //TODO: myprints System.out.println(programName + " pc=" + programCounter);
        Instruction instruction = instructions.get(programCounter);
        return instruction;
    }

    @Override
    public void nextInstruction() {
        programCounter++;
    }

    @Override
    public boolean hasNextInstruction(){
        int next = programCounter + 1;
        return (!(next >= instructions.size()));
    }

    public void addInstruction(Instruction instruction){
        this.instructions.add(instruction);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    public String toString() {
        return String.format("{%d, %s}", this.getPID(), this.getProgramName());
    }
}
