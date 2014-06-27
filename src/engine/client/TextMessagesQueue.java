/************************************
 * Title: 	TextMessageQueue
 * Date:	10.18.2012
 * Purpose: A thread carrying a queue
 * 			that keeps track of and 
 * 			handles text messages
 ************************************/

package engine.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

public class TextMessagesQueue extends Thread {
	private Queue<byte[]> messageQueue;
	private ByteBuffer messageBreaker;
	private boolean run;

	public TextMessagesQueue() {
		messageQueue = new LinkedList<byte[]>();
	}

	public synchronized void addMessage(byte[] message) {
		messageQueue.add(message);
	}

	public void run() {
		run = true;
		while (run) {
			// Might cause synchronization problem
			if (messageQueue.isEmpty()) {
				try {
					Thread.sleep(500);
					continue;
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			byte[] message;
			synchronized (messageQueue) {
				message = messageQueue.remove();
			}
			messageBreaker = ByteBuffer.allocate(message.length);
			messageBreaker.put(message);
			handleMessage(message[0]);
		}

	}

	private void handleMessage(byte code) {
		boolean isPublic = (code == 0) ? true : false;
		int roomNumber = messageBreaker.getInt(1);
		int senderID = messageBreaker.getInt(5);
		messageBreaker.position(8);
		Charset charset = Charset.defaultCharset();
		ClientManager.displayMessage(isPublic, roomNumber, senderID, charset
				.decode(messageBreaker).toString());
	}

	public void stopQueue() {
		run = false;
	}
}
