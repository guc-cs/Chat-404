package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import engine.client.ClientManager;

@SuppressWarnings("serial")
public class MembersKickPanel extends JPanel {

	private DefaultListModel model;
	private JList list;
	private static JFrame frame;
	private JPanel panel_1;
	private JButton kickButton;
	private JButton muteButton;
	private JButton UnmuteButton;

	// TODO complete class
	// TODO add Listeners

	public MembersKickPanel(String[] roomMembersNames) {
		setBackground(new Color(0, 0, 139));
		setLayout(new BorderLayout());
		model = new DefaultListModel();
		list = new JList(model);
		model.clear();
		for (int i = 0; i < roomMembersNames.length; i++) {
			if (roomMembersNames[i] != null) {
				System.out.println(roomMembersNames[i]);
				model.add(i, roomMembersNames[i]);
			}
		}
		list.setModel(model);
		list.setForeground(new Color(0, 0, 139));
		list.setBackground(Color.WHITE);
		list.setVisible(true);
		JScrollPane pane = new JScrollPane(list);

		pane.setPreferredSize(new Dimension(100, 230));

		this.add(pane, BorderLayout.NORTH);

		panel_1 = new JPanel();
		add(panel_1, BorderLayout.SOUTH);

		UnmuteButton = new JButton("Un-mute User");
		panel_1.add(UnmuteButton);

		muteButton = new JButton("Mute User");
		panel_1.add(muteButton);

		kickButton = new JButton("Kick User");
		panel_1.add(kickButton);

		System.out.print(model.size());
		System.out.println("aheeeeee " + model.get(0));
		addListeners();
	}

	public static void setFrame(MembersKickPanel panel) {
		frame = new JFrame("Room Members");
		frame.setVisible(true);
		frame.getContentPane().add(panel);
		frame.setSize(380, 302);
		frame.setResizable(false);
	}

	private void addListeners() {

		ActionListener RemoveMemberReq = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()) {
					return;
				} else {
					String member = (String) list.getSelectedValue();
					int room = GUIManager.chatFrame.getSelectedRoomId();
					boolean isPriv = GUIManager.chatFrame.selectedRoomPriv();
					ClientManager.sendRemoveMemberRequest(room, isPriv, member);
				}
			}

		};
		kickButton.addActionListener(RemoveMemberReq);

		ActionListener MuteMemberReq = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()) {
					return;
				} else {
					String member = (String) list.getSelectedValue();
					int room = GUIManager.chatFrame.getSelectedRoomId();
					boolean isPriv = GUIManager.chatFrame.selectedRoomPriv();
					ClientManager.sendMuteMemberRequest(room, isPriv, member);
				}
			}

		};
		muteButton.addActionListener(MuteMemberReq);

		ActionListener UnMuteMemberReq = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()) {
					return;
				} else {
					// String member = (String) list.getSelectedValue();
					// System.out.println("Member Selected"+ member);
					String member = (String) list.getSelectedValue();
					// Like this
					int room = GUIManager.chatFrame.getSelectedRoomId();
					boolean isPriv = GUIManager.chatFrame.selectedRoomPriv();
					ClientManager.sendUnMuteMemberRequest(room, isPriv, member);
				}
			}

		};
		UnmuteButton.addActionListener(UnMuteMemberReq);
	}

}
