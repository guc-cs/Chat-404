/************************************
 * Title: 	ClientManager
 * Date:	10.18.2012
 * Purpose: Collection of static
 * 			methods and variables 
 * 			that provide the core
 * 			engine of the client
 ************************************/

package engine.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;

import ui.DisplayBox;
import ui.GUIManager;
import engine.CompressionLayer;
import engine.EncryptionLayer;
import engine.FileParser;
import engine.FixedIndexArray;
import engine.MessageCodes;

public class ClientManager {

	protected static final String IP = "localhost";
	protected static final int port = 1221;
	protected static Socket localSocket;
	protected static DatagramPacket datagramPacket;
	protected static TextMessagesQueue textQueue;
	protected static SystemMessagesQueue sysQueue;
	protected static Client client;
	protected static ByteBuffer messageWrapper;
	protected static String[] userNames;
	protected static String[] publicRoomNames;
	protected static MulticastSocket[] multicastSockets;
	protected static ArrayList<PrivateRoom> privateRooms;
	protected static EncryptionLayer encryptionLayer;
	protected static String userName;
	protected static int id;
	protected static CompressionLayer compressionLayer;
	protected static FixedIndexArray<FileParser> tempFiles;
	protected static DatagramListener datagramListener;
	protected static ArrayList<Integer> MutedPublicRooms;
	protected static ArrayList<Integer> MutedPrivateRooms;

	public static void main(String... args) {
		init();
	}

	public static void init() {
		encryptionLayer = new EncryptionLayer();
		compressionLayer = new CompressionLayer();
	}

	public synchronized static boolean connect(String name) {
		try {
			userName = name;
			localSocket = new Socket(IP, port);
			client = new Client();
			tempFiles = new FixedIndexArray<FileParser>(10);
			textQueue = new TextMessagesQueue();
			sysQueue = new SystemMessagesQueue();
			datagramListener = new DatagramListener(sysQueue);
			textQueue.start();
			sysQueue.start();
			client.start();
			datagramListener.start();
			MutedPrivateRooms = new ArrayList<Integer>();
			MutedPublicRooms = new ArrayList<Integer>();

			byte[] nameBytes = name.getBytes();
			messageWrapper = ByteBuffer.allocate(1 + nameBytes.length);
			messageWrapper.put(MessageCodes.LOGIN_REQUEST);
			messageWrapper.put(nameBytes);
			return client.sendMessage(messageWrapper.array());

		} catch (IOException e) {
			System.out.println(e);

			return false;
		}
	}

	public synchronized static boolean sendMessage(String message, int room,
			boolean priv) {
		if (priv) {
			if (MutedPrivateRooms.contains(new Integer(room))) {
				JOptionPane.showMessageDialog(null,
						"you are muted in this room");
				return false;
			}
		} else {
			if (MutedPublicRooms.contains(new Integer(room))) {
				JOptionPane.showMessageDialog(null,
						"you are muted in this room");
				return false;
			}
		}
		byte[] messageBytes = message.getBytes();
		messageWrapper = ByteBuffer.allocate(9 + messageBytes.length);
		messageWrapper.put((priv) ? MessageCodes.TEXT_MESSAGE_PRIV
				: MessageCodes.TEXT_MESSAGE_PUBL);
		messageWrapper.putInt(room);
		messageWrapper.putInt(id);
		messageWrapper.put(messageBytes);
		return client.sendMessage(messageWrapper.array());
	}

	public synchronized static boolean sendUserListRequest() {
		messageWrapper = ByteBuffer.allocate(5);
		messageWrapper.put(MessageCodes.USER_LIST_REQUEST);
		messageWrapper.putInt(id);
		return client.sendMessage(messageWrapper.array());
	}

