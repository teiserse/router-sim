import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

public class ActiveSocket extends Thread{
	
	static final int PACKETSIZE = 65536;
	
	DatagramSocket socket;
	InetSocketAddress destination;
	String id;
	NetworkElement holder;
	
	ActiveSocket(int port, InetSocketAddress destination, String id, NetworkElement holder) throws SocketException{
		socket = new DatagramSocket(port);
		this.destination = destination;
		this.id = id;
		this.holder = holder;
	}
	
	public void run(){
		while (true) {
			try {
			DatagramPacket received = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
			socket.receive(received);
			holder.process(received, id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send(DatagramPacket toSend) throws IOException {
		toSend.setSocketAddress(destination);
		socket.send(toSend);
	}
	
	public static Hashtable<String,ActiveSocket> SetupRouter(String config, NetworkElement holder) throws SocketException {
		Hashtable<String,ActiveSocket> sockets = new Hashtable<String,ActiveSocket>(Router.MAX_CONNECTIONS); 
		int readFrom = config.indexOf(":");
		
		for(int index = 0; index < Router.MAX_CONNECTIONS
				&& config.charAt(config.indexOf(",", readFrom) + 1) != '!'; index++) {
			String newId = config.substring(readFrom + 1, config.indexOf("-", readFrom));
			readFrom = config.indexOf("-", readFrom) + 1;
			int ownPort = Integer.parseInt(config.substring(readFrom, config.indexOf("-", readFrom)));
			readFrom = config.indexOf("-", readFrom) + 1;
			int dstPort = Integer.parseInt(config.substring(readFrom, config.indexOf(",", readFrom)));
			readFrom = config.indexOf(",", readFrom);
			
			sockets.put(newId, new ActiveSocket(ownPort,new InetSocketAddress("localhost",dstPort),newId,holder));
			sockets.get(newId).setDaemon(true);
			sockets.get(newId).start();
		}
		
		return sockets;
	}
	
	public static ActiveSocket SetupEndpoint(String config, NetworkElement holder) throws SocketException {
		ActiveSocket socket; 
		int readFrom = config.indexOf(":");
		
		String newId = config.substring(readFrom, config.indexOf("-", readFrom));
		readFrom = config.indexOf("-", readFrom) + 1;
		int ownPort = Integer.parseInt(config.substring(readFrom, config.indexOf("-", readFrom)));
		readFrom = config.indexOf("-", readFrom) + 1;
		int dstPort = Integer.parseInt(config.substring(readFrom, config.indexOf(",", readFrom)));
		readFrom = config.indexOf(",", readFrom) + 1;
		
		socket = new ActiveSocket(ownPort,new InetSocketAddress("localhost",dstPort),newId,holder);
		socket.setDaemon(true);
		socket.start();
		
		return socket;
	}
	
}
