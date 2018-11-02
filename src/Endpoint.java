import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Random;

public class Endpoint implements NetworkElement{
	
	private static final int DELAY_TIMING = 10000;
	private static final int DELAY_RANGE = 5000;
	
	ActiveSocket connection;
	String id;
	String sendTo;
	Random rng;
	Wrapper wrapper;
	//Scanner input;
	
	Endpoint(String configInfo, Wrapper wrapper){
		id = configInfo.substring(0, configInfo.indexOf(":"));
		sendTo = (id.equals("E1")?"E2":"E1");
		try {
			connection = ActiveSocket.SetupEndpoint(configInfo, this);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		rng = new Random();
		this.wrapper = wrapper;
		
		// TODO - Some sort of header that denotes a target endpoint
		//			two bytes - one clear, one destination name
		//			the clear will be used later as a connection ID
		//			Don't worry about it being outside of the controller
		//			Assume that it already knows the system's name for the target
		//			Akin to using a DNS server
	}

	synchronized void work() {
		
		printForMe("Starting operations");
		
		try {
			String lastInput = "";
			while (true) {
				int delay = DELAY_TIMING + rng.nextInt(DELAY_RANGE);
				lastInput = String.valueOf(delay);
				wait(delay);
				printForMe("Sending: " + lastInput + ", Dst: " + sendTo);
			
				byte buffer[] = new byte[6 + lastInput.length()];
				System.arraycopy(sendTo.getBytes(), 0, buffer, 0, 2);
				System.arraycopy(id.getBytes(), 0, buffer, 2, 2);
				System.arraycopy("00".getBytes(), 0, buffer, 4, 2);
				System.arraycopy(lastInput.getBytes(), 0, buffer, 6, lastInput.length());
			
				connection.send(new DatagramPacket(buffer, buffer.length));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void process(DatagramPacket packet, String id) {
		byte[] content = packet.getData();
		String message = new String(content).trim();
		printForMe("Received: " + message.substring(6, message.length()) + ", Src: " + message.substring(2, 4));
	}
	/*
	public static void main(String[] args) {
		Scanner givename = new Scanner(System.in);
		String filename = givename.nextLine();
		Scanner file = null;
		try {
			file = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String config = file.next();

		new Endpoint(config).work();

		givename.close();
		file.close();
	}
	*/
	private void printForMe(String out) {
		if(wrapper == null)
			System.out.println(id + ": " + out);
		else
			wrapper.print(id + ": " + out);
	}
	
}
