package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class ListPanel extends JPanel {

	private DefaultListModel model;
	private JList list;
	private JButton joinButton;
	private JButton leaveButton;

	public ListPanel(String[] publicRoomNames) {
		setBackground(new Color(0, 0, 139));
		setLayout(new BorderLayout());
		model = new DefaultListModel();
		list = new JList(model);
		list.setForeground(new Color(0, 0, 139));
		JScrollPane pane = new JScrollPane(list);
		joinButton = new JButton("Join Room ");
		joinButton.setBorder(new LineBorder(new Color(0, 0, 0)));
		joinButton.setForeground(Color.WHITE);
		joinButton.setBackground(new Color(0, 0, 139));
		leaveButton = new JButton("Leave Room");
		leaveButton.setForeground(Color.WHITE);
		leaveButton.setBackground(new Color(0, 0, 139));
		pane.setPreferredSize(new Dimension(100, 230));
		joinButton.setPreferredSize(new Dimension(140, 0));
		leaveButton.setPreferredSize(new Dimension(140, 0));

		add(pane, BorderLayout.NORTH);
		add(joinButton, BorderLayout.CENTER);
		add(leaveButton, BorderLayout.EAST);

		for (int i = 0; i < publicRoomNames.length; i++) {
			if (publicRoomNames[i] != null && !publicRoomNames[i].equals("Lobby"))
				model.addElement(publicRoomNames[i]);
		}

		addListeners();
	}

	private void addListeners() {
		
		ActionListener joinRoomReq = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()
						|| list.getSelectedValue()
								.equals("Lobby"))
					return;

				GUIManager.joinRoom((String)list.getSelectedValue());

			}

		};
		joinButton.addActionListener(joinRoomReq);
		
		ActionListener leaveRoomReq = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.isSelectionEmpty()
						|| list.getSelectedValue()
								.equals("Lobby"))
					return;

				GUIManager.leaveRoom(list.getSelectedValue().toString());

			}

		};
		leaveButton.addActionListener(leaveRoomReq);
	
	}
}
