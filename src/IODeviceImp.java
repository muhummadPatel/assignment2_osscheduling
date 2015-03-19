/**
 * muhummad        15/03/19.
 */
public class IODeviceImp implements IODevice {

    private int id;
    private String name;
    private long freeTime;

    public IODeviceImp(){
        this(-1, "nulldev");
    }

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
    public long getFreeTime() {
        return freeTime;
    }

    @Override
    public long requestIO(int duration, ProcessControlBlock process) {
        return 0;
    }

    public String toString(){
        return ("dev: " + id + " " + name);
    }
}