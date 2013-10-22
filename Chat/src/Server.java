import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import java.awt.List;

public class Server {
	
	// gui assets
	
	private JFrame frame;
	private JPanel panel;
	private JTextArea conversationArea;
	private List usersList;
	
	public Server() {
		frame = new JFrame();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Chat Server GUI");
		frame.setSize(600, 500);
		
		panel = new JPanel();
		frame.getContentPane().add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JLabel label1 = new JLabel("Connected Users");
		sl_panel.putConstraint(SpringLayout.NORTH, label1, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, label1, 10, SpringLayout.WEST, panel);
		panel.add(label1);
		
		JLabel label2 = new JLabel("Conversations");
		sl_panel.putConstraint(SpringLayout.NORTH, label2, 0, SpringLayout.NORTH, label1);
		panel.add(label2);
		
		conversationArea = new JTextArea();
		sl_panel.putConstraint(SpringLayout.NORTH, conversationArea, 6, SpringLayout.SOUTH, label2);
		sl_panel.putConstraint(SpringLayout.WEST, conversationArea, 145, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, conversationArea, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, conversationArea, -10, SpringLayout.EAST, panel);
		sl_panel.putConstraint(SpringLayout.WEST, label2, 0, SpringLayout.WEST, conversationArea);
		panel.add(conversationArea);
		
		usersList = new List();
		sl_panel.putConstraint(SpringLayout.NORTH, usersList, 6, SpringLayout.SOUTH, label1);
		sl_panel.putConstraint(SpringLayout.WEST, usersList, 0, SpringLayout.WEST, label1);
		sl_panel.putConstraint(SpringLayout.SOUTH, usersList, 0, SpringLayout.SOUTH, conversationArea);
		sl_panel.putConstraint(SpringLayout.EAST, usersList, -6, SpringLayout.WEST, conversationArea);
		panel.add(usersList);
	
		frame.setVisible(true);
	}
	
	// server assets
	protected ArrayList<String> connectedClients;
	protected ArrayList<PrintWriter> clientOutputStreams;
	protected Socket socket;
	protected ServerSocket serverSocket;
	protected BufferedReader reader;	
	
	// ----- THREAD RUN CODE -----
	public class ClientHandler implements Runnable {
		
		public ClientHandler(Socket clientSocket) {
			try {
				socket = clientSocket;
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			String message = null, guestUser = null;
			try {		
				// receives the client's user name
				 guestUser = reader.readLine();
				 System.out.println("Welcome " + guestUser);
				 conversationArea.append("\"" + guestUser + "\" has just connected\n");
				 
				 if (connectedClients.contains(guestUser)) {
					 conversationArea.append("Username "+ guestUser +" already exists, removing user!\n");
					 System.out.println("Username already exists, removing user");
					 removeUser(guestUser);
					 socket.close();
				 } else {
					 connectedClients.add(guestUser);
				 }
				 
				 System.out.println(connectedClients);
				 usersList.add(guestUser);
			
				 // the conversation loop
				while ((message = reader.readLine())!=null) {
					conversationArea.append(guestUser + ": " + message + "\n");
					System.out.println("Read: " + message);
					tellEveryone(message);
				}
				removeUser(guestUser);
			} catch (IOException e) {
				System.err.println("Unable to read from Client");
				try {
					socket.close();
				} catch (IOException e1) {
					System.out.println("Could not close client socket");
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			
		}		
	}
	// ---------------------------
	
	public void go() {
		connectedClients = new ArrayList<String>();
		clientOutputStreams = new ArrayList<PrintWriter>();
		try {
			serverSocket = new ServerSocket(5000);
			
			while (true) {
				System.out.println("Listening for connection attempts..");
				// accepts a connection from a client
				Socket clientSocket = serverSocket.accept();
				System.out.println("Accepted");
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				
				System.out.println("Got a Connection!\nConnected client names: " + connectedClients);
			}			
			
		} catch (Exception e) {
			System.err.println("Unable to Connect!");
			e.printStackTrace();
		}
	}
	
	public void tellEveryone(String message) {
		Iterator iter = clientOutputStreams.iterator();
		while(iter.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) iter.next();
				writer.println("Tells: " + message);
				writer.flush();
			} catch (Exception e) {
				System.err.println("Unable to send message!");
				e.printStackTrace();
			}
		}
	}
	
	public void removeUser(String name) {
		connectedClients.remove(name);
		System.out.println(connectedClients);
		usersList.remove(name);
		usersList.repaint();
	}
	
	public boolean doesUserExists(String name) {		
		if (connectedClients.contains(name)) return true;
		else return false;
	}
	
	public static void main(String[] args) {
		System.out.println("Chat Server now running..");
		new Server().go();
	}
}
