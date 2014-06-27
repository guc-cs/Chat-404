/************************************
 * Title: 	SystemMessageQueue
 * Date:	10.17.2012
 * Purpose: A thread carrying a queue
 * 			that keeps track of and 
 * 			handles system messages
 ************************************/

package engine.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;

import engine.MessageCodes;

public class SystemMessagesQueue extends Thread {

	private Queue<byte[]> messageQueue;
	private ByteBuffer messageBreaker;

	public SystemMessagesQueue() {
		messageQueue = new LinkedList<byte[]>();
	}

	public synchronized void addMessage(byte[] message) {
		messageQueue.add(message);
	}

	public void run() {
		while (true) {
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
		int sender = messageBreaker.getInt(0);
		boolean privateRoom = false;
		if ((0x10 & code) > 0)
			privateRoom = true;
		switch (code) {

		case MessageCodes.USER_LIST_REQUEST: {
			ServerManager.getUserList(sender);
			break;
		}

		case MessageCodes.ROOM_LIST_REQUEST: {
			ServerManager.getRoomList(sender);
			break;
		}

		case MessageCodes.NEW_ROOM_REQUEST_PRIV:
		case MessageCodes.NEW_ROOM_REQUEST_PUBL: {
			boolean multicast = messageBreaker.get(4) == -1 ? true : false;
			byte[] nameBytes = new byte[messageBreaker.capacity() - 5];
			messageBreaker.position(5);
			messageBreaker.get(nameBytes, 0, nameBytes.length);
			Charset charset = Charset.defaultCharset();
			String roomName = charset.decode(ByteBuffer.wrap(nameBytes))
					.toString();
			ServerManager.createRoom(sender, roomName, privateRoom, multicast);
			break;
		}

		case MessageCodes.INIVITE_USER_REQUEST: {
			int invited = messageBreaker.getInt(4);
			int roomNumber = messageBreaker.getInt(8);
			ServerManager.inviteUser(sender, invited, roomNumber);
			break;
		}

		case MessageCodes.INIVITE_USER_MC_REQUEST: {
			int invited = messageBreaker.getInt(4);
			int roomNumber = messageBreaker.getInt(8);
			messageBreaker.position(12);
			byte[] address = new byte[4];
			address[0] = messageBreaker.get();
			address[1] = messageBreaker.get();
			address[2] = messageBreaker.get();
			address[3] = messageBreaker.get();
			messageBreaker.position(16);
			int port = messageBreaker.getInt();
			ServerManager.inviteMCUser(sender, invited, roomNumber, address,
					port);
			break;
		}

		case MessageCodes.JOIN_PUBLIC_ROOM: {
			int roomNumber = messageBreaker.getInt(4);
			ServerManager.joinRoom(sender, roomNumber);
			break;
		}

		case MessageCodes.DISCONNECT_REQUEST:
			ServerManager.removeClient(sender);
			break;

		case MessageCodes.CLOSE_ROOM_PUBL_REQUEST:
		case MessageCodes.CLOSE_ROOM_PRIV_REQUEST: {
			int roomNumber = messageBreaker.getInt(4);
			ServerManager.closeRoom(roomNumber, privateRoom);
			break;
		}

		case MessageCodes.FILE_TRANSFER_TCP_REQUEST: {
			long timeStamp = messageBreaker.getLong(4);
			int reciever = messageBreaker.getInt(12);
			messageBreaker.position(16);
			byte temp = messageBreaker.get();
			int i = 1;
			while (temp != -1) {
				i++;
				temp = messageBreaker.get();
			}
			messageBreaker.position(messageBreaker.position() - i);
			byte[] nameBytes = new byte[i];
			messageBreaker.get(nameBytes);
			String fileName = new String(nameBytes, 0, nameBytes.length - 1);
			byte[] fileBytes = new byte[messageBreaker.remaining()];
			messageBreaker.get(fileBytes);

			ServerManager.getFileTransferRequest(sender, reciever, timeStamp,
					fileName, fileBytes);
			break;
		}

		case MessageCodes.FILE_TRANSFER_UDP_REQUEST: {
			int reciever = messageBreaker.getInt(4);
			long fileSize = messageBreaker.getLong(8);
			messageBreaker.position(16);
			byte temp = messageBreaker.get();
			int i = 1;
			while (temp != -1) {
				i++;
				temp = messageBreaker.get();
			}
			messageBreaker.position(messageBreaker.position() - i);
			byte[] nameBytes = new byte[i];
			messageBreaker.get(nameBytes);
			String fileName = new String(nameBytes, 0, nameBytes.length - 1);
			byte[] fileBytes = new byte[messageBreaker.remaining()];
			messageBreaker.get(fileBytes);
			ServerManager.getFileTransferUDPRequest(sender, reciever, fileName,
					fileSize, fileBytes);
			break;
		}

		case MessageCodes.FILE_TRANSFER_TCP_ACCEPT: {
			ServerManager.getFileTransferTCPReply(sender,
					messageBreaker.getInt(4), true);
			break;
		}

		case MessageCodes.FILE_TRANSFER_UDP_ACCEPT: {
			int fileID = messageBreaker.getInt(4);
			long fileSize = messageBreaker.getLong(8);
			int port = messageBreaker.getInt(16);
			messageBreaker.position(20);
			byte[] hostAddress = { messageBreaker.get(), messageBreaker.get(),
					messageBreaker.get(), messageBreaker.get() };
			ServerManager.getFileTransferUDPReply(fileID, fileSize, port,
					hostAddress);
			break;
		}

		case MessageCodes.FILE_TRANSFER_REFUSE: {
			ServerManager.getFileTransferTCPReply(sender,
					messageBreaker.getInt(4), false);
			break;
		}

		// TODO complete the switch block for this Message code taken for the
		// message code
		// TODO should I remove the byte
		// TODO should I move the message breaker position before each get
		// statement
		case MessageCodes.ROOM_MEMBERS_IDS_LIST_REQUEST: {
			messageBreaker.position(4);
			int room = messageBreaker.getInt();
			messageBreaker.position(8);
			boolean isPriv;
			/*
			 * if(messageBreaker.get(infoBytes, 7, 7).getInt()== -1){
			 * if(messageBreaker.get(infoBytes, 8, 8).getInt()== 1){ isPriv =
			 * true; } if(messageBreaker.get(infoBytes, 8, 8).getInt()== 0){
			 * isPriv = false; } }
			 */

			isPriv = (messageBreaker.get() == 1) ? true : false;

			ServerManager.getMembersList(sender, room, isPriv);
			break;
		}

		case MessageCodes.SEND_REMOVE_MEMBER_REQUEST: {
			messageBreaker.position(4);
			int room = messageBreaker.getInt();
			messageBreaker.position(8);
			boolean isPriv;
			isPriv = (messageBreaker.get() == 1) ? true : false;
			messageBreaker.position(9);
			int member = messageBreaker.getInt();
			ServerManager.RemoveRoomMember(sender, room, isPriv, member);
			break;
		}
		case MessageCodes.SEND_MUTE_MEMBER_REQUEST: {
			messageBreaker.position(4);
			int room = messageBreaker.getInt();
			messageBreaker.position(8);
			boolean isPriv;
			isPriv = (messageBreaker.get() == 1) ? true : false;
			messageBreaker.position(9);
			int member = messageBreaker.getInt();
			ServerManager.MuteRoomMemberReply(sender, room, isPriv, member);
			break;
		}

		case MessageCodes.SEND_UN_MUTE_MEMBER_REQUEST: {
			messageBreaker.position(4);
			int room = messageBreaker.getInt();
			messageBreaker.position(8);
			boolean isPriv;
			isPriv = (messageBreaker.get() == 1) ? true : false;
			messageBreaker.position(9);
			int member = messageBreaker.getInt();
			ServerManager.UnMuteRoomMemberReply(sender, room, isPriv, member);
			break;
		}

		default:
			return;
		}
	}
}
