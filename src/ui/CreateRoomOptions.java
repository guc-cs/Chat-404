package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import engine.client.ClientManager;

@SuppressWarnings("serial")
public class CreateRoomOptions extends JFrame {

	private JPanel contentPane;
	private JTextField roomField;
	private JRadioButton publicRoom;
	private JRadioButton privateRoom;
	private JCheckBox multicast;
	private JButton createButton;
	private ButtonGroup roomType;

	public CreateRoomOptions() {
		setTitle("Room information");
		setBounds(100, 100, 424, 240);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 139));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		roomField = new JTextField();
		roomField.setBounds(137, 41, 231, 31);
		contentPane.add(roomField);
		roomField.setColumns(10);

		JLabel lblRoomName = new JLabel("Room name: ");
		lblRoomName.setForeground(Color.WHITE);
		lblRoomName.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblRoomName.setBounds(41, 46, 86, 14);
		contentPane.add(lblRoomName);

		JLabel lblRoomType = new JLabel("Room type:");
		lblRoomType.setForeground(Color.WHITE);
		lblRoomType.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblRoomType.setBounds(41, 100, 86, 14);
		contentPane.add(lblRoomType);
		roomType = new ButtonGroup();
		publicRoom = new JRadioButton("public");
		publicRoom.setFont(new Font("Tahoma", Font.PLAIN, 12));
		publicRoom.setForeground(Color.WHITE);
		publicRoom.setBackground(new Color(0, 0, 139));
		publicRoom.setBounds(136, 97, 109, 23);
		contentPane.add(publicRoom);
		roomType.add(publicRoom);
		publicRoom.setSelected(true);

		privateRoom = new JRadioButton("private");
		privateRoom.setFont(new Font("Tahoma", Font.PLAIN, 12));
		privateRoom.setForeground(Color.WHITE);
		privateRoom.setBackground(new Color(0, 0, 139));
		privateRoom.setBounds(136, 127, 109, 23);
		contentPane.add(privateRoom);
		roomType.add(privateRoom);

		createButton = new JButton("Create");
		createButton.setBounds(309, 168, 89, 23);
		contentPane.add(createButton);

		multicast = new JCheckBox("multicast");
		multicast.setBackground(new Color(0, 0, 139));
		multicast.setForeground(Color.WHITE);
		multicast.setBounds(137, 157, 97, 23);
		contentPane.add(multicast);

		addListeners();
		setVisible(true);
	}

	private void addListeners() {
		Action createRoom = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String roomName = roomField.getText();
				if (roomName.equals("")) {
					JOptionPane.showMessageDialog(CreateRoomOptions.this,
							"Room name can not be empty!");
					return;
				}
				if (publicRoom.isSelected()) {
					if (multicast.isSelected()) {
						if (!ClientManager.sendNewRoomRequest(roomName, false,
								true))
							JOptionPane.showMessageDialog(
									CreateRoomOptions.this,
									"Couldn't Create Public Room!");
					} else {
						if (!ClientManager.sendNewRoomRequest(roomName, false,
								false))
							JOptionPane.showMessageDialog(
									CreateRoomOptions.this,
									"Couldn't Create Public Room!");
					}
				} else {
					if (!ClientManager.sendNewRoomRequest(roomName + "*", true,
							false))
						JOptionPane.showMessageDialog(CreateRoomOptions.this,
								"Couldn't Create Private Room!");
				}
				CreateRoomOptions.this.dispose();
			}

		};
		createButton.addActionListener(createRoom);

		ActionMap aMap = createButton.getActionMap();
		InputMap iMap = createButton
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "create");
		aMap.put("create", createRoom);

	}
}
