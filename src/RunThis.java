import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class RunThis {

    @Parameter(names = "-debug", description = "Debug mode")
    private boolean debug = false;

    @Parameter(names = "-help", help = true)
    private boolean help;

    public static void main(String[] args) {
        RunThis runThis = new RunThis();

        JCommander jCommander = new JCommander(runThis);

        jCommander.parse(args);

        if(args.length == 0) {

           jCommander.usage();

        }

        System.out.println(runThis.help);


    }



}



