import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Simulator class handles all the simulation components and drives the simulation. This class contains the main
 * simulation loop and sets up the simulation. Sort of like the motherboard that interconnects all the components on a
 * real computer.
 */
public class Simulator {

    //All the components (static so that they can access each other if needed just like a real computer)
    static CPUImp cpu;
    static KernelImp kernel;
    static SystemTimerImp timer;
    static EventQueue eventQueue;
    static int dispatchOverhead; //the cost of context switching

    //Method to set up the simulation. Initialises and loads everything required for the main simulation loop to run.
    public static void setUp(String configFilename, int sliceLength){
        //initialising components
        kernel = new KernelImp(sliceLength);
        cpu = new CPUImp();
        timer = new SystemTimerImp();
        eventQueue = new EventQueue();

        //Read in config file
        try {
            Scanner infile = new Scanner(new FileReader(configFilename));

            //add IO devices and programs
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

            //reset the timer to 0
            timer.reset();

        }catch(FileNotFoundException e){
            //Couldn't set up the simulation, so exit the program
            System.out.println(e.getMessage() + "\nError opening file.");
            System.exit(1);
        }
    }

    //THe main simulation loop
    public static void runSimulation(){
        //While the are more events and the CPU is doing something
        while(!(eventQueue.isEmpty() && cpu.isIdle())) {

            //if there is an event in the queue, move the time along to that event
            Event nextEvent = eventQueue.peek();
            if (eventQueue.peek() != null) {
                timer.setSystemTime(eventQueue.peek().getTime());
            }

            //While there are events to process at or before current systemTime
            while (nextEvent != null && nextEvent.getTime() <= timer.getSystemTime()) {
                //remove from eventQueue and process it
                eventQueue.poll();
                nextEvent.process();

                nextEvent = eventQueue.peek();
            }

            //Until there is an event that needs to run, keep running the cpu
            while(eventQueue.peek() != null && eventQueue.peek().getTime() > timer.getSystemTime() && !cpu.isIdle()){
                cpu.execute((int)(eventQueue.peek().getTime() - timer.getSystemTime()));
            }
        }

        //after the main loop, print the required timing data
        System.out.println(timer);
    }

    //Main method. This is run when the program is executed
    public static void main(String[] args) {
        try {
            //parse cmd line args
            String configFilename = args[0];
            int sliceLength = (int) Double.parseDouble(args[1]);
            dispatchOverhead = (int) Double.parseDouble(args[2]);

            setUp(configFilename, sliceLength); //set up the simulation
            runSimulation();//run simulation to completion
        }catch(Exception e){
            System.out.println("Error\nUsage: java Simulator <config file> <sliceLength> <dispatch overhead>");
        }
    }
}
