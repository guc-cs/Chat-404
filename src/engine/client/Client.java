/****************************************
 * Title: 	Client
 * Date:	10.18.2012
 * Purpose: Handles remote communication
 * 			with the server
 *****************************************/

package engine.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.swing.JOptionPane;

public class Client extends Thread {

	private DataOutputStream outStream;
	private boolean listen;
	private DataInputStream inStream;

	public Client() {
		try {
			outStream = new DataOutputStream(
					ClientManager.localSocket.getOutputStream());
			listen = true;
			inStream = new DataInputStream(
					ClientManager.localSocket.getInputStream());
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	public void run() {
		while (listen) {
			try {
				int encryptedSize = inStream.readInt();
				int rawSize = inStream.readInt();
				byte[] message = new byte[encryptedSize];
				int readSoFar = 0;
				while (readSoFar < encryptedSize)
					readSoFar += inStream.read(message, readSoFar,
							message.length - readSoFar);
				message = ClientManager.encryptionLayer.decrypt(message);
				if (rawSize != -1) {
					message = ClientManager.compressionLayer.decompress(
							message, rawSize);
				}
				if (message[0] == 0x10 || message[0] == 0x00)
					ClientManager.textQueue.addMessage(message);
				else
					ClientManager.sysQueue.addMessage(message);
			} catch (EOFException e) {
				System.out.println("User " + ClientManager.userName
						+ " is logging off");
				break;
			} catch (DataFormatException e) {
				System.out.println("Server message corrupted!");
			} catch (IOException e) {
				System.out.println(e);
				if (!listen)
					return;
				listen = false;
				JOptionPane.showMessageDialog(null, "Server is down!");
				ClientManager.logoff();
			}

		}
	}

	public boolean sendMessage(byte[] message) {
		try {
			int rawSize = -1;
			int encryptedSize;
			if (rawSize >= 28) {
				message = ClientManager.compressionLayer.compress(message);
				rawSize = message.length;
			}
			message = ClientManager.encryptionLayer.encrypt(message);
			encryptedSize = message.length;
			outStream.writeInt(encryptedSize);
			outStream.writeInt(rawSize);
			outStream.write(message);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}

	}

	public void close() throws IOException {
		listen = false;
		outStream.close();
		inStream.close();
	}

}
