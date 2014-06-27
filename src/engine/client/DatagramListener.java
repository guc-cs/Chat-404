package engine.client;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DatagramListener extends Thread{
	protected static DatagramSocket datagramSocket;
	protected static SystemMessagesQueue sysQueue; 
	private boolean run;
	
	public DatagramListener(SystemMessagesQueue sysQueue){
		DatagramListener.sysQueue = sysQueue;
		while(true){
		try {
			datagramSocket = new DatagramSocket();
			break;
		}catch (BindException e){
			e.printStackTrace();
		}catch (SocketException e) {
			e.printStackTrace();
		}
		}
	}
	
	public void run() {
		run = true;
		while (run) {
			byte[] buffPackets = new byte[60000];
			DatagramPacket packet = new DatagramPacket(buffPackets,
					buffPackets.length);
			try {
				datagramSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sysQueue.addMessage(packet.getData());
		}
	}
	
	public void stopListener(){
		run = false;
		datagramSocket.close();
	}
	
}
