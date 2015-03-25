/**
 * Implementation of the SystemTimer interface. This class keeps track of the time in out simulation. Various things are
 * recorded including the overall systemTime, userTime, and kernelTime.
 */
public class SystemTimerImp implements SystemTimer {

    //variables to keep track of the time spent in userspace, kernelspace and the overall system time
    private long systemTime, userTime, kernelTime;

    //Constructor method intialises everything to zero
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

    //NOTE: also advances systemTime
    @Override
    public void advanceUserTime(long time) {
        this.userTime += time;
        this.systemTime += time;
    }

    //NOTE: also advances systemTime
    @Override
    public void advanceKernelTime(long time) {
        this.kernelTime += time;
        this.systemTime += time;
    }

    //Resets the clock. Everything is reset to 0;
    public void reset(){
        systemTime = 0;
        userTime = 0;
        kernelTime = 0;
    }

    //Schedule a timeoutevt in timeUnits for the process given
    @Override
    public void scheduleInterrupt(int timeUnits, ProcessControlBlock process) {
        Simulator.eventQueue.add(new TimeOutEvent(timeUnits, process));
    }

    //Cancel the timeoutEvt on the given process
    @Override
    public void cancelInterrupt(int processID) {
        Simulator.eventQueue.removeTimeoutEvent(processID);
    }

    //return a string containing all relevant details ars required by the assignment specifications
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
