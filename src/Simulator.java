import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 * muhummad        15/03/19.
 */
public class Simulator {

    private KernelImp kernel = new KernelImp();

    public void setUp(String configFilename){
        try {
            Scanner infile = new Scanner(new FileReader(configFilename));

            while(infile.hasNext()){
                String line = infile.nextLine().trim();

                if(line.charAt(0) != '#') {
                    String[] data = line.split("\\s+");

                    if(data[0].equalsIgnoreCase("I/O")){
                        //TODO: make_device syscall to kernel
                        kernel.syscall(1, data[1], data[2]);
                    }else if(data[0].equalsIgnoreCase("PROGRAM")){
                        //TODO: create load program evt and insert into evt queue
                    }
                }
            }

        }catch(FileNotFoundException e){
            System.out.println(e.getMessage() + "\nError opening file.");
            System.exit(1);
        }

    }

    public Simulator(String[] args){
        //TODO: handle invalid args here
        String configFilename = args[0];
        double sliceLength = Double.parseDouble(args[1]);
        double dispatchOverhead = Double.parseDouble(args[2]);

        setUp(configFilename);
    }

    public static void main(String[] args) {
        new Simulator(args);
    }
}
