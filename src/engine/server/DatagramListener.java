package engine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import engine.MessageCodes;

public class DatagramListener extends Thread{
	protected static DatagramSocket datagramSocket;
	protected static SystemMessagesQueue sysQueue; 
	
	public DatagramListener(SystemMessagesQueue sysQueue){
		DatagramListener.sysQueue = sysQueue;
		try {
			//TODO replace with server IP
			datagramSocket = new DatagramSocket(2312,
					InetAddress.getLocalHost());
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			byte[] buffPackets = new byte[60000];
			DatagramPacket packet = new DatagramPacket(buffPackets,
					buffPackets.length);
			try {
				datagramSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (packet.getData()[0] == MessageCodes.FILE_TRANSFER_UDP_ACCEPT) {
				byte[] hostAddress = packet.getAddress().getAddress();				
				int portNo = packet.getPort();
				byte[] usefulData = Arrays.copyOfRange(packet.getData(), 0, (1 + 4 + 4 + 8));
				//code : reciever - fileID - fileSize - portNo - hostNameBytes
				ByteBuffer data = ByteBuffer.allocate(usefulData.length + 4 + 4);
				data.put(usefulData);
				data.putInt(portNo);
				data.put(hostAddress);
				sysQueue.addMessage(data.array());
			} else {
				sysQueue.addMessage(packet.getData());
			}

		}
	}
	
}
