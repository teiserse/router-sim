import java.net.DatagramPacket;

public interface NetworkElement {
	
	public void process(DatagramPacket packet, String id);
	
}
