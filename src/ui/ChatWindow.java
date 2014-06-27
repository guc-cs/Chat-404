package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import engine.client.ClientManager;

@SuppressWarnings("serial")
public class ChatWindow extends JFrame {

	private JPanel contentPane;
	private TextArea messageBox;
	private JMenu chattingMenu;
	private JMenuItem menuClose;
	private JMenuItem menuLogOut;
	private DisplayBox lobby;
	private JTabbedPane tabbedPane;
	private JMenu ChatRoomsMenu;
	private JMenuItem menuCreateRoom;
	private JMenuItem menuInvite;
	private JMenuItem menuPRoomsList;
	private DefaultListModel theModel;
	private JList onLineUsers;
	private JButton sendButton;
	private JPanel users;
	private JScrollPane scrollPane;
	private JScrollPane pane;
	private JMenuItem sendFileMenuItem;
	private JDesktopPane desktopPane;
	private JDesktopPane desktopPane_1;
	private JLabel lblNewLabel;

	public ChatWindow(String userName) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				ChatWindow.class.getResource("/icons/chatting_online.png")));
		setTitle(userName + " : Now Chatting...");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 770, 520);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		chattingMenu = new JMenu("Chatting");
		menuBar.add(chattingMenu);

		menuLogOut = new JMenuItem("Log out");
		menuLogOut.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/logout.png")));
		menuLogOut.setAlignmentX(Component.LEFT_ALIGNMENT);
		chattingMenu.add(menuLogOut);

		menuClose = new JMenuItem("Close");
		menuClose.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/close.png")));
		chattingMenu.add(menuClose);

		JMenu mnNewMenu_1 = new JMenu("Tools");
		menuBar.add(mnNewMenu_1);

		sendFileMenuItem = new JMenuItem("Send file");
		sendFileMenuItem.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/send_file.png")));
		mnNewMenu_1.add(sendFileMenuItem);

		ChatRoomsMenu = new JMenu("ChatRooms");
		menuBar.add(ChatRoomsMenu);

		menuCreateRoom = new JMenuItem("Create room");
		menuCreateRoom.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/new_room.png")));
		ChatRoomsMenu.add(menuCreateRoom);

		menuInvite = new JMenuItem("Invite your friends ");
		menuInvite.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/invite_user.png")));
		menuInvite.setEnabled(false);
		ChatRoomsMenu.add(menuInvite);

		menuPRoomsList = new JMenuItem("Public rooms list");
		menuPRoomsList.setIcon(new ImageIcon(ChatWindow.class
				.getResource("/icons/room_list.png")));
		ChatRoomsMenu.add(menuPRoomsList);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About ");
		mnHelp.add(mntmAbout);

		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 139));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		desktopPane = new JDesktopPane();
		desktopPane.setBounds(10, 24, 596, 286);
		desktopPane.setBackground(Color.WHITE);

		users = new JPanel();
		users.setBounds(644, 24, 100, 426);
		users.setBackground(Color.WHITE);

		desktopPane_1 = new JDesktopPane();
		desktopPane_1.setBounds(10, 346, 596, 55);
		desktopPane_1.setBackground(Color.WHITE);
		desktopPane_1.setLayout(new BorderLayout(0, 0));

		messageBox = new TextArea();
		desktopPane_1.add(messageBox);

		theModel = new DefaultListModel();
		users.setLayout(new BorderLayout(0, 0));
		pane = new JScrollPane();
		users.add(pane);
		contentPane.setLayout(null);
		contentPane.add(desktopPane_1);
		contentPane.add(desktopPane);
		desktopPane.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setOpaque(true);
		tabbedPane.setBackground(Color.WHITE);
		desktopPane.add(tabbedPane);

		scrollPane = new JScrollPane();
		tabbedPane.addTab("Lobby", scrollPane);

		lobby = new DisplayBox();
		lobby.setID(0);
		scrollPane.setViewportView(lobby);
		lobby.setName("Lobby");
		contentPane.add(users);
		onLineUsers = new JList(theModel);
		users.add(onLineUsers);
		onLineUsers.setVisible(true);
		onLineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		lblNewLabel = new JLabel("Press Enter to send your message");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBounds(409, 321, 197, 14);
		contentPane.add(lblNewLabel);

		sendButton = new JButton("Send message");
		sendButton.setBounds(493, 427, 113, 23);
		contentPane.add(sendButton);

		// ================================================

		addListeners();
		addResizeListeners();
		setVisible(true);
	}

	private void addListeners() {
		ActionListener roomCreating = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIManager.roomCreationFrame = new CreateRoomOptions();
			}

		};
		menuCreateRoom.addActionListener(roomCreating);

		ActionListener messageSend = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageBox.getText();
				if (message.equals(""))
					return;
				DisplayBox room = (DisplayBox) ((JScrollPane) tabbedPane
						.getSelectedComponent()).getViewport().getView();

				boolean priv = (room.getName().charAt(
						room.getName().length() - 1) == '*') ? true : false;
				if (ClientManager.sendMessage(message, room.getID(), priv))
					room.displayMessage(ClientManager.getUserID(),
							ClientManager.getUserName(), message);

				messageBox.setText("");
			}
		};
		sendButton.addActionListener(messageSend);

		ActionListener logOutListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClientManager.logoff();
			}
		};
		menuLogOut.addActionListener(logOutListener);

		ActionListener closeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClientManager.logoff();
			}
		};
		menuClose.addActionListener(closeListener);

		KeyListener theEnterListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String message = messageBox.getText();
					if (message.equals(""))
						return;

					DisplayBox room = (DisplayBox) ((JScrollPane) tabbedPane
							.getSelectedComponent()).getViewport().getView();

					boolean priv = (room.getName().charAt(
							room.getName().length() - 1) == '*') ? true : false;
					if (ClientManager.sendMessage(message, room.getID(), priv))
						room.displayMessage(ClientManager.getUserID(),
								ClientManager.getUserName(), message);

					messageBox.setText("");
				}
			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					messageBox.setText("");
				}
			}

		};
		messageBox.addKeyListener(theEnterListener);

		ActionListener pRoomList = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				GUIManager.showPublicRooms();
			}
		};
		menuPRoomsList.addActionListener(pRoomList);

		ActionListener inviteListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (onLineUsers.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(ChatWindow.this,
					"Who are you inviting ?!!");
					return;
				}
				int roomID = ((DisplayBox) ((JScrollPane) tabbedPane
						.getSelectedComponent()).getViewport().getView())
						.getID();
				ClientManager.sendInvitationRequest(
						(String) onLineUsers.getSelectedValue(), roomID);

			}
		};
		menuInvite.addActionListener(inviteListener);

		ActionListener sendFileListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (onLineUsers.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(ChatWindow.this,
					"Sending to whom ?!!");
					return;
				}

				if (onLineUsers.getSelectedValue().equals(
						ClientManager.getUserName())) {
					JOptionPane.showMessageDialog(ChatWindow.this,
					"Sending to yourself ?!!");
					return;
				}

				String[] possibleValues = { "over TCP", "over UDP" };
				String selected = (String) JOptionPane.showInputDialog(null,
						"Please choose a protocol for the transfer:",
						"Method:", JOptionPane.INFORMATION_MESSAGE, null,
						possibleValues, possibleValues[0]);

				JFileChooser fc = new JFileChooser();
				fc.setApproveButtonText("Send");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fc.showOpenDialog(ChatWindow.this) == JFileChooser.APPROVE_OPTION) {
					String filePath = fc.getSelectedFile().getAbsolutePath();
					long fileSize = fc.getSelectedFile().length();
					String userName = (String) onLineUsers.getSelectedValue();
					if (selected.equals("over TCP")) {
						ClientManager.sendFileTransferTCPRequest(userName,
								filePath);
					} else {
						ClientManager.sendFileTransferUDPRequest(userName,
								filePath, fileSize);
					}

				}
			}
		};
		sendFileMenuItem.addActionListener(sendFileListener);

	}

	private void addResizeListeners() {
		HierarchyBoundsListener resizeListener = new HierarchyBoundsAdapter() {

			public void ancestorResized(HierarchyEvent e) {
				if (e.getSource() == users)
					users.setBounds(ChatWindow.this.getWidth() - 126, 24, 100,
							ChatWindow.this.getHeight() - 94);

				// setBounds(100, 100, 770, 520);
				// sendButton.setBounds(493, 427, 113, 23);
				if (e.getSource() == desktopPane_1)
					desktopPane_1.setBounds(10,
							ChatWindow.this.getHeight() - 174,
							ChatWindow.this.getWidth() - 174, 55);
				else if (e.getSource() == desktopPane)
					desktopPane.setBounds(10, 24,
							ChatWindow.this.getWidth() - 174,
							ChatWindow.this.getHeight() - 234);
				else if (e.getSource() == lblNewLabel)
					lblNewLabel.setBounds(ChatWindow.this.getWidth() - 361,
							ChatWindow.this.getHeight() - 199, 197, 14);
				else if (e.getSource() == sendButton)
					sendButton.setBounds(ChatWindow.this.getWidth() - 277,
							ChatWindow.this.getHeight() - 93, 113, 23);

			}
		};
		users.addHierarchyBoundsListener(resizeListener);
		desktopPane_1.addHierarchyBoundsListener(resizeListener);
		desktopPane.addHierarchyBoundsListener(resizeListener);
		lblNewLabel.addHierarchyBoundsListener(resizeListener);
		sendButton.addHierarchyBoundsListener(resizeListener);
	}

	protected void createRoom(String roomName, int id, boolean priv) {
		if (priv) {
			menuInvite.setEnabled(true);
		}

		DisplayBox area = new DisplayBox();
		area.setName(roomName);
		area.setID(id);
		JScrollPane scrollPane = new JScrollPane(area);
		tabbedPane.addTab(roomName, scrollPane);
		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new RoomTab(
				tabbedPane, tabbedPane.getTabCount() - 1));
	}

	protected void showUsers(String[] userNames) {
		for (int i = 0; i < userNames.length; i++) {
			if (userNames[i] != null && i != ClientManager.getUserID())
				theModel.addElement(userNames[i]);
		}
		DisplayBox.addColors(userNames.length);
	}

	protected void addUser(String name, int length) {
		theModel.addElement(name);
		DisplayBox.addColors(length);
	}

	protected void displayMessage(int senderID, String senderName,
			String roomName, String msg) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getTitleAt(i).equals(roomName)) {
				((DisplayBox) (((JScrollPane) tabbedPane.getComponentAt(i))
						.getViewport().getView())).displayMessage(senderID,
								senderName, msg);
				break;
			}
		}
	}

	protected void removeUser(String user) {
		theModel.removeElement(user);
	}

	protected void joinRoom(String roomName, int id) {
		DisplayBox area = new DisplayBox();
		area.setName(roomName);
		area.setID(id);
		JScrollPane scrollPane = new JScrollPane(area);
		tabbedPane.addTab(roomName, scrollPane);
	}

	protected void leaveRoom(String roomName) {
		int tabsCount = tabbedPane.getTabCount();
		for (int i = 0; i < tabsCount; i++) {
			if (tabbedPane.getTitleAt(i).equals(roomName)) {
				tabbedPane.removeTabAt(i);
			}
		}
	}

	protected void closeRoom(String roomName) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getTitleAt(i).equals(roomName)) {
				if (tabbedPane.getSelectedIndex() == i)
					tabbedPane.setSelectedIndex(0);
				tabbedPane.removeTabAt(i);
				break;
			}
		}
	}

	protected int getSelectedRoomId() {
		DisplayBox room = (DisplayBox) ((JScrollPane) tabbedPane
				.getSelectedComponent()).getViewport().getView();
		return room.getID();
	}

	protected boolean selectedRoomPriv() {
		DisplayBox room = (DisplayBox) ((JScrollPane) tabbedPane
				.getSelectedComponent()).getViewport().getView();
		String name = room.getName();
		int n = name.length();
		boolean isPriv;
		if (name.charAt(n - 1) == '*') {
			isPriv = true;
		} else {
			isPriv = false;
		}
		return isPriv;
	}
}

