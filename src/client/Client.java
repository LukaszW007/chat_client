package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class Client  extends JFrame{

	public static final String HOST = "localhost";
	public static final int PORT = 4122;

	private Socket userSocket;

	private ExecutorService threadService;
	
	private JTextField inputText;
	private JTextPane textArea;
	
	private PrintWriter printWriter;
	private StringBuilder messages;
	

	public static void main(String[] args) {

		new Client();
	}

	public Client() {
		textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.setContentType("text/html");
		textArea.setPreferredSize(new Dimension(500, 250));
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane,BorderLayout.CENTER);
		setTitle("CHAT");
		
		Box box =Box.createHorizontalBox();
		add(box,BorderLayout.SOUTH);
		
		inputText = new JTextField();
		JButton buttonSend = new JButton("Send:");
		
		box.add(inputText);
		box.add(buttonSend);
		
		ActionListener sendButtonListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String str = inputText.getText();
				if(str!=null && str.trim().length()>0){
					sendMessage(str);
					inputText.selectAll();
					inputText.setText("");
				}
			}
		};
		
		buttonSend.addActionListener(sendButtonListener);
		inputText.addActionListener(sendButtonListener);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setResizable(false);
		setVisible(true);
		
	connectToServer();
		
	
	}
	private Socket getSocket(){
		return userSocket;
	}
	
	private void init(){
		try {
			printWriter = new PrintWriter(getSocket().getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		messages = new StringBuilder("<html>");
		

	}
	
	private void sendMessage(String msg){
		printWriter.println(msg);
	}
	
	private void connectToServer(){
		try {
			userSocket = new Socket(HOST, PORT);
			//userSocket.setSoTimeout(500);
			userSocket.setTcpNoDelay(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Connection in " + HOST + ":" + PORT);

		threadService = Executors.newSingleThreadExecutor();
		init();
		readMessage();
	}
	
	private void addTextLine(String text){
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				messages.append(text);
				messages.append("<br>");
				textArea.setText(messages.toString());
			}
			
		});
	}

	private void readMessage() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
//					Scanner scan = new Scanner(System.in);
					while (true) {

						String message = in.readLine();
						if (message != null) {
							addTextLine(message);
//							System.out.println("Sever's message: " + message);
//							System.out.println("Please, write message to server");
//							String messageFromClient = scan.nextLine();
							
//							PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
//							writer.println(messageFromClient);

						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		threadService.execute(runnable);
	}
}
