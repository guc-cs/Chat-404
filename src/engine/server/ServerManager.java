/************************************
 /************************************
 * Title: 	ServerManager
 * Date:	10.17.2012
 * Purpose: Collection of static
 * 			methods and variables 
 * 			that provide the engine
 * 			of the server
 ************************************/

package engine.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import engine.CompressionLayer;
import engine.EncryptionLayer;
import engine.FileParser;
import engine.FixedIndexArray;
import engine.MessageCodes;

public class ServerManager {

	protected static SystemMessagesQueue sysQueue;
	protected static TextMessagesQueue textQueue;
	protected static FixedIndexArray<Client> clients;
	protected static FixedIndexArray<ArrayList<Integer>> privateRooms;
	protected static FixedIndexArray<ArrayList<Integer>> publicRooms;
	protected static FixedIndexArray<String> publicRoomNames;
	protected static FixedIndexArray<String> privateRoomNames;
	public static FixedIndexArray<Multicast> publicRoomMC;
	protected static ChatServer socketListener;
	protected static HoldingCell holdingCell;
	protected static EncryptionLayer encryptionLayer;
	protected static FixedIndexArray<FileParser> files;
	protected static InetAddress initialMulticast;
	protected static int multicastPorts;
	protected static DatagramListener datagramListener;
	protected static CompressionLayer compressionLayer;

	public static void main(String... args) {
		init();
	}

