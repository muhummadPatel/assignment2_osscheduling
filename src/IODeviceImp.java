import java.util.LinkedList;

/**
 * Implementation of the IODevice class. This class simulates an IO device.
 */
public class IODeviceImp implements IODevice {

    private int id;
    private String name;
    private long freeTime;

    //the device queue to hold devices waiting to use this device.
    LinkedList<ProcessControlBlock> deviceQueue = new LinkedList<ProcessControlBlock>();

    //Constructor method to set up this device
    public IODeviceImp(int id, String name){
        this.id = id;
        this.name = name;
        this.freeTime = 0;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getFreeTime() { return freeTime;}

    //places the given process on the deviceQueue and updates the freetime of the device.
    //returns the time at which the device will free up again.
    @Override
    public long requestIO(int duration, ProcessControlBlock process) {
        deviceQueue.add(process);

        if(freeTime <= Simulator.timer.getSystemTime()){
            //device is free now
            freeTime = Simulator.timer.getSystemTime() + duration;
        }else{
            freeTime += duration;
        }

        return freeTime;
    }

    public String toString(){
        return ("dev: " + id + " " + name);
    }
}
