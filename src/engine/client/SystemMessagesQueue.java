/************************************
 * Title: 	SystemMessageQueue
 * Date:	10.18.2012
 * Purpose: A thread carrying a queue
 * 			that keeps track of and 
 * 			handles system messages
 ************************************/

package engine.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JOptionPane;

import engine.MessageCodes;

public class SystemMessagesQueue extends Thread {

	private Queue<byte[]> messageQueue;
	private ByteBuffer messageBreaker;
	private boolean run;

	public SystemMessagesQueue() {
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
			messageBreaker = ByteBuffer.allocate(message.length - 1);
			messageBreaker.put(message, 1, message.length - 1);
			handleMessage(message[0]);

		}
	}

	private void handleMessage(byte code) {

		switch (code) {

		case MessageCodes.LOGIN_REPLY_REFRESH_PRIV: {
			ClientManager.id = messageBreaker.getInt(0);
			ClientManager.getLoginRefreshPriv(messageBreaker.array());
			break;
		}

		case MessageCodes.LOGIN_REPLY_REFRESH_PUBL: {
			ClientManager.getLoginRefreshPubl(messageBreaker.array());
			break;
		}

		case MessageCodes.LOGIN_REPLY_SUCC: {
			ClientManager.id = messageBreaker.getInt(0);
			ClientManager.sendUserListRequest();
			ClientManager.sendRoomListRequest();
			break;
		}

		case MessageCodes.LOGIN_REPLY_INFORM: {
			int id = messageBreaker.getInt(0);
			messageBreaker.position(4);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getLoginReplyInform(id, name);
			break;
		}

		case MessageCodes.LOGIN_REPLY_FAIL: {
			// TODO: change this
			JOptionPane.showMessageDialog(null, "Name already in use!");
			break;
		}

		case MessageCodes.USER_LIST_REPLY: {
			ClientManager.getUserListReply(messageBreaker.array());
			break;
		}

		case MessageCodes.ROOM_LIST_REPLY: {
			ClientManager.getRoomListReply(messageBreaker.array());
			break;
		}

		case MessageCodes.NEW_ROOM_REPLY_PRIV: {
			int roomNumber = messageBreaker.getInt(0);
			ClientManager.getNewRoomReplyPriv(roomNumber);
			break;
		}
		case MessageCodes.NEW_ROOM_REPLY_PUBL: {
			int roomNumber = messageBreaker.getInt(0);
			messageBreaker.position(4);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getNewRoomReplyPubl(roomNumber, name);
			break;
		}
		
		case MessageCodes.NEW_ROOM_REPLY_MC_PUBL: {
			int roomNumber = messageBreaker.getInt(0);
			messageBreaker.position(4);
			byte [] mcAddress = new byte [4];
			mcAddress[0] = messageBreaker.get();
			mcAddress[1] = messageBreaker.get();
			mcAddress[2] = messageBreaker.get();
			mcAddress[3] = messageBreaker.get();
			messageBreaker.position(8);
			int port = messageBreaker.getInt();
			messageBreaker.position(12);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getNewRoomReplyMCPubl(roomNumber, mcAddress, port, name);
			break;
		}

		case MessageCodes.NEW_ROOM_REPLY_INFORM: {
			int roomNumber = messageBreaker.getInt(0);
			messageBreaker.position(4);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getNewRoomReplyInform(roomNumber, name);
			break;
		}

		case MessageCodes.INVITE_USER_REPLY: {
			int roomNumber = messageBreaker.getInt(4);
			messageBreaker.position(8);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getInvitationReply(roomNumber, name);
			break;
		}
		
		case MessageCodes.INVITE_USER_MC_REPLY: {
			int roomNumber = messageBreaker.getInt(4);
			messageBreaker.position(8);
			byte [] address = new byte [4];
			address[0] = messageBreaker.get();
			address[1] = messageBreaker.get();
			address[2] = messageBreaker.get();
			address[3] = messageBreaker.get();
			messageBreaker.position(12);
			int port = messageBreaker.getInt();
			messageBreaker.position(16);
			String name = Charset.defaultCharset().decode(messageBreaker)
					.toString();
			ClientManager.getInvitationReply(roomNumber, name);
			ClientManager.registerInMulticast(roomNumber, address, port);
			break;
		}
		
		case MessageCodes.REGISTER_IN_MULTICAST: {
			int roomNumber = messageBreaker.getInt(0);
			messageBreaker.position(4);
			byte [] mcAddress = new byte [4];
			mcAddress[0] = messageBreaker.get();
			mcAddress[1] = messageBreaker.get();
			mcAddress[2] = messageBreaker.get();
			mcAddress[3] = messageBreaker.get();
			messageBreaker.position(9);
			int port = messageBreaker.getInt();
			ClientManager.registerInMulticast(roomNumber, mcAddress, port);
			break;
		}

		case MessageCodes.DISCONNECT_INFORM: {
			ClientManager.getDisconnectInform(messageBreaker.getInt(0));
			break;
		}

		case MessageCodes.CLOSE_ROOM_PRIV_INFORM:
		case MessageCodes.CLOSE_ROOM_PUBL_INFORM: {
			boolean priv = false;
			if ((code & 0x10) > 0)
				priv = true;

			int room = messageBreaker.getInt(0);
			ClientManager.getCloseRoomInform(room, priv);
			break;
		}

		case MessageCodes.FILE_TRANSFER_TCP_REPLY: {
			messageBreaker.position(8);
			ClientManager.getFileTransferReply(messageBreaker.getInt(0),
					messageBreaker.getInt(4),
					Charset.defaultCharset().decode(messageBreaker).toString());
			break;
		}
		
		case MessageCodes.FILE_TRANSFER_UDP_REPLY: {
			messageBreaker.position(16);
			ClientManager.getFileTransferUDPReply(messageBreaker.getInt(0),
					messageBreaker.getInt(4), messageBreaker.getLong(8),
					Charset.defaultCharset().decode(messageBreaker).toString());
			break;
		}
		
		case MessageCodes.FILE_TRANSFER_REPLY_DATA: {
			messageBreaker.rewind();
			byte temp = messageBreaker.get();
			int i = 1;
			while (temp != -1) {
				i++;
				temp = messageBreaker.get();
			}
			messageBreaker.position(messageBreaker.position() - i);
			byte[] nameBytes = new byte[i];
			messageBreaker.get(nameBytes);
			String fileName = new String(nameBytes,0,nameBytes.length-1);
			byte[] fileBytes = new byte[messageBreaker.remaining()];
			messageBreaker.get(fileBytes);
			ClientManager.getFileTransferData(fileName, fileBytes);
			break;
		}
		
		case MessageCodes.FILE_TRANSFER_UDP_REPLY_DATA: {
			long fileSize = messageBreaker.getLong(4);
			messageBreaker.position(12);
			byte temp = messageBreaker.get();
			int i = 1;
			while (temp != -1) {
				i++;
				temp = messageBreaker.get();
			}
			messageBreaker.position(messageBreaker.position() - i);
			byte[] nameBytes = new byte[i];
			messageBreaker.get(nameBytes);
			String fileName = new String(nameBytes,0,nameBytes.length-1);
			byte[] fileBytes = new byte[messageBreaker.remaining()];
			messageBreaker.get(fileBytes);
			ClientManager.getUDPFileTransferData(fileSize, fileName, fileBytes);
			break;
		}
		
		case MessageCodes.FILE_TRANSFER_ECHO: {
			JOptionPane.showMessageDialog(null, "Time taken to upload file: " + messageBreaker.getLong(0)/1000.0 + " s");
			break;
		}
		
		case MessageCodes.ROOM_MEMBERS_IDS_LIST_REPLY:
		{   
			messageBreaker.position(0);
			int room = messageBreaker.getInt();
			messageBreaker.position(4);
			byte[] info = new byte[messageBreaker.remaining()];
			messageBreaker.get(info);
			ClientManager.roomMembersIDList(room, info);
			break;
		}
		
		case MessageCodes.SEND_REMOVE_MEMBER_REPLY:
		{
			int roomID = messageBreaker.getInt(0);
			byte privateRoom = messageBreaker.get(4);
			ClientManager.userKicked(roomID, (privateRoom > 0) ? true : false);
			break;
		}
		
		case MessageCodes.SEND_MUTE_MEMBER_REPLY:{
			messageBreaker.position(0);
			int room  = messageBreaker.getInt();
			messageBreaker.position(4);
			boolean isPriv = (messageBreaker.get()==1)? true : false;
			ClientManager.addMutedRoom(room, isPriv);
			break;
		}
		
		case MessageCodes.SEND_UN_MUTE_MEMBER_REQUEST:{
			messageBreaker.position(0);
			int room  = messageBreaker.getInt();
			messageBreaker.position(4);
			boolean isPriv = (messageBreaker.get()==1)? true : false;
			ClientManager.RemoveMutedRoom(room, isPriv);
			break;
		}


		default:
			return;
		}
	}

	public void stopQueue() {
		run = false;
	}
}
