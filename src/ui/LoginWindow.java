package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import engine.client.ClientManager;

@SuppressWarnings("serial")
public class LoginWindow extends JFrame {

	private JPanel contentPane;
	private JPasswordField passwordField;
	private JTextField nameField;
	private JButton connectButton;

	public LoginWindow() {
		setIconImage(new ImageIcon(LoginWindow.class.getResource("/icons/chatting_online.png")).getImage());
		setTitle("Start Chatting...");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 457, 382);
		contentPane = new JPanel();
		contentPane.setOpaque(false);
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBounds(504, 95, 1, 1);
		desktopPane.setOpaque(false);

		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setBounds(49, 36, 1, 1);

		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				new ImageIcon(LoginWindow.class.getResource("/backgrounds/p1.jpg")).paintIcon(this, g, 0, 0);
				g.setColor(Color.WHITE);
				g.setFont(new Font("Tahoma", Font.BOLD, 12));
				g.drawString("Username", 192, 218);
				g.setColor(Color.WHITE);
				g.setFont(new Font("Tahoma", Font.BOLD, 12));
				g.drawString("Password", 192, 253);
				g.setColor(Color.WHITE);
				g.setFont(new Font("Tahoma", Font.BOLD, 12));
				g.drawString("Don't have an account?!!", 270, 145);
				this.paintChildren(g);
			}
		};
		panel.setBounds(0, 0, 451, 354);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton btnNewButton_1 = new JButton("sign up");

		nameField = new JTextField();
		nameField.setColumns(10);
		
		passwordField = new JPasswordField();

		JCheckBox chckbxNewCheckBox = new JCheckBox("remember me");
		chckbxNewCheckBox.setFont(new Font("Tahoma", Font.BOLD, 11));
		chckbxNewCheckBox.setForeground(new Color(255, 255, 255));
		chckbxNewCheckBox.setBackground(new Color(0, 0, 102));

		connectButton = new JButton("Connect");

		connectButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel
				.createParallelGroup(Alignment.TRAILING)
				.addGroup(
						gl_panel.createSequentialGroup()
								.addContainerGap(282, Short.MAX_VALUE)
								.addComponent(connectButton).addContainerGap())
				.addGroup(
						gl_panel.createSequentialGroup()
								.addContainerGap(304, Short.MAX_VALUE)
								.addComponent(btnNewButton_1,
										GroupLayout.PREFERRED_SIZE, 85,
										GroupLayout.PREFERRED_SIZE).addGap(51))
				.addGroup(
						gl_panel.createSequentialGroup()
								.addGap(255)
								.addGroup(
										gl_panel.createParallelGroup(
												Alignment.LEADING)
												.addComponent(chckbxNewCheckBox)
												.addComponent(
														nameField,
														GroupLayout.DEFAULT_SIZE,
														179, Short.MAX_VALUE)
												.addComponent(
														passwordField,
														Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE,
														179, Short.MAX_VALUE))
								.addContainerGap()));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(
				Alignment.TRAILING).addGroup(
				gl_panel.createSequentialGroup()
						.addContainerGap(155, Short.MAX_VALUE)
						.addComponent(btnNewButton_1)
						.addGap(18)
						.addComponent(nameField, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(passwordField,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chckbxNewCheckBox).addGap(18)
						.addComponent(connectButton).addContainerGap()));
		panel.setLayout(gl_panel);
		contentPane.setLayout(null);
		contentPane.add(layeredPane);
		contentPane.add(panel);
		contentPane.add(desktopPane);
		
		addListeners();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		nameField.requestFocusInWindow();
	}

	private void addListeners() {

		Action connectListener = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (nameField.getText().equals("")) {
					JOptionPane.showMessageDialog(LoginWindow.this,
							"Username can not be empty !");
					return;
				} else {
					if (!ClientManager.connect(nameField.getText()))
						JOptionPane.showMessageDialog(LoginWindow.this, "Server is down!");
				}

			}
		};
		InputMap loginInputMap = connectButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap loginActionMap = connectButton.getActionMap();
		loginInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "connect");
		loginActionMap.put("connect", connectListener);
		connectButton.addActionListener(connectListener);
		
		

	}
	
	public String getUserName(){
		return this.nameField.getText();
	}

}