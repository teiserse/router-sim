import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

public class Wrapper {
	
	private static int MAX_ROUTERS = 15;
	private static int MAX_ENDPOINTS = 2;
	private static int CONTROLLER_PORT = 49999;
	
	Endpoint[] endpoints;
	Router[] routers;
	Controller control;
	PrintStream totalOutput;
	
	Wrapper(String filename){
		try {
			totalOutput = new PrintStream("wrapperout.txt");
			endpoints = new Endpoint[MAX_ENDPOINTS];
			routers = new Router[MAX_ROUTERS];
			Scanner configFile = new Scanner(new File(filename));
			Scanner currentFile = null;
			control = new Controller(CONTROLLER_PORT, this);
			
			while(configFile.hasNextLine()) {
				String currentSetup = configFile.nextLine();
				currentFile = new Scanner(new File(currentSetup));
				if(currentSetup.substring(0, 1).equals("e"))
					endpoints[Integer.parseInt(currentSetup.substring(1)) - 1]
							= new Endpoint(currentFile.nextLine(),this);
				else if(currentSetup.substring(0,1).equals("r"))
					routers[Integer.parseInt(currentSetup.substring(1)) - 1]
							= new Router(currentFile.nextLine(),this);
			}
			
			endpoints[0].work();
			
			if(currentFile != null)
				currentFile.close();
			configFile.close();
		} catch (Exception e) {
			System.out.println("File not found.");
			e.printStackTrace();
		}
	}
	
	synchronized void Operate() {
		try {
			while (true) {
				wait(25);
			}
		} catch (InterruptedException e) {
			System.out.print("This should never be seen.");
			e.printStackTrace();
		}
	}
	
	void print(String toPrint) {
		totalOutput.println(toPrint);
	}

	public static void main(String[] args) {
		new Wrapper("config").Operate();
	}
	
}
