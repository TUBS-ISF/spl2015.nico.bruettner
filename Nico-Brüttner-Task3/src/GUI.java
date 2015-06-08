import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyledDocument;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;


public class GUI extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JTextField textField;
	private StyledDocument doc;
	private Client main;
	private JTextArea textArea;
	private JScrollPane scrollPane;

	/**
	 * Create the frame.
	 */
	public GUI(Client main) {
		this.main = main;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		textField = new JTextField();
		contentPane.add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		textField.addActionListener(this);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
	
	public void addText(String text) {
		textArea.append(text + "\n");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String text = textField.getText();
		text = text.trim();
		if(text.isEmpty())
			return;
		
		String txt = textField.getText();
		textField.setText("");
		addText(txt);
		this.main.handleUserInput(txt);
	}

}
