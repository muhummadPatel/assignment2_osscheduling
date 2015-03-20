import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * muhummad        15/03/19.
 */
public class Simulator {

    static KernelImp kernel = new KernelImp();
    static SystemTimerImp timer = new SystemTimerImp();

    static EventQueue eventQueue = new EventQueue();

    public static void setUp(String configFilename){
        try {
            Scanner infile = new Scanner(new FileReader(configFilename));

            while(infile.hasNext()){
                String line = infile.nextLine().trim();

                if(line.charAt(0) != '#') {
                    String[] data = line.split("\\s+");

                    if(data[0].equalsIgnoreCase("I/O")){
                        //MAKE_DEVICE system call to kernel
                        kernel.syscall(SystemCall.MAKE_DEVICE, data[1], data[2]);
                    }else if(data[0].equalsIgnoreCase("PROGRAM")){
                        //create load program evt and insert into evt queue
                        eventQueue.add(new ExecveEvent(Long.parseLong(data[1]), data[2]));
                    }
                }
            }

            timer.setSystemTime(0);

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError opening file.");
            System.exit(1);
        }
        System.out.println("completed " + timer.getSystemTime());
    }

    public static void main(String[] args) {
        //TODO: handle invalid args here
        String configFilename = args[0];
        double sliceLength = Double.parseDouble(args[1]);
        double dispatchOverhead = Double.parseDouble(args[2]);

        setUp(configFilename);
    }
}
