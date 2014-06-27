/************************************
 * Title: 	Client
 * Date:	10.16.2012
 * Purpose: A thread that handles 
 * 			communication with remote 
 * 			Client
 ************************************/

package engine.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;

import engine.MessageCodes;

public class Client extends Thread {

	private int id;
	private String userName;
	private DataInputStream inStream;
	private DataOutputStream outStream;
	private Socket socket;
	private boolean open;
	private boolean doingTime; // Is in the holdingCell for logging off without
								// sending proper
								// disconnect request

	public Client(Socket socket) {
		open = true;
		doingTime = false;
		try {
			this.socket = socket;
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void run() {
		try {
			byte[] loginMessage = new byte[inStream.readInt()];
			int rawSize = inStream.readInt();
			inStream.read(loginMessage);
			loginMessage = ServerManager.encryptionLayer.decrypt(loginMessage);
			if (rawSize != -1) {
				ServerManager.compressionLayer
						.decompress(loginMessage, rawSize);
			}
			Charset charset = Charset.defaultCharset();
			ByteBuffer userNameBuffer = ByteBuffer
					.allocate(loginMessage.length - 1);
			userNameBuffer.put(loginMessage, 1, loginMessage.length - 1);
			userNameBuffer.rewind();
			userName = charset.decode(userNameBuffer).toString();
			if (ServerManager.holdingCell.reinstate(socket, userName))
				return;

			ServerManager.createClient(this);
		} catch (DataFormatException e) {
			System.out.println("Client " + socket.getInetAddress()
					+ " : messag corrupted!");
		} catch (IOException e) {
			System.out.println(e);
		}

		while (open) {
			try {
				if (doingTime) {
					sleep(1000);
					continue;
				}
				byte[] message = new byte[inStream.readInt()];
				int rawSize = inStream.readInt();
				int readSoFar = 0;
				while (readSoFar < message.length)
					readSoFar += inStream.read(message, readSoFar,
							message.length - readSoFar);
				message = ServerManager.encryptionLayer.decrypt(message);
				if (rawSize != -1) {
					message = ServerManager.compressionLayer.decompress(
							message, rawSize);
				}
				if (message[0] == MessageCodes.TEXT_MESSAGE_PRIV
						|| message[0] == MessageCodes.TEXT_MESSAGE_PUBL)
					ServerManager.textQueue.addMessage(message);
				else
					ServerManager.sysQueue.addMessage(message);

				if (message[0] == MessageCodes.DISCONNECT_REQUEST)
					open = false;

			} catch (IOException e) {
				System.out.println("Host " + socket.getInetAddress()
						+ " was cutoff, going to holding cell!");
				ServerManager.holdingCell.addInmate(this);
				doingTime = true;
				System.out.println(e);
			} catch (InterruptedException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public void sendMessage(byte[] message) {
		try {
			if (doingTime)
				return;
			int rawSize = -1;
			int encryptedSize;
			if (rawSize >= 28) {
				message = ServerManager.compressionLayer.compress(message);
				rawSize = message.length;
			}
			message = ServerManager.encryptionLayer.encrypt(message);
			encryptedSize = message.length;
			outStream.writeInt(encryptedSize);
			outStream.writeInt(rawSize);
			outStream.write(message);
		} catch (IOException e) {
			System.out.println("Host " + socket.getInetAddress()
					+ " was cutoff, going to holding cell!");
			ServerManager.holdingCell.addInmate(this);
			doingTime = true;
		}
	}

	public String getUserName() {
		return userName;
	}

	public int getUserId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Socket getSocket() {
		return socket;
	}

	public void reinstate(Socket socket) {
		try {
			this.socket = socket;
			inStream = new DataInputStream(socket.getInputStream());
			outStream = new DataOutputStream(socket.getOutputStream());
			doingTime = false;
			ServerManager.getPrivateRoomListOnRefresh(id);
			ServerManager.getUserList(id);
			ServerManager.getRoomList(id);
			ServerManager.getPublicRoomListOnRefresh(id);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void close() {
		try {
			open = false;
			inStream.close();
			outStream.close();
			socket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