	public synchronized static void getUserListReply(byte[] list) {
		messageWrapper = ByteBuffer.wrap(list);
		int users = messageWrapper.getInt();
		userNames = new String[users];
		Charset charset = Charset.defaultCharset();
		while (users-- > 0) {
			int userID = messageWrapper.getInt();

			byte tempByte = messageWrapper.get();
			int i = 1;
			while (tempByte != -1) {
				i++;
				tempByte = messageWrapper.get();
			}
			messageWrapper.position(messageWrapper.position() - i);
			byte[] name = new byte[i];
			messageWrapper.get(name);
			ByteBuffer tempBuffer = ByteBuffer.wrap(name, 0, name.length - 1);
			if (userNames.length <= userID) {
				String[] temp = new String[userID + 10];
				for (int j = 0; j < userNames.length; j++)
					temp[j] = userNames[j];
				userNames = temp;

			}
			userNames[userID] = charset.decode(tempBuffer).toString();

		}
		GUIManager.showUsers(userNames);
	}

	public synchronized static boolean sendRoomListRequest() {
		messageWrapper = ByteBuffer.allocate(5);
		messageWrapper.put(MessageCodes.ROOM_LIST_REQUEST);
		messageWrapper.putInt(id);
		return client.sendMessage(messageWrapper.array());
	}

	public synchronized static void getRoomListReply(byte[] list) {
		messageWrapper = ByteBuffer.wrap(list);
		int rooms = messageWrapper.getInt();
		publicRoomNames = new String[rooms];
		multicastSockets = new MulticastSocket[rooms];
		Charset charset = Charset.defaultCharset();
		while (rooms-- > 0) {
			int roomID = messageWrapper.getInt();
			byte tempByte = messageWrapper.get();
			int i = 1;
			while (tempByte != -1) {
				i++;
				tempByte = messageWrapper.get();
			}
			messageWrapper.position(messageWrapper.position() - i);
			byte[] name = new byte[i];
			messageWrapper.get(name);
			ByteBuffer tempBuffer = ByteBuffer.wrap(name, 0, name.length - 1);
			if (publicRoomNames.length <= roomID) {
				String[] temp = new String[roomID + 10];
				for (int j = 0; j < publicRoomNames.length; j++)
					temp[j] = publicRoomNames[j];
				publicRoomNames = temp;

			}
			publicRoomNames[roomID] = charset.decode(tempBuffer).toString();
		}
	}

	public synchronized static void getNewRoomReplyPriv(int roomNumber) {
		for (PrivateRoom room : privateRooms) {
			if (room.id == -1) {
				room.id = roomNumber;
				GUIManager.createRoom(room.name, room.id, true);
				break;
			}
		}

	}

	public synchronized static void getNewRoomReplyPubl(int roomNumber,
			String name) {
		if (roomNumber >= publicRoomNames.length) {
			String[] temp = new String[roomNumber + 10];
			MulticastSocket[] temp2 = new MulticastSocket[roomNumber + 10];
			for (int i = 0; i < publicRoomNames.length; i++) {
				temp[i] = publicRoomNames[i];
				temp2[i] = multicastSockets[i];
			}
			publicRoomNames = temp;
			multicastSockets = temp2;
		}

		publicRoomNames[roomNumber] = name;
		GUIManager.createRoom(name, roomNumber, false);
	}

	public synchronized static void getNewRoomReplyMCPubl(int roomNumber,
			byte[] address, int port, String name) {
		if (roomNumber >= publicRoomNames.length) {
			String[] temp = new String[roomNumber + 10];
			MulticastSocket[] temp2 = new MulticastSocket[roomNumber + 10];
			for (int i = 0; i < publicRoomNames.length; i++) {
				temp[i] = publicRoomNames[i];
				temp2[i] = multicastSockets[i];
			}
			publicRoomNames = temp;
			multicastSockets = temp2;
		}

		publicRoomNames[roomNumber] = name;
		try {
			multicastSockets[roomNumber] = new MulticastSocket(port);
			InetAddress add = InetAddress.getByAddress(address);
			multicastSockets[roomNumber].joinGroup(add);
		} catch (IOException e) {
			e.printStackTrace();
		}
		GUIManager.createRoom(name, roomNumber, false);
	}

