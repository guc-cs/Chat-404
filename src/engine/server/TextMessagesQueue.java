/************************************
 * Title: 	TextMessageQueue
 * Date:	10.17.2012
 * Purpose: A thread carrying a queue
 * 			that keeps track of and 
 * 			handles text messages
 ************************************/

package engine.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TextMessagesQueue extends Thread{
	
	private Queue<byte []> messageQueue;
	private ByteBuffer messageBreaker;
	
	public TextMessagesQueue()
	{
		messageQueue = new LinkedList<byte[]>();
	}
	
	public synchronized void addMessage(byte [] message)
	{
		messageQueue.add(message);
	}
	
	public void run()
	{
		while(true)
		{
			//Might cause synchronization problem
			if (messageQueue.isEmpty())
			{
				try
				{
					Thread.sleep(500);
					continue;
				}catch(InterruptedException e)
				{
					System.out.println(e);
				}
			}
			
			byte [] message;
			synchronized (messageQueue){
			message = messageQueue.remove();
			}
			messageBreaker = ByteBuffer.allocate(message.length);
			messageBreaker.put(message);
			handleMessage(message[0]);
			
		}
	}
	
	private void handleMessage(byte code)
	{
		int room = messageBreaker.getInt(1);
		int sender = messageBreaker.getInt(5);
		boolean privateRoom = false;
		if ((code & 0x10) > 0)
			privateRoom = true;
		ArrayList<Integer> roomClients = ServerManager.getRoomClients(room, privateRoom);
		for (int c : roomClients)
		{
			if (c == sender)
				continue;
			ServerManager.clients.get(c).sendMessage(messageBreaker.array());
		}
	}
	
}
