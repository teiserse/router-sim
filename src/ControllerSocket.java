import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

class ControllerSocket extends Thread{
		static final int PACKETSIZE = 65536;
		DatagramSocket socket;
		Controller holder;
		
		ControllerSocket(int port, Controller holder){
			try {
				socket = new DatagramSocket(port);
				this.holder = holder;
			} catch (SocketException e) {
				e.printStackTrace();
			}
			
		}
		
		public void run(){
			while (true) {
				try {
				DatagramPacket received = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
				socket.receive(received);
				holder.process(received);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void send(DatagramPacket toSend, InetSocketAddress destination) throws IOException {
			toSend.setSocketAddress(destination);
			socket.send(toSend);
		}
	}