	public synchronized static void getNewRoomReplyInform(int roomNumber,
			String name) {
		if (roomNumber >= publicRoomNames.length) {
			String[] temp = new String[roomNumber + 10];
			for (int i = 0; i < publicRoomNames.length; i++) {
				temp[i] = publicRoomNames[i];
			}
			publicRoomNames = temp;
		}

		publicRoomNames[roomNumber] = name;
	}

	public synchronized static boolean sendNewRoomRequest(String name,
			boolean priv, boolean multicast) {

		if (priv) {
			if (privateRooms == null)
				privateRooms = new ArrayList<ClientManager.PrivateRoom>();
			privateRooms.add(new PrivateRoom(name));

		}

		byte[] nameBytes = name.getBytes();
		messageWrapper = ByteBuffer.allocate(1 + 4 + 1 + nameBytes.length);
		messageWrapper.put((priv) ? MessageCodes.NEW_ROOM_REQUEST_PRIV
				: MessageCodes.NEW_ROOM_REQUEST_PUBL);
		messageWrapper.putInt(id);
		if (multicast) {
			messageWrapper.put((byte) -1); // indicates a multicast session
		} else {
			messageWrapper.put((byte) -2); // indicates not a multicast session
		}
		messageWrapper.put(nameBytes);
		return client.sendMessage(messageWrapper.array());
	}

