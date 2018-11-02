import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;


public class Router implements NetworkElement{
	
	public static final int MAX_CONNECTIONS = 5;
	
	Hashtable<String,ActiveSocket> connections;
	Hashtable<Integer,String> routes;
	public String myid;
	Wrapper wrapper;
	
	Router(String configInfo, Wrapper wrapper){
		myid = configInfo.substring(0, configInfo.indexOf(":"));
		this.wrapper = wrapper;
		routes = new Hashtable<Integer,String>();
		try {
			connections = ActiveSocket.SetupRouter(configInfo, this);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		StringBuilder getinfo = new StringBuilder(myid + ":");
		for(Enumeration<String> keys = connections.keys();keys.hasMoreElements();){
			String connectid = keys.nextElement();
			getinfo.append(connectid); 
			getinfo.append(keys.hasMoreElements()?",":"!");
		}
		String controllerInfo = getinfo.toString();
		try {
			connections.get("CN").send(new DatagramPacket(controllerInfo.getBytes(), controllerInfo.length()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	synchronized void work() {
		try {
			
		this.wait();
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void process(DatagramPacket packet, String id) {
		byte[] content = packet.getData();
		String received = new String(content).trim();
		if(received.substring(0, 2).equals("CN")) {
			//CN:id|to
			//01234567
			int packetid = Integer.parseInt(received.substring(3, 5));
			String routeto = received.substring(6);
			routes.put(packetid, routeto);
		}
		else {
		//String source = received.substring(2, 4);
		String dest = received.substring(0, 2);
		int packetid = Integer.parseInt(received.substring(4, 6));
		//String message = ("Received: " + received.substring(6, received.length())
		//	+ ", Src: " + source + ", Dst: " + dest + ", ID: " + packetid);
		//printForMe(message);
		DatagramPacket sending = new DatagramPacket(packet.getData(), packet.getLength());
		try {
			
			if(connections.containsKey(dest)) {
				printForMe("Sending packet " + packetid + " to local link " + dest);
				connections.get(dest).send(sending);
			}
			else if(packetid == 0) {
				printForMe("ID-less packet, consulting Controller");
				sending = new DatagramPacket((myid + "@" + received).getBytes(), packet.getLength() + 3);
				connections.get("CN").send(sending);
			}
			else if(routes.containsKey(packetid)) {
				printForMe("Routing packet " + packetid + " to " + routes.get(packetid));
				connections.get(routes.get(packetid)).send(sending);
				routes.remove(packetid);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}}	
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
		
		new Router(config).work();
		
		givename.close();
		file.close();
	}
	*/
	private void printForMe(String out) {
		if(wrapper == null)
			System.out.println(myid + ": " + out);
		else
			wrapper.print(myid + ": " + out);
	}
	
	// for now just focus on trying to get two endpoints to connect to eachother with one router.
	// have two sockets, one connecting to each endpoint
	// the start of the packet data will have the endpoint to send to
	// implement a basic Runnable/Thread subclass that looks over one socket
	// create functions that send to preset destinations so that no direct target can be stated
	//			Like the .send() of a packet, but with a fixed destination per port
	//			(this may require a secondary array of some sort of address class - figure out how they work)
}
