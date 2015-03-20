import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * muhummad        15/03/19.
 */
public class KernelImp implements Kernel {

    //<device id, queue of pids waiting for device with device id>
    private HashMap<Integer, Queue<Integer>> deviceQueues = new HashMap<Integer, Queue<Integer>>();


    @Override
    public void interrupt(int interruptType, Object... varargs) {

    }

    @Override
    public int syscall(int number, Object... varargs) {
        switch(number){
            case MAKE_DEVICE:
                IODeviceImp dev = new IODeviceImp(Integer.parseInt((String) varargs[0]), (String)varargs[1]);
                System.out.println(dev);
                deviceQueues.put(dev.getID(), new LinkedList<Integer>());
                System.out.println(deviceQueues.size());
                break;
            case EXECVE:
                //TODO: EXECVE
                break;
            case IO_REQUEST:
                //TODO: IO_REQUEST
                break;
            case TERMINATE_PROCESS:
                //TODO: TERMINATE PROCESS
                break;
            default:
                return 1;
        }

        Simulator.timer.advanceKernelTime(SystemTimer.SYSCALL_COST);
        return 0;
    }
}