	public synchronized static boolean sendInvitationRequest(String user,
			int room) {
		int userID = -1;
		if (user.equals(userName)) {
			JOptionPane.showMessageDialog(null, "Inviting yourself ?!!");
			return false;
		} else {
			for (int i = 0; i < userNames.length; i++) {
				if (userNames[i] != null && userNames[i].equals(user)) {
					userID = i;
					break;
				}
			}
		}
		if (multicastSockets[room] == null) {
			messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 4);
			messageWrapper.put(MessageCodes.INIVITE_USER_REQUEST);
			messageWrapper.putInt(id);
			messageWrapper.putInt(userID);
			messageWrapper.putInt(room);
			return client.sendMessage(messageWrapper.array());
		} else {
			messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 4 + 4 + 4);
			messageWrapper.put(MessageCodes.INIVITE_USER_MC_REQUEST);
			messageWrapper.putInt(id);
			messageWrapper.putInt(userID);
			messageWrapper.putInt(room);
			messageWrapper.put(multicastSockets[room].getInetAddress()
					.getAddress());
			messageWrapper.putInt(multicastSockets[room].getPort());
			return client.sendMessage(messageWrapper.array());
		}

	}

	public synchronized static void getInvitationReply(int room, String name) {
		if (privateRooms == null)
			privateRooms = new ArrayList<ClientManager.PrivateRoom>();
		PrivateRoom temp = new PrivateRoom(name);
		temp.id = room;
		GUIManager.joinRoom(name, room);
		privateRooms.add(temp);
	}

	public synchronized static void sendJoinPublRoom(int room) {
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4);
		messageWrapper.put(MessageCodes.JOIN_PUBLIC_ROOM);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		client.sendMessage(messageWrapper.array());
	}

	public synchronized static void registerInMulticast(int room,
			byte[] address, int port) {
		try {
			multicastSockets[room] = new MulticastSocket(port);
			multicastSockets[room].joinGroup(InetAddress.getByAddress(address));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static int getJoinRoomId(String room) {
		for (int i = 0; i < publicRoomNames.length; i++) {
			if (publicRoomNames[i].equals(room)) {
				sendJoinPublRoom(i);
				return i;
			}
		}
		return -1;
	}

	public static synchronized void displayMessage(boolean isPublic,
			int roomID, int senderID, String msg) {
		String roomName = "";
		if (isPublic)
			roomName = publicRoomNames[roomID];
		else {
			for (PrivateRoom p : privateRooms) {
				if (p.id == roomID) {
					roomName = p.name;
					break;
				}

			}
		}

		GUIManager.displayMessage(senderID, userNames[senderID], roomName, msg);
	}

	public synchronized static void getDisconnectInform(int user) {
		GUIManager.removeUser(userNames[user]);
		userNames[user] = null;
	}

	public synchronized static void logoff() {
		messageWrapper = ByteBuffer.allocate(1 + 4);
		messageWrapper.put(MessageCodes.DISCONNECT_REQUEST);
		messageWrapper.putInt(id);
		client.sendMessage(messageWrapper.array());

		try {
			textQueue.stopQueue();
			sysQueue.stopQueue();
			datagramListener.stopListener();
			localSocket.close();
		} catch (IOException e) {
			System.out.println(e);
		}

		GUIManager.disconnect();

	}

	public synchronized static void getCloseRoomInform(int room, boolean priv) {
		String roomName = "";
		if (priv) {
			for (int i = 0; i < privateRooms.size(); i++) {
				if (privateRooms.get(i).id == room) {
					roomName = privateRooms.get(i).name;
					privateRooms.remove(i);
					break;
				}
			}
		} else {
			roomName = publicRoomNames[room];
			publicRoomNames[room] = null;
			if (multicastSockets[room] != null) {
				multicastSockets[room].close();
				multicastSockets[room] = null;
			}
		}
		GUIManager.closeRoom(roomName);
	}

	public synchronized static void sendCloseRoomRequest(int room, boolean priv) {
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4);
		messageWrapper.put((priv) ? MessageCodes.CLOSE_ROOM_PRIV_REQUEST
				: MessageCodes.CLOSE_ROOM_PUBL_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		client.sendMessage(messageWrapper.array());
	}

	public synchronized static void getLoginRefreshPubl(byte[] list) {
		messageWrapper = ByteBuffer.wrap(list);
		byte nextByte = messageWrapper.get();
		while (nextByte != -2) {
			messageWrapper.position(messageWrapper.position() - 1);
			int roomID = messageWrapper.getInt();
			if (roomID == 0) {
				nextByte = messageWrapper.get();
				continue;
			}

			GUIManager.createRoom(publicRoomNames[roomID], roomID, false);
			nextByte = messageWrapper.get();
		}
	}

	public synchronized static void getLoginRefreshPriv(byte[] list) {
		messageWrapper = ByteBuffer.wrap(list, 4, list.length - 4);
		privateRooms = new ArrayList<PrivateRoom>();
		Charset charset = Charset.defaultCharset();
		byte nextByte = messageWrapper.get();
		while (nextByte != -2) {
			messageWrapper.position(messageWrapper.position() - 1);
			int roomID = messageWrapper.getInt();
			byte tempByte = messageWrapper.get();
			int i = 1;
			while (tempByte != -1) {
				i++;
				tempByte = messageWrapper.get();
			}
			messageWrapper.position(messageWrapper.position() - i);
			byte[] name = new byte[i];
			messageWrapper.get(name);
			ByteBuffer tempBuffer = ByteBuffer.wrap(name, 0, name.length - 1);
			String roomName = charset.decode(tempBuffer).toString();
			PrivateRoom newPrivateRoom = new PrivateRoom(roomName);
			newPrivateRoom.id = roomID;
			privateRooms.add(newPrivateRoom);
			GUIManager.createRoom(newPrivateRoom.name, newPrivateRoom.id, true);
			nextByte = messageWrapper.get();

			GUIManager.showUsers(userNames);
		}

	}

	public synchronized static void getLoginReplyInform(int id, String name) {
		if (id >= userNames.length) {
			String[] temp = new String[id + 10];
			for (int i = 0; i < userNames.length; i++) {
				temp[i] = userNames[i];
			}
			userNames = temp;
		}
		userNames[id] = name;
		GUIManager.updateUsers(name, userNames.length);
		DisplayBox.addColors(userNames.length);
	}

	public synchronized static void sendFileTransferTCPRequest(String user,
			String filePath) {
		int userID = -1;
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i].equals(user)) {
				userID = i;
				break;
			}
		}

		FileParser file = new FileParser(filePath);
		byte[] name = file.getName().getBytes();
		byte[] fileBytes = file.getFileBytes();
		Date currentDate = new Date();
		messageWrapper = ByteBuffer.allocate(1 + 4 + 8 + 4 + name.length + 1
				+ fileBytes.length);
		messageWrapper.put(MessageCodes.FILE_TRANSFER_TCP_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putLong(currentDate.getTime());
		messageWrapper.putInt(userID);
		messageWrapper.put(name);
		messageWrapper.put((byte) -1);
		messageWrapper.put(fileBytes);
		client.sendMessage(messageWrapper.array());
	}

	public synchronized static void sendFileTransferUDPRequest(String user,
			String filePath, long fileSize) {
		int userID = -1;
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i] != null && userNames[i].equals(user)) {
				userID = i;
				break;
			}
		}
		FileParser file = new FileParser(filePath);
		byte[] name = file.getName().getBytes();
		byte[] fileBytes = file.getFileBytes();
		int length = fileBytes.length;
		int from = 0;
		do {
			messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 8 + name.length
					+ 1 + 50000);
			messageWrapper.position(0);
			messageWrapper.put(MessageCodes.FILE_TRANSFER_UDP_REQUEST);
			messageWrapper.putInt(id);
			messageWrapper.putInt(userID);
			messageWrapper.putLong(fileSize);
			messageWrapper.put(name);
			messageWrapper.put((byte) -1);
			int to = (from + 50000);
			messageWrapper.put(Arrays.copyOfRange(fileBytes, from, to));
			from += 50000;
			try {
				byte[] wrapperBytes = messageWrapper.array();
				// Should change local host to the Server IP
				datagramPacket = new DatagramPacket(wrapperBytes,
						wrapperBytes.length, InetAddress.getLocalHost(), 2312);
				DatagramListener.datagramSocket.send(datagramPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			length -= 50000;
		} while (length > 50000);
	}

	public synchronized static void getFileTransferReply(int sender,
			int fileID, String fileName) {
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4);
		if (GUIManager.recieveFile(userNames[sender], fileName) == 1)
			messageWrapper.put(MessageCodes.FILE_TRANSFER_REFUSE);
		else
			messageWrapper.put(MessageCodes.FILE_TRANSFER_TCP_ACCEPT);

		messageWrapper.putInt(id);
		messageWrapper.putInt(fileID);
		client.sendMessage(messageWrapper.array());
	}

	public synchronized static void getFileTransferUDPReply(int sender,
			int fileID, long fileSize, String fileName) {
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 8);
		if (GUIManager.recieveFile(userNames[sender], fileName) == 1)
			messageWrapper.put(MessageCodes.FILE_TRANSFER_REFUSE);
		else
			messageWrapper.put(MessageCodes.FILE_TRANSFER_UDP_ACCEPT);

		messageWrapper.putInt(id);
		messageWrapper.putInt(fileID);
		messageWrapper.putLong(fileSize);
		byte[] buffData = messageWrapper.array();
		try {
			datagramPacket = new DatagramPacket(buffData, buffData.length,
					InetAddress.getLocalHost(), 2312);
			DatagramListener.datagramSocket.send(datagramPacket);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static void getFileTransferData(String fileName,
			byte[] fileBytes) {
		String filePath = GUIManager.getFilePath(fileName);
		if (filePath == null)
			return;

		new FileParser(null, filePath, fileBytes);
	}

	// FIXME fix assembly of file
	public synchronized static void getUDPFileTransferData(long fileSize,
			String fileName, byte[] fileBytes) {
		FileParser file = null;
		for (FileParser fp : tempFiles) {
			String existingFileName = fp.getName().substring(0,
					fp.getName().length() - 1);
			System.out.println(existingFileName);
			System.out.println(fileName);
			if (existingFileName.equals(fileName)) {
				file = fp;
				break;
			}
		}

		if (file != null && fileSize >= file.getFileBytes().length) {
			byte[] newFile = new byte[fileBytes.length
					+ file.getFileBytes().length];
			byte[] oldFile = file.getFileBytes();
			System.arraycopy(oldFile, 0, newFile, 0, oldFile.length);
			System.arraycopy(fileBytes, 0, newFile, oldFile.length,
					fileBytes.length);
			tempFiles.removeItem(file);
			tempFiles.addItem(new FileParser(tempFiles, fileName, newFile));

		} else if (file != null && fileSize <= file.getFileBytes().length) {
			byte[] newFile = new byte[fileBytes.length
					+ file.getFileBytes().length];
			byte[] oldFile = file.getFileBytes();
			System.arraycopy(oldFile, 0, newFile, 0, oldFile.length);
			System.arraycopy(fileBytes, 0, newFile, oldFile.length,
					fileBytes.length);
			tempFiles.removeItem(file);
			String filePath = GUIManager.getFilePath(fileName);
			if (filePath == null)
				return;
			new FileParser(null, filePath, newFile);

		} else if (file == null && fileSize <= fileBytes.length) {
			String filePath = GUIManager.getFilePath(fileName);
			if (filePath == null)
				return;
			new FileParser(null, filePath, fileBytes);
		} else if (file == null && fileSize >= fileBytes.length) {
			tempFiles.addItem(new FileParser(tempFiles, fileName, fileBytes));
		}
	}

	private synchronized static String[] getRoomMembersNames(int room,
			int[] membersIds) {
		String[] roomMembersNames = new String[membersIds.length];
		int id;
		for (int i = 0; i < membersIds.length; i++) {
			id = membersIds[i];
			roomMembersNames[i] = userNames[id];
		}

		return roomMembersNames;

	}

	// TODO check the method again
	// TODO is it needed to put the byte -1 or not as I'm accessing the byte
	// buffer with the index
	public synchronized static void sendRoomMembersRequest(int room,
			boolean isPriv) {
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 1);
		messageWrapper.put(MessageCodes.ROOM_MEMBERS_IDS_LIST_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		if (isPriv) {
			messageWrapper.put((byte) 1);
		} else {
			messageWrapper.put((byte) 0);
		}
		client.sendMessage(messageWrapper.array());
	}

	// TODO is it needed to send the sender id
	public synchronized static void getMemberSelected(int member) {

	}

	public synchronized static void sendRemoveMemberRequest(int room,
			boolean isPriv, String member) {
		int memberID = -1;
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i] != null && userNames[i].equals(member)) {
				memberID = i;
			}
		}

		messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 1 + 4);
		messageWrapper.put(MessageCodes.SEND_REMOVE_MEMBER_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		if (isPriv) {
			messageWrapper.put((byte) 1);
		} else {
			messageWrapper.put((byte) 0);
		}
		messageWrapper.putInt(memberID);
		client.sendMessage(messageWrapper.array());

	}

	public static void roomMembersIDList(int roomID, byte[] info) {
		ByteBuffer messageBreaker = ByteBuffer.allocate(info.length);
		messageBreaker.put(info);
		int[] membersIds = new int[info.length / 4];
		int j = 0;
		// now we need to loop over the ID's in the byte buffer sent from
		// servers
		for (int i = 0; i < membersIds.length; i++) {
			messageBreaker.position(j);
			membersIds[i] = messageBreaker.getInt();
			j = j + 4;

		}
		String[] roomMemberNames = getRoomMembersNames(roomID, membersIds);
		GUIManager.getRoomMembers(roomID, roomMemberNames);

	}

	public synchronized static void userKicked(int roomID, boolean privateRoom) {
		if (privateRoom) {
			for (PrivateRoom p : privateRooms) {
				if (p.id == roomID) {
					JOptionPane
							.showMessageDialog(null,
									"you have been kicked out of room "
											+ p.name + " !");
					GUIManager.closeRoom(p.name);
					break;
				}
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"you have been kicked out room " + publicRoomNames[roomID]
							+ "!");
			GUIManager.closeRoom(publicRoomNames[roomID]);
		}
	}

	public synchronized static void sendMuteMemberRequest(int room,
			boolean isPriv, String member) {
		int memberID = -1;
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i] != null && userNames[i].equals(member)) {
				memberID = i;
			}
		}
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 1 + 4);
		messageWrapper.put(MessageCodes.SEND_MUTE_MEMBER_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		if (isPriv) {
			messageWrapper.put((byte) 1);
		} else {
			messageWrapper.put((byte) 0);
		}
		messageWrapper.putInt(memberID);
		client.sendMessage(messageWrapper.array());
		System.out.println("1st step done");
	}

	public static void addMutedRoom(int room, boolean isPriv) {
		if (isPriv) {
			MutedPrivateRooms.add(new Integer(room));
			for (PrivateRoom p : privateRooms) {
				if (p.id == room) {
					JOptionPane.showMessageDialog(null, "Room " + p.name
							+ " says SHUT UP!");
					break;
				}
			}
		} else {
			MutedPublicRooms.add(new Integer(room));
			JOptionPane.showMessageDialog(null, "Room " + publicRoomNames[room]
					+ " says SHUT UP!");
		}

	}

	public synchronized static void sendUnMuteMemberRequest(int room,
			boolean isPriv, String member) {
		int memberID = -1;
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i] != null && userNames[i].equals(member)) {
				memberID = i;
			}
		}
		messageWrapper = ByteBuffer.allocate(1 + 4 + 4 + 1 + 4);
		messageWrapper.put(MessageCodes.SEND_UN_MUTE_MEMBER_REQUEST);
		messageWrapper.putInt(id);
		messageWrapper.putInt(room);
		if (isPriv) {
			messageWrapper.put((byte) 1);
		} else {
			messageWrapper.put((byte) 0);
		}
		messageWrapper.putInt(memberID);
		client.sendMessage(messageWrapper.array());
	}

	public static void RemoveMutedRoom(int room, boolean isPriv) {
		if (isPriv) {
			for (int i = 0; i < MutedPrivateRooms.size(); i++) {
				if (MutedPrivateRooms.get(i).intValue() == room) {
					MutedPrivateRooms.remove(i);
					for (PrivateRoom p : privateRooms) {
						if (p.id == room) {
							JOptionPane.showMessageDialog(null, "Room "
									+ p.name + " has forgiven you!");
							break;
						}
					}
				}
			}
		} else {
			for (int i = 0; i < MutedPublicRooms.size(); i++) {
				if (MutedPublicRooms.get(i).intValue() == room) {
					MutedPublicRooms.remove(i);
					JOptionPane.showMessageDialog(null, "Room "
							+ publicRoomNames[room] + " has forgiven you!");
				}
			}
		}
	}

	public static int getUserID() {
		return id;
	}

	public static String getUserName() {
		return userName;
	}

	public static String[] getRoomNamesList() {
		return publicRoomNames;
	}

	// ====================Inner Classes====================
	// This is will probably be modified to be a GUI class
	private static class PrivateRoom {
		private String name;

		private int id;

		public PrivateRoom(String name) {
			this.name = name;
			id = -1;
			// This is a very stupid way to deal with this
			// but I couldn't come up with a better way that
			// was simple enough
		}
	}
	// ======================================================
}
