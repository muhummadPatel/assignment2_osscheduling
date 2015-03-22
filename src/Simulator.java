import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * muhummad        15/03/19.
 */
public class Simulator {

    static CPUImp cpu;
    static KernelImp kernel;
    static SystemTimer timer;
    static EventQueue eventQueue;

    public static void setUp(String configFilename, int sliceLength){
        kernel = new KernelImp(sliceLength);
        cpu = new CPUImp();
        timer = new SystemTimerImp();
        eventQueue = new EventQueue();

        try {
            Scanner infile = new Scanner(new FileReader(configFilename));

            while(infile.hasNext()){
                String[] data = infile.nextLine().trim().split("\\s+");

                if(data[0].equalsIgnoreCase("I/O") || data[0].equalsIgnoreCase("I/O")){
                    //MAKE_DEVICE system call to kernel
                    kernel.syscall(SystemCall.MAKE_DEVICE, data[1], data[2]);
                }else if(data[0].equalsIgnoreCase("PROGRAM")){
                    //create load program evt and insert into evt queue
                    eventQueue.add(new ExecveEvent(Long.parseLong(data[1]), data[2]));
                    System.out.println("Execve evt added to queue." + eventQueue.peek());
                    System.out.println("Qsiz: " + eventQueue.size());
                }
            }

            timer.setSystemTime(0);

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError opening file.");
            System.exit(1);
        }
        System.out.println("completed " + timer.getSystemTime());
    }

    public static void runSimulation(){
        System.out.println("\n\n\nLOOOOOOOOPPPPP");
        while(!(eventQueue.isEmpty() && cpu.isIdle())){
            System.out.println("looopstart. ==================================");

            Event nextEvent = eventQueue.poll();
            if(nextEvent != null){
                nextEvent.process();
            }

            //TODO: fix this madness
            //int nextEventTime = (int)eventQueue.peek().getTime();
            cpu.execute(5);
            System.out.println("looopend. ==================================\n\n\n");
        }
    }

    public static void main(String[] args) {
        //TODO: handle invalid args here
        String configFilename = args[0];
        int sliceLength = (int)Double.parseDouble(args[1]);
        int dispatchOverhead =(int)Double.parseDouble(args[2]);

        setUp(configFilename, sliceLength);
        runSimulation();
    }
}
