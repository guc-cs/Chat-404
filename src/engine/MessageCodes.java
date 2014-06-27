package engine;

public class MessageCodes {
	
	public static final byte TEXT_MESSAGE_PUBL = 0x00;
	public static final byte TEXT_MESSAGE_PRIV = 0x10;
	public static final byte LOGIN_REQUEST = 0x01;
	public static final byte LOGIN_REPLY_SUCC = 0x01;
	public static final byte LOGIN_REPLY_INFORM = 0x21;
	public static final byte LOGIN_REPLY_REFRESH_PRIV = 0x31;
	public static final byte LOGIN_REPLY_REFRESH_PUBL = 0x41;
	public static final byte LOGIN_REPLY_FAIL = 0x11;
	public static final byte USER_LIST_REQUEST = 0x02;
	public static final byte USER_LIST_REPLY = 0x02;
	public static final byte ROOM_LIST_REQUEST = 0x03;
	public static final byte ROOM_LIST_REPLY = 0x03;
	public static final byte NEW_ROOM_REQUEST_PRIV = 0x14;
	public static final byte NEW_ROOM_REQUEST_PUBL = 0x04;
	public static final byte NEW_ROOM_REPLY_PRIV = 0x14;
	public static final byte NEW_ROOM_REPLY_PUBL = 0x04;
	public static final byte NEW_ROOM_REPLY_MC_PUBL = 0x34;
	public static final byte NEW_ROOM_REPLY_INFORM = 0x24;
	public static final byte INIVITE_USER_REQUEST = 0x05;
	public static final byte INIVITE_USER_MC_REQUEST = 0x15;
	public static final byte INVITE_USER_REPLY = 0x05;
	public static final byte INVITE_USER_MC_REPLY = 0x15;
	public static final byte JOIN_PUBLIC_ROOM = 0x06;
	public static final byte REGISTER_IN_MULTICAST = 0x16;
	public static final byte DISCONNECT_REQUEST = 0x07;
	public static final byte DISCONNECT_INFORM = 0x07;
	public static final byte CLOSE_ROOM_PUBL_REQUEST = 0x08;
	public static final byte CLOSE_ROOM_PRIV_REQUEST = 0x18;
	public static final byte CLOSE_ROOM_PRIV_INFORM = 0x18;
	public static final byte CLOSE_ROOM_PUBL_INFORM = 0x08;
	public static final byte FILE_TRANSFER_TCP_REQUEST = 0x09;
	public static final byte FILE_TRANSFER_UDP_REQUEST = 0x39;
	public static final byte FILE_TRANSFER_TCP_REPLY = 0x09;
	public static final byte FILE_TRANSFER_UDP_REPLY = 0x39;
	public static final byte FILE_TRANSFER_REPLY_DATA = 0x19;
	public static final byte FILE_TRANSFER_UDP_REPLY_DATA = 0x29;
	public static final byte FILE_TRANSFER_TCP_ACCEPT = 0x19;
	public static final byte FILE_TRANSFER_UDP_ACCEPT = 0x49;
	public static final byte FILE_TRANSFER_REFUSE  = 0x29;
	public static final byte FILE_TRANSFER_ECHO = 0x59;
	public static final byte ROOM_MEMBERS_IDS_LIST_REQUEST = 0x35;
	public static final byte ROOM_MEMBERS_IDS_LIST_REPLY= 0x35;
	public static final byte KICK_USER_FROM_ROOM = 0x40;
	public static final byte MUTE_USER_IN_ROOM = 0x45;
	public static final byte UN_MUTE_USER_IN_ROOM = 0x50;
    public static final byte SEND_REMOVE_MEMBER_REQUEST = 0x55;
    public static final byte SEND_REMOVE_MEMBER_REPLY = 0x55;
    public static final byte SEND_MUTE_MEMBER_REQUEST = 0x60;
    public static final byte SEND_MUTE_MEMBER_REPLY = 0x60;
    public static final byte SEND_UN_MUTE_MEMBER_REQUEST = 0x65;
    public static final byte SEND_UN_MUTE_MEMBER_REPLY = 0x65;

}