@SuppressWarnings("serial")
class RoomTab extends JPanel {
	private final JTabbedPane tabbedPane;
	private final OptionsMenu menu;
	private final int index;

	public RoomTab(JTabbedPane tabbedPane, int index) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.tabbedPane = tabbedPane;
		this.index = index;

		setOpaque(false);

		JLabel title = new JLabel() {

			public String getText() {
				return RoomTab.this.tabbedPane.getTitleAt(RoomTab.this.index);
			}
		};
		add(title);
		add(new TabButton());

		menu = new OptionsMenu();
	}

	private class TabButton extends JButton implements ActionListener {

		public TabButton() {
			setPreferredSize(new Dimension(25, 25));
			setBorderPainted(false);
			setContentAreaFilled(false);
			addActionListener(this);
			setRolloverEnabled(true);
			setToolTipText("Options");
			setIcon(new ImageIcon(
					ChatWindow.class.getResource("/icons/options_menu.png")));
		}

		public void actionPerformed(ActionEvent e) {
			menu.show(TabButton.this, getWidth() - 10, getHeight() - 10);
		}

	}

	private class OptionsMenu extends JPopupMenu implements ActionListener {
		JMenuItem menuItemMute;
		JMenuItem menuItemClose;
		JMenuItem menuItemKick;

		public OptionsMenu() {
			menuItemMute = new JMenuItem("Mute user", new ImageIcon(
					OptionsMenu.class.getResource("/icons/user_mute.png")));
			menuItemKick = new JMenuItem("Remove user", new ImageIcon(
					OptionsMenu.class.getResource("/icons/user_remove.png")));
			menuItemClose = new JMenuItem("Close room", new ImageIcon(
					OptionsMenu.class.getResource("/icons/close_room.png")));
			add(menuItemMute);
			add(menuItemKick);
			add(new JSeparator());
			add(menuItemClose);

			menuItemClose.addActionListener(this);
			menuItemKick.addActionListener(this);
			menuItemMute.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			RoomTab.this.tabbedPane.setSelectedIndex(RoomTab.this.index);
			if (e.getSource() == menuItemMute) {
				boolean priv = (tabbedPane.getTitleAt(index).charAt(
						tabbedPane.getTitleAt(index).length() - 1) == '*') ? true
								: false;
				int room = ((DisplayBox) (((JScrollPane) tabbedPane
						.getComponentAt(index)).getViewport().getView()))
						.getID();
				GUIManager.showRoomMembers(room, priv);
			} else if (e.getSource() == menuItemKick) {
				boolean priv = (tabbedPane.getTitleAt(index).charAt(
						tabbedPane.getTitleAt(index).length() - 1) == '*') ? true
								: false;
				int room = ((DisplayBox) (((JScrollPane) tabbedPane
						.getComponentAt(index)).getViewport().getView()))
						.getID();
				GUIManager.showRoomMembers(room, priv);
			}

			else if (e.getSource() == menuItemClose) {
				boolean priv = (tabbedPane.getTitleAt(index).charAt(
						tabbedPane.getTitleAt(index).length() - 1) == '*') ? true
								: false;
				int room = ((DisplayBox) (((JScrollPane) tabbedPane
						.getComponentAt(index)).getViewport().getView()))
						.getID();
				ClientManager.sendCloseRoomRequest(room, priv);
				tabbedPane.remove(index);
			}
		}
	}

}