	public static void init() {
		encryptionLayer = new EncryptionLayer();
		compressionLayer = new CompressionLayer();
		sysQueue = new SystemMessagesQueue();
		textQueue = new TextMessagesQueue();
		datagramListener = new DatagramListener(sysQueue);
		clients = new FixedIndexArray<Client>(10);
		privateRooms = new FixedIndexArray<ArrayList<Integer>>(10);
		publicRooms = new FixedIndexArray<ArrayList<Integer>>(10);
		publicRoomNames = new FixedIndexArray<String>(10);
		publicRoomMC = new FixedIndexArray<ServerManager.Multicast>(10);
		privateRoomNames = new FixedIndexArray<String>(10);
		files = new FixedIndexArray<FileParser>(5);
		try {
			initialMulticast = InetAddress.getByName("224.0.0.1");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		multicastPorts = 2000;
		socketListener = new ChatServer();
		holdingCell = new HoldingCell();
		publicRooms.addItem(new ArrayList<Integer>());
		publicRoomNames.addItem("Lobby");
		sysQueue.start();
		textQueue.start();
		socketListener.start();
		datagramListener.start();
	}

	protected synchronized static void removeClient(int user) {
		int r = 0;
		for (ArrayList<Integer> room : publicRooms) {
			if (room == null)
				break;
			room.remove(new Integer(user));
			if (room.size() == 0 && r != 0)
				closeRoom(r, false);
			r++;
		}
		r = 0;
		for (ArrayList<Integer> room : privateRooms) {
			if (room == null)
				break;
			room.remove(new Integer(user));
			if (room.size() == 0)
				closeRoom(r, true);
			r++;
		}

		ByteBuffer reply = ByteBuffer.allocate(1 + 4);
		reply.put(MessageCodes.DISCONNECT_INFORM);
		reply.putInt(user);
		for (Client c : clients) {
			if (c == null || c.getUserId() == user)
				continue;
			c.sendMessage(reply.array());
		}
		clients.get(user).close();
		clients.removeItem(user);
	}

	protected synchronized static ArrayList<Integer> getRoomClients(int room,
			boolean privateRoom) {
		return (privateRoom) ? privateRooms.get(room) : publicRooms.get(room);
	}

	protected synchronized static void createClient(Client client) {
		String name = client.getUserName();
		for (Client c : clients) {
			if (c == null)
				break;
			if (name.equals(c.getUserName())) {
				byte[] message = new byte[1];
				message[0] = MessageCodes.LOGIN_REPLY_FAIL;
				// TODO: add encryption
				client.sendMessage(message);
				client.close();
				return;
			}
		}

		int clientID = clients.addItem(client);
		client.setId(clientID);
		publicRooms.get(0).add(clientID);
		ByteBuffer message = ByteBuffer.allocate(5);
		message.put(MessageCodes.LOGIN_REPLY_SUCC);
		message.putInt(clientID);
		// TODO: add encryption
		client.sendMessage(message.array());
		ArrayList<Integer> roomClients = publicRooms.get(0);
		message = ByteBuffer.allocate(1 + 4 + name.getBytes().length);
		message.put(MessageCodes.LOGIN_REPLY_INFORM);
		message.putInt(clientID);
		message.put(name.getBytes());
		for (int c : roomClients) {
			if (c == clientID)
				continue;
			ServerManager.clients.get(c).sendMessage(message.array());
		}
	}

	protected synchronized static void getUserList(int sender) {
		ByteBuffer clientList = ByteBuffer.allocate(5 + 4 * clients.size());
		clientList.put(MessageCodes.USER_LIST_REPLY);
		clientList.putInt(clients.numberOfElements());
		for (Client c : clients) {
			if (c == null)
				continue;
			clientList.putInt(c.getUserId());
			byte[] name = c.getUserName().getBytes();

			ByteBuffer temp = ByteBuffer.allocate(clientList.capacity()
					+ name.length + 1);
			temp.put(clientList.array());
			temp.position(clientList.position());
			clientList = temp;

			clientList.put(c.getUserName().getBytes());
			clientList.put((byte) -1);
		}
		clients.get(sender).sendMessage(clientList.array());

	}

	protected synchronized static void getRoomList(int sender) {
		ByteBuffer roomList = ByteBuffer.allocate(5 + 4 * publicRooms.size());
		roomList.put(MessageCodes.ROOM_LIST_REPLY);
		roomList.putInt(publicRooms.numberOfElements());
		int i = 0;
		for (String room : publicRoomNames) {
			if (room != null) {
				roomList.putInt(i);
				byte[] roomName = room.getBytes();

				ByteBuffer temp = ByteBuffer.allocate(roomList.capacity()
						+ roomName.length + 1);
				temp.put(roomList.array());
				temp.position(roomList.position());
				roomList = temp;

				roomList.put(roomName);
				roomList.put((byte) -1);
			}
			i++;
		}
		clients.get(sender).sendMessage(roomList.array());

	}

	protected synchronized static void createRoom(int sender, String roomName,
			boolean privateRoom, boolean multicast) {
		if (privateRoom) {
			int roomID = privateRooms.addItem(new ArrayList<Integer>());
			privateRooms.get(roomID).add(sender);
			privateRoomNames.addItem(roomName);
			byte[] roomNameBytes = roomName.getBytes();
			ByteBuffer reply = ByteBuffer.allocate(5 + roomNameBytes.length);
			reply.put(MessageCodes.NEW_ROOM_REPLY_PRIV);
			reply.putInt(roomID);
			reply.put(roomNameBytes);
			clients.get(sender).sendMessage(reply.array());

		} else {
			int roomID = publicRooms.addItem(new ArrayList<Integer>());
			publicRooms.get(roomID).add(sender);
			publicRoomNames.addItem(roomName);
			byte[] roomNameBytes = roomName.getBytes();
			ByteBuffer reply = ByteBuffer
					.allocate(1 + 4 + roomNameBytes.length);
			reply.put(MessageCodes.NEW_ROOM_REPLY_INFORM);
			reply.putInt(roomID);
			reply.put(roomNameBytes);
			ArrayList<Integer> roomClients = publicRooms.get(0);
			for (int c : roomClients) {
				if (c == sender)
					continue;
				ServerManager.clients.get(c).sendMessage(reply.array());
			}
			reply.clear();
			if (multicast) {
				reply = ByteBuffer.allocate(1 + 4 + 4 + 4
						+ roomNameBytes.length);
				reply.put(MessageCodes.NEW_ROOM_REPLY_MC_PUBL);
			} else {
				reply.put(MessageCodes.NEW_ROOM_REPLY_PUBL);
			}
			reply.putInt(roomID);
			if (multicast) {
				ServerManager mc = null;
				publicRoomMC.addItem(mc.new Multicast(initialMulticast,
						multicastPorts));
				reply.put(initialMulticast.getAddress());
				reply.putInt(multicastPorts++);
				// TODO fix multicast ports maximum limit
				byte[] hostAddress = initialMulticast.getAddress();
				hostAddress[3]++;
				try {
					initialMulticast = InetAddress.getByAddress(hostAddress);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
			reply.put(roomNameBytes);
			System.out.println(Arrays.toString(reply.array()));
			clients.get(sender).sendMessage(reply.array());
		}
	}

	protected synchronized static void inviteUser(int sender, int invited,
			int room) {
		privateRooms.get(room).add(invited);
		byte[] roomName = privateRoomNames.get(room).getBytes();
		ByteBuffer reply = ByteBuffer.allocate(roomName.length + 1 + 4 + 4);
		reply.put(MessageCodes.INVITE_USER_REPLY);
		reply.putInt(sender);
		reply.putInt(room);
		reply.put(roomName);
		clients.get(invited).sendMessage(reply.array());
	}

	protected synchronized static void inviteMCUser(int sender, int invited,
			int room, byte[] address, int port) {
		publicRooms.get(room).add(invited);
		byte[] roomName = publicRoomNames.get(room).getBytes();
		ByteBuffer reply = ByteBuffer.allocate(roomName.length + 1 + 4 + 4 + 4
				+ 4);
		reply.put(MessageCodes.INVITE_USER_MC_REPLY);
		reply.putInt(sender);
		reply.putInt(room);
		reply.put(address);
		reply.putInt(port);
		reply.put(roomName);
		clients.get(invited).sendMessage(reply.array());
	}

	protected synchronized static void joinRoom(int sender, int room) {
		publicRooms.get(room).add(sender);
		Multicast mc = publicRoomMC.get(room);
		if (mc != null) {
			byte[] address = mc.add.getAddress();
			int port = mc.port;
			ByteBuffer reply = ByteBuffer.allocate(1 + 4 + 4 + 4);
			reply.put(MessageCodes.REGISTER_IN_MULTICAST);
			reply.putInt(room);
			reply.put(address);
			reply.putInt(port);
			clients.get(sender).sendMessage(reply.array());
		}
	}

	protected synchronized static void closeRoom(int room, boolean privateRoom) {
		ArrayList<Integer> roomClients = (privateRoom) ? privateRooms.get(room)
				: publicRooms.get(room);
		if (privateRoom) {
			privateRooms.removeItem(room);
			privateRoomNames.removeItem(room);
		} else {
			publicRooms.removeItem(room);
			publicRoomNames.removeItem(room);
			publicRoomMC.removeItem(room);
		}
		ByteBuffer reply = ByteBuffer.allocate(1 + 4);
		reply.put((privateRoom) ? MessageCodes.CLOSE_ROOM_PRIV_INFORM
				: MessageCodes.CLOSE_ROOM_PUBL_INFORM);
		reply.putInt(room);
		for (int client : roomClients) {
			clients.get(client).sendMessage(reply.array());
		}
	}

	protected synchronized static void getPublicRoomListOnRefresh(int user) {
		ByteBuffer roomList = ByteBuffer.allocate(5 + 1 + 12 * publicRooms
				.size());
		roomList.put(MessageCodes.LOGIN_REPLY_REFRESH_PUBL);
		int i = 0;
		for (ArrayList<Integer> room : publicRooms) {
			if (room != null && room.contains(user))
				roomList.putInt(i);
			i++;
		}
		roomList.put((byte) -2);
		clients.get(user).sendMessage(roomList.array());

	}

	protected synchronized static void getPrivateRoomListOnRefresh(int user) {
		ByteBuffer reply = ByteBuffer.allocate(1 + 4 + 1 + 4
				* privateRooms.size());
		reply.put(MessageCodes.LOGIN_REPLY_REFRESH_PRIV);
		reply.putInt(user);
		int i = 0;
		for (ArrayList<Integer> room : privateRooms) {
			if (room != null && room.contains(user)) {
				reply.putInt(i);
				byte[] roomName = privateRoomNames.get(i).getBytes();
				if (reply.remaining() < roomName.length) {
					ByteBuffer temp = ByteBuffer.allocate(reply.capacity()
							+ roomName.length + 1);
					temp.put(reply);
					reply = temp;
				}
				reply.put(roomName);
				reply.put((byte) -1);
			}
			i++;
		}
		reply.put((byte) -2);

		clients.get(user).sendMessage(reply.array());
	}

	protected synchronized static void getFileTransferRequest(int sender,
			int reciever, long timeStamp, String fileName, byte[] fileBytes) {
		FileParser file = new FileParser(files, fileName, fileBytes);
		int fileID = files.addItem(file);

		ByteBuffer reply = ByteBuffer.allocate(1 + 8);
		reply.put(MessageCodes.FILE_TRANSFER_ECHO);
		Date currentTime = new Date();
		reply.putLong(currentTime.getTime() - timeStamp);
		clients.get(sender).sendMessage(reply.array());

		reply = ByteBuffer.allocate(1 + 4 + 4 + fileName.getBytes().length);
		reply.put(MessageCodes.FILE_TRANSFER_TCP_REPLY);
		reply.putInt(sender);
		reply.putInt(fileID);
		reply.put(fileName.getBytes());
		clients.get(reciever).sendMessage(reply.array());
	}

	protected synchronized static void getFileTransferUDPRequest(int sender,
			int reciever, String fileName, long fileSize, byte[] fileBytes) {
		FileParser file = null;
		for (FileParser fp : files) {
			String existingFileName = fp.getName().substring(0,
					fp.getName().length() - 2);
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
			files.removeItem(file);
			files.addItem(new FileParser(files, fileName, newFile));
		} else if (file != null && fileSize <= file.getFileBytes().length) {
			byte[] newFile = new byte[fileBytes.length
					+ file.getFileBytes().length];
			byte[] oldFile = file.getFileBytes();
			System.arraycopy(oldFile, 0, newFile, 0, oldFile.length);
			System.arraycopy(fileBytes, 0, newFile, oldFile.length,
					fileBytes.length);
			files.removeItem(file);
			int fileID = files
					.addItem(new FileParser(files, fileName, newFile));
			ByteBuffer reply = ByteBuffer.allocate(1 + 4 + 4 + 8
					+ fileName.getBytes().length);
			reply.put(MessageCodes.FILE_TRANSFER_UDP_REPLY);
			reply.putInt(sender);
			reply.putInt(fileID);
			reply.putLong(fileSize);
			reply.put(fileName.getBytes());
			clients.get(reciever).sendMessage(reply.array());
		} else if (file == null && fileSize <= fileBytes.length) {
			int fileID = files.addItem(new FileParser(files, fileName,
					fileBytes));
			// int fileID = files.addItem(new FileParser(fileName));
			ByteBuffer reply = ByteBuffer.allocate(1 + 4 + 4 + 8
					+ fileName.getBytes().length);
			reply.put(MessageCodes.FILE_TRANSFER_UDP_REPLY);
			reply.putInt(sender);
			reply.putInt(fileID);
			reply.putLong(fileSize);
			reply.put(fileName.getBytes());
			clients.get(reciever).sendMessage(reply.array());
		} else if (file == null && fileSize >= fileBytes.length) {
			files.addItem(new FileParser(files, fileName, fileBytes));
		}

	}

	protected synchronized static void getFileTransferTCPReply(int sender,
			int fileID, boolean accept) {
		if (!accept) {
			FileParser fp = files.get(fileID);
			fp.cancel();
			fp.deleteFile();
			files.removeItem(fileID);
		}
		FileParser fp = files.get(fileID);
		byte[] nameBytes = fp.getName().getBytes();
		byte[] fileBytes = fp.getFileBytes();
		ByteBuffer reply = ByteBuffer.allocate(1 + nameBytes.length + 1
				+ fileBytes.length);
		reply.put(MessageCodes.FILE_TRANSFER_REPLY_DATA);
		reply.put(nameBytes);
		reply.put((byte) -1);
		reply.put(fileBytes);
		clients.get(sender).sendMessage(reply.array());

	}

	protected synchronized static void getFileTransferUDPReply(int fileID,
			long fileSize, int port, byte[] hostAddress) {
		FileParser fp = files.get(fileID);
		byte[] nameBytes = fp.getName().getBytes();
		byte[] fileBytes = fp.getFileBytes();
		int length = fileBytes.length;
		int from = 0;
		do {
			ByteBuffer messageWrapper = ByteBuffer.allocate(1 + 4 + 8
					+ nameBytes.length + 1 + 50000);
			messageWrapper.put(MessageCodes.FILE_TRANSFER_UDP_REPLY_DATA);
			messageWrapper.putInt(fileID);
			messageWrapper.putLong(fileSize);
			messageWrapper.put(nameBytes);
			messageWrapper.put((byte) -1);
			int to = (from + 50000);
			messageWrapper.put(Arrays.copyOfRange(fileBytes, from, to));
			from += 50000;
			try {
				byte[] wrapperBytes = messageWrapper.array();
				DatagramPacket datagramPacket = new DatagramPacket(
						wrapperBytes, wrapperBytes.length,
						InetAddress.getByAddress(hostAddress), port);
				DatagramListener.datagramSocket.send(datagramPacket);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			length -= 50000;
		} while (length > 50000);
	}

	protected synchronized static void getMembersList(int sender, int room,
			boolean privRoom) {
		ArrayList<Integer> membersId;
		membersId = ServerManager.getRoomClients(room, privRoom);
		ByteBuffer roomMembersId = ByteBuffer.allocate(1 + 4 + 4 * membersId
				.size());
		// roomMembersId.put(arg0) // TO put the Message code
		// roomMembersId.put(); //send the ArrayList<Integers> of the room
		// members
		roomMembersId.put(MessageCodes.ROOM_MEMBERS_IDS_LIST_REPLY);
		roomMembersId.putInt(room);
		for (int i = 0; i < membersId.size(); i++) {
			roomMembersId.putInt(membersId.get(i).intValue());
		}
		clients.get(sender).sendMessage(roomMembersId.array());
	}

	// FIXME how to retrieve the member needed?!!
	protected synchronized static void RemoveRoomMember(int sender, int room,
			boolean isPriv, int member) {
		if (isPriv) {
			for (int i = 0; i < privateRooms.get(room).size(); i++) {
				if ((privateRooms.get(room).get(i)) == member) {
					privateRooms.get(room).remove(i);
					System.out.println("Aiwaaaan");
				}

			}
			System.out.println("Aiwaaaan");
		} else {
			for (int i = 0; i < publicRooms.get(room).size(); i++) {
				if ((publicRooms.get(room).get(i)) == member) {
					publicRooms.get(room).remove(i);
					System.out.println("shalet");
				}
			}

		}

		ByteBuffer reply = ByteBuffer.allocate(1 + 4 + 1);
		reply.put(MessageCodes.SEND_REMOVE_MEMBER_REPLY);
		reply.putInt(room);
		reply.put((isPriv) ? (byte) 1 : (byte) 0);
		clients.get(member).sendMessage(reply.array());

	}

	protected synchronized static void MuteRoomMemberReply(int sender,
			int room, boolean isPriv, int member) {

		ByteBuffer message = ByteBuffer.allocate(1 + 4 + 1);
		message.put(MessageCodes.SEND_MUTE_MEMBER_REPLY);
		message.putInt(room);
		if (isPriv) {
			message.put((byte) 1);
		} else {
			message.put((byte) 0);
		}
		clients.get(member).sendMessage(message.array());

	}

	protected synchronized static void UnMuteRoomMemberReply(int sender,
			int room, boolean isPriv, int member) {

		ByteBuffer message = ByteBuffer.allocate(1 + 4 + 1);
		message.put(MessageCodes.SEND_UN_MUTE_MEMBER_REPLY);
		message.putInt(room);
		if (isPriv) {
			message.put((byte) 1);
		} else {
			message.put((byte) 0);
		}
		clients.get(member).sendMessage(message.array());

	}

	class Multicast {
		protected InetAddress add;
		protected int port;

		public Multicast(InetAddress add, int port) {
			this.add = add;
			this.port = port;
		}
	}

}
