import java.util.LinkedList;

/**
 * Implementation of the ProcessControlBlock interface. This class simulates a process. It includes the text(program),
 * and other associated data that would usually be stored in the PCB.
 */
public class ProcessControlBlockImp implements ProcessControlBlock {

    //used to assign unique PIDs
    private static int count = 1;

    private int pid;
    private String programName;
    private LinkedList<Instruction> instructions;
    private int programCounter;
    private State state;

    //constructor method set up the PCB
    public ProcessControlBlockImp(String programName){
        this.pid = count;
        this.programName = programName;
        this.instructions = new LinkedList<Instruction>();
        this.programCounter = 0;

        //increment so that next PCB will have unique PID
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

    //Add instruction to this process (used when loading the program in)
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
