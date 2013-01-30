import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class DWCLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
	
		// command line arguments
		Options cmdoptions = new Options();
		
		cmdoptions.addOption("help", false, "display command line argument help");
		
		CommandLineParser parser = new GnuParser();
		try 
		{
			CommandLine line = parser.parse( cmdoptions, args );
		    
			// help
			if (line.hasOption("help"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar DWCLI.jar [OPTIONS]", cmdoptions );
				System.exit(0);
			}
			
		
		}
		catch( ParseException exp ) 
		{
		    System.err.println( "Could not parse command line: " + exp.getMessage() );
		    System.exit(-1);
		}
		
		
	}

}
