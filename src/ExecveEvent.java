/**
 * An Execve event represents the creation of a program execution.
 * 
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class ExecveEvent extends Event {

    private String progName;
    
    public ExecveEvent(long startTime, String progName) {
        super(startTime);
        this.progName=progName;
    }

    /**
     * Obtain the name of the program that must be run.
     */
    public String getProgramName() {
        return progName;
    }

    @Override
    public void process() {
        Simulator.kernel.syscall(SystemCall.EXECVE, progName);
    }

    @Override
    public boolean equals(Object o){

        if(o instanceof ExecveEvent){
            ExecveEvent other = (ExecveEvent)o;
            return ((this.progName.equalsIgnoreCase(other.progName)) && (this.getTime() == other.getTime()));
        }

        return false;
    }

    public String toString() { return "ExecveEvent("+this.getTime()+", "+this.getProgramName()+")"; }
}
