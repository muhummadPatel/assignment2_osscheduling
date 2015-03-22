
/**
 * A WakeUp event occurs when an I/O operation completes.
 * 
 * @author Stephan Jamieson
 * @version 8/3/15
 */
public class WakeUpEvent extends Event {
    
    private IODevice device;
    private ProcessControlBlock process;
    
    /**
     * Create a WakeUpEvent for the given process waiting on an I/O operation on the given device.
     */
    public WakeUpEvent(long systemTime, IODevice device, ProcessControlBlock process) {
        super(systemTime);
        this.device=device;
        this.process=process;
    }
    
    /**
     * Obtain the I/O device.
     */
    public IODevice getDevice() { return device; }
    
    /**
     * Obtain the waiting process.
     */
    public ProcessControlBlock getProcess() { return process; }

    @Override
    public void process() {
        System.out.println("process wakeupevt");
        Simulator.kernel.interrupt(InterruptHandler.WAKE_UP, device.getID(), process.getPID());
    }

    @Override
    public boolean equals(Object o){

        if(o instanceof WakeUpEvent){
            WakeUpEvent other = (WakeUpEvent)o;
            return ((this.device == other.device) && (this.process.getPID() == other.process.getPID()) && (this.getTime() == other.getTime()));
        }

        return false;
    }
}
