import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Client{
	public Client() {
	}
	// GUI
	private JFrame frame;
	private JPanel panel;
	private JTextArea textArea;
	private JTextField textField;
	private JButton connectButton, sendButton;
	
	// Networking Assets
	protected BufferedReader reader;
	protected PrintWriter writer;
	protected Socket socket;
	protected Thread readerThread;
	protected String userName;
	
	private boolean isConnected;

	public void go() {	
		frame = new JFrame("Simple Chat Client");
		// will kill all client processes when closing the window
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		textArea = new JTextArea(18, 40);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane jScroller = new JScrollPane(textArea);
		jScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		textField = new JTextField(20);
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		sendButton.addActionListener(new SendButtonListener());

		frame.getContentPane().add(BorderLayout.CENTER, panel);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectButtonListener());
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(textField)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(sendButton)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(connectButton))
						.addComponent(jScroller, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(11, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(5)
					.addComponent(jScroller, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(connectButton)
						.addComponent(sendButton)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		panel.setLayout(gl_panel);
		frame.setSize(380, 430);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void setupNetworking() {
		try {
			try {	
				socket = new Socket("127.0.0.1", 5000); // local host, port 5000
				
			} catch (ConnectException ex) {			
				System.out.println("Unable to connect to server, make sure the server is up!");
				textArea.append("Unable to connect to server, Please try again later\n");
			}
			
		InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
		this.reader = new BufferedReader(streamReader);
		this.writer = new PrintWriter(socket.getOutputStream());
		System.out.println("Connection Established");
						
		} catch (IOException e) {
			System.err.println("Unable to connect to Server!");
			e.printStackTrace();
		}
	}
	
	// listener for connect button
	public class ConnectButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// request user name
			if (userName==null) {
				userName = (String) JOptionPane.showInputDialog(frame, "Please enter your name", "Prompt", JOptionPane.PLAIN_MESSAGE, null, null, null);
				frame.setTitle(frame.getTitle() + ", Username \"" + userName + "\" ");
			}
			readerThread = new Thread(new IncomingReader(userName));
			setupNetworking();
			if (!isConnected) {			
				if (userName!=null) {
					textArea.append("\"" + userName + "\" is connecting ... ");
					

					// send to server the use name for this client
					writer.println(userName);
					writer.flush();
					
					readerThread.start();					
					isConnected = true;
					sendButton.setEnabled(true);
					textArea.append("CONNECTED!\n");
					connectButton.setText("Disconnect");
				} else {
					textArea.append("Client: No name entered!\n");
				}
			} else { 
				try {
					readerThread.join();
					isConnected = false;
					sendButton.setEnabled(false);
					textArea.append("DISCONNECTED!\n");
					connectButton.setText("Connect");
					
				} catch (Exception ex) {
					System.err.println("Could not properly disconnect, check stack trace for details!");
					ex.printStackTrace();
				}
			}
		}	
	}

	// listener for send button
	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				if (textField.getText().length()>0) {
					writer.println(textField.getText());
					writer.flush();
					
					textField.setText("");
					textField.requestFocus();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// ----- THREAD RUN CODE -----

	public class IncomingReader implements Runnable {
		
		private String userName;
		
		public IncomingReader(String userName) {
			this.userName = userName;
		}
		
		@Override
		public void run() {
			String message = null;
			try {
				// the conversation loop
				while ((message = reader.readLine()) != null) {
					System.out.println("Received: " + message);
					textArea.append(message + "\n");
				}
			} catch (IOException e) {
				System.out.println("Unable to read from Server");
				e.printStackTrace();
			}
		}
	}
	
	// ----------------------------
	
	public static void main(String[] args) {
		// attempt windows form look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		// start the client
		new Client().go();
	}
}
