package engine.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer extends Thread {
	ServerSocket serverSocket;
	static int port = 1221;

	public ChatServer() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (BindException e) {
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				Socket client = serverSocket.accept();
				Client temp = new Client(client);
				temp.start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
