import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * muhummad        15/03/19.
 */
public class Simulator {

    static CPUImp cpu;
    static KernelImp kernel;
    static SystemTimerImp timer;
    static EventQueue eventQueue;
    static int dispatchOverhead;

    public static void setUp(String configFilename, int sliceLength){
        kernel = new KernelImp(sliceLength);
        cpu = new CPUImp();
        timer = new SystemTimerImp();
        eventQueue = new EventQueue();

        try {
            Scanner infile = new Scanner(new FileReader(configFilename));

            while(infile.hasNext()){
                String[] data = infile.nextLine().trim().split("\\s+");

                if(data[0].equalsIgnoreCase("I/O") || data[0].equalsIgnoreCase("I/O") || data[0].equalsIgnoreCase("DEVICE")){
                    //MAKE_DEVICE system call to kernel
                    kernel.syscall(SystemCall.MAKE_DEVICE, data[1], data[2]);
                }else if(data[0].equalsIgnoreCase("PROGRAM")){
                    //create load program evt and insert into evt queue
                    eventQueue.add(new ExecveEvent(Long.parseLong(data[1]), data[2]));


                }
            }

            timer.reset();

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError opening file.");
            System.exit(1);
        }

    }

    public static void runSimulation(){

        while(!(eventQueue.isEmpty() && cpu.isIdle())) {
            System.out.println("looopstart. ==================================");

            Event nextEvent = eventQueue.peek();

            if (eventQueue.peek() != null) {
                timer.setSystemTime(eventQueue.peek().getTime());
            }

            while (nextEvent != null && nextEvent.getTime() <= timer.getSystemTime()) {
                //we have an event to process so process it
                //System.out.println("l1");

                eventQueue.poll();
                nextEvent.process();

                nextEvent = eventQueue.peek();
            }

            long unusedTime = 0;
            while(eventQueue.peek() != null && eventQueue.peek().getTime() > timer.getSystemTime() && !cpu.isIdle()){
                //System.out.println("l2");
                cpu.execute((int)(eventQueue.peek().getTime() - timer.getSystemTime()));
            }
        }


        System.out.println(timer);
    }

    public static void main(String[] args) {

        String configFilename = args[0];
        int sliceLength = (int)Double.parseDouble(args[1]);
        dispatchOverhead =(int)Double.parseDouble(args[2]);

        setUp(configFilename, sliceLength);
        runSimulation();
    }
}
