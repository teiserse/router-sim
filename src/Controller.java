import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Random;

public class Controller {
	
	ControllerSocket connection;
	Hashtable<String,ConnectionStruct> connectionMap;
	Hashtable<String,Hashtable<String,String>> paths;
	ArrayList<String> allKnownLocations;
	Random rng;
	Wrapper wrapper;
	
	Controller(int port, Wrapper wrapper) throws SocketException{
		connection = new ControllerSocket(port, this);
		connection.setDaemon(true);
		connection.start();
		connectionMap = new Hashtable<String,ConnectionStruct>();
		paths = new Hashtable<String,Hashtable<String,String>>();
		allKnownLocations = new ArrayList<String>();
		rng = new Random();
		this.wrapper = wrapper;
		printForMe("Initialised Controller.");
	}
	
	//Expected format for connections: ME:C1,C2,C3,C4!
	//Expected format for new inbound: ME@rest-of-actual-packet
	
	public void process(DatagramPacket packet) {
		String content = new String(packet.getData()).trim();
		String sender = content.substring(0, 2);
		if(!allKnownLocations.contains(sender))
			allKnownLocations.add(sender);
		if(content.charAt(2) == ':') {	//info packet
			printForMe("Received adjacency info from " + sender);
			ArrayList<String> links = new ArrayList<String>();
			for(int index = 3; content.charAt(index - 1) != '!'; index += 3) {
				if(!content.substring(index, index + 2).equals("CN")){
					links.add(content.substring(index, index + 2));
					if(!allKnownLocations.contains(content.substring(index, index + 2)))
						allKnownLocations.add(content.substring(index, index + 2));
				}
			}
			connectionMap.put(sender, new ConnectionStruct(packet.getPort(),links));
			calculatePaths();
		} else if(content.charAt(2) == '@') {
			int id = rng.nextInt(90) + 10;
			byte[] encoded = new byte[2];
			byte[] conv = String.valueOf(id).getBytes();
			encoded = conv;
			byte[] sendback = content.substring(3).getBytes();
			sendback[4] = encoded[0];
			sendback[5] = encoded[1];
			
			givePaths(content.substring(0, 2), content.substring(3, 5), id);
			printForMe("ID-less packet marked and routed, rebounding to router.");
			try {
				connection.send(new DatagramPacket(sendback, sendback.length)
						, new InetSocketAddress("localhost", packet.getPort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void calculatePaths() {
		for(Enumeration<String> keys = connectionMap.keys();keys.hasMoreElements();) {
			String currentKey = keys.nextElement();
			
			ArrayList<String> marked = new ArrayList<String>();
			Hashtable<String,String> edgeTo = new Hashtable<String,String>();
			PriorityQueue<String> bfsQueue = new PriorityQueue<String>();
			bfsQueue.add(currentKey);
			marked.add(currentKey);
			
			while(!bfsQueue.isEmpty()) {
				String curr = bfsQueue.poll();
				if(connectionMap.containsKey(curr))
					for(String adj : connectionMap.get(curr).connections) {
						if(!marked.contains(adj)) {
							bfsQueue.add(adj);
							marked.add(adj);
							edgeTo.put(adj, curr);
						}
					}
			}
			
			paths.put(currentKey, edgeTo);
			
		}
	}
	
	private void givePaths(String source, String dest, int id) {
		String previousRec = dest;
		String receiver = paths.get(source).get(dest);
		while(!previousRec.equals(source)) {
			
			if(connectionMap.containsKey(receiver)) {
				sendInstruction(receiver, id, previousRec);
			}
			
			previousRec = receiver;
			receiver = paths.get(source).get(previousRec);
		}
	}
	
	private void sendInstruction(String router, int id, String pathTo) {
		String content = "CN:" + String.valueOf(id) + "|" + pathTo;
		byte[] buffer = content.getBytes();
		try {
			connection.send(new DatagramPacket(buffer, buffer.length)
					, new InetSocketAddress("localhost", connectionMap.get(router).port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printForMe(String out) {
		if(wrapper == null)
			System.out.println("CN: " + out);
		else
			wrapper.print("CN: " + out);
	}
	
	private class ConnectionStruct{
		ArrayList<String> connections;
		int port;
		
		ConnectionStruct(int port, ArrayList<String> connections) {
			this.port = port;
			this.connections = connections;
		}
	}
	
	//public static void main(String[] args) {
	//}
}
