/**
 * Created by ptlmuh006 on 2015/03/20
 */
public class SystemTimerImp implements SystemTimer {

    private long systemTime, userTime, kernelTime;

    public SystemTimerImp(){
        systemTime = 0;
        userTime = 0;
        kernelTime = 0;
    }

    @Override
    public long getSystemTime() {
        return systemTime;
    }

    @Override
    public long getIdleTime() {
        return systemTime - (userTime + kernelTime);
    }

    @Override
    public long getUserTime() {
        return userTime;
    }

    @Override
    public long getKernelTime() {
        return kernelTime;
    }

    @Override
    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    @Override
    public void advanceSystemTime(long time) {
        this.systemTime += time;
    }

    @Override
    public void advanceUserTime(long time) {
        this.userTime += time;
        this.systemTime += time;
    }

    @Override
    public void advanceKernelTime(long time) {
        this.kernelTime += time;
        this.systemTime += time;
    }

    public void reset(){
        systemTime = 0;
        userTime = 0;
        kernelTime = 0;
    }

    @Override
    public void scheduleInterrupt(int timeUnits, ProcessControlBlock process) {
        Simulator.eventQueue.add(new TimeOutEvent(timeUnits, process));
    }

    @Override
    public void cancelInterrupt(int processID) {
        Simulator.eventQueue.removeTimeoutEvent(processID);
    }

    public String toString(){
        String timeStats = "";
        timeStats += "System time: " + systemTime + "\n";
        timeStats += "Kernel time: " + kernelTime + "\n";
        timeStats += "User time: " + userTime + "\n";
        timeStats += "Idle time: " + getIdleTime() + "\n";
        timeStats += "Context switches: " + Simulator.cpu.numContextSwitches + "\n";
        timeStats += String.format("CPU utilization: %5.2f", ((userTime / (systemTime * 1.0)) * 100));

        return timeStats;
    }
}
