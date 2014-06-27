/************************************
 * Title: 	HoldingCell
 * Date:	10.20.2012
 * Purpose: Keeps track of clients
 * 			that were cutoff without
 * 			sending proper disconnect
 * 			request, reinstates them
 * 			if they reconnect withen
 * 			1 minute
 ************************************/

package engine.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HoldingCell {

	private ArrayList<Client> inmates;
	private Timer timer;
	private static final int sentence = 60000;

	public HoldingCell() {
		inmates = new ArrayList<Client>();
		timer = new Timer();
	}

	public synchronized void addInmate(Client c) {
		// TODO: make sure this works
		inmates.add(c);
		timer.schedule(new Excutioner(c), sentence);
	}

	public synchronized boolean reinstate(Socket s, String name) {
		for (Client inmate : inmates) {
			if (inmate.getSocket().getInetAddress().toString()
					.equals(s.getInetAddress().toString())
					&& inmate.getUserName().equals(name)) {
				inmates.remove(inmate);
				inmate.reinstate(s);
				return true;
			}
		}

		return false;
	}

	// ==================Inner Classes=================
	private class Excutioner extends TimerTask {
		private Client inmate;

		public Excutioner(Client inmate) {
			this.inmate = inmate;
		}

		public void run() {
			if (!inmates.contains(inmate))
				return;
			inmates.remove(inmate);
			ServerManager.removeClient(inmate.getUserId());
		}
	}
	// =================================================

}
