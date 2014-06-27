/************************************
 * Title: 	DisplayBox
 * Date:	10.28.2012
 * Purpose: Extends JTextPane to 
 * 			display text messages
 ************************************/

package ui;

import java.awt.Color;
import java.util.Random;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class DisplayBox extends JTextPane {

	private static Color [] colors = new Color[0];
	private static Random r = new Random();
	private Style nameStyle;
	private StyledDocument doc;
	private int id;

	public DisplayBox() {
		nameStyle = addStyle(null, null);
		StyleConstants.setItalic(nameStyle, true);
		setEditable(false);

		doc = getStyledDocument();
	}
	
	public static void addColors(int length)
	{
		if (colors.length < length)
		{
			Color [] temp = new Color[length];
			for (int i = 0; i < colors.length; i++)
			{
				temp[i] = colors[i];
			}
			for (int i = colors.length; i < temp.length; i++)
			{
				temp[i] = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256));
			}
			colors = temp;
		}
	}

	protected void displayMessage(int index, String name, String message) {
		try {
			StyleConstants.setForeground(nameStyle, colors[index]);
			doc.insertString(doc.getLength(), name + ":\n", nameStyle);
			doc.insertString(doc.getLength(), "  " + message + "\n", getLogicalStyle());
		} catch (BadLocationException e) {
			System.out.println(e);
		}

	}
	
	protected void setID(int id)
	{
		this.id = id;
	}
	
	protected int getID()
	{
		return id;
	}

}
