package client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

public class Client extends JFrame {

    public static final String HOST = "localhost";
    public static final int PORT = 4122;

    private Socket userSocket;
    private JFrame jFrame;

    private ExecutorService threadService;

    private JTextField inputText;
    private JTextPane textArea;

    private PrintWriter printWriter;
    private StringBuilder messages;
    private StringBuilder list;

    private JScrollPane scrollPane;
    private JTextPane chatarea;
    private JTextPane listarea;


    public static void main(String[] args) {

        new Client();
    }

    public Client() {
        jFrame = new JFrame();
        jFrame.setTitle("CHAT");
        jFrame.setSize(new Dimension(800, 800));
        chatarea = new JTextPane();
        listarea = new JTextPane();
        chatarea.setEditable(false);
        listarea.setEditable(false);
        chatarea.setContentType("text/html");
        listarea.setContentType("text/html");
        JScrollPane scrollPaneLeft = new JScrollPane(chatarea);
        JScrollPane scrollPaneRight = new JScrollPane(listarea);
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneLeft, scrollPaneRight);
        Dimension minimumSize = new Dimension(400, 400);
        scrollPaneLeft.setMinimumSize(minimumSize);
        scrollPaneRight.setMinimumSize(minimumSize);
        jSplitPane.setResizeWeight(0.7);

        Box box = Box.createHorizontalBox();
        inputText = new JTextField();
        JButton buttonSend = new JButton("Send:");
        box.add(inputText);
        box.add(buttonSend);
        jFrame.add(box, BorderLayout.SOUTH);
        jFrame.add(jSplitPane, BorderLayout.CENTER);

       /* textArea = new JTextPane();
        textArea.setEditable(false);
        textArea.setContentType("text/html");
        textArea.setPreferredSize(new Dimension(800, 800));
        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        setTitle("CHAT");

        Box box = Box.createHorizontalBox();
        add(box, BorderLayout.SOUTH);

        inputText = new JTextField();
        JButton buttonSend = new JButton("Send:");

        box.add(inputText);
        box.add(buttonSend);*/

        ActionListener sendButtonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String str = inputText.getText();
                if (str != null && str.trim().length() > 0) {
                    sendMessage(str);
                    inputText.selectAll();
                    inputText.setText("");
                }
            }
        };

        buttonSend.addActionListener(sendButtonListener);
        inputText.addActionListener(sendButtonListener);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(true);
        jFrame.setVisible(true);

        connectToServer();


    }

    private Socket getSocket() {
        return userSocket;
    }

    private void init() {
        try {
            printWriter = new PrintWriter(getSocket().getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        messages = new StringBuilder("<html>");
        list = new StringBuilder("<html>");


    }

    private void sendMessage(String msg) {
        printWriter.println(msg);
    }

    private void connectToServer() {
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
        // getUsersList();
        readMessage();
    }

    private void addTextLine(String text) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                messages.append(text);
                messages.append("<br>");
                chatarea.setText(messages.toString());
//                textArea.setText(messages.toString());
            }

        });
    }

    private void addUser(String user) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                list.append(user);
                list.append("<br>");
                listarea.setText(list.toString());
                System.out.println(list);
            }

        });
    }

    private void readMessage() {
        Runnable runnable = () -> {        //new Runnable()

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
//					Scanner scan = new Scanner(System.in);
                while (true) {

                    String message = in.readLine();
                    if(message.contains("#usersList")){
                        String[] args = message.split(" ");
                        list.delete(0,list.length());
                        for(int i=1;i<args.length;i++){
                            addUser(args[i]);
                        }

                    }
                    else if (message.equals("#adminlogindialoginput")) {
                        inputDialog();
                        continue;
                    } else if (message != null) {
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

        };

        threadService.execute(runnable);
    }

    private void inputDialog() {

        String pass = JOptionPane.showInputDialog(jFrame, "password", "Enter the password", JOptionPane.PLAIN_MESSAGE);
        sendMessage("/login " + pass);

    }
    /*private void getUsersList() {
        Runnable runnable = () -> {        //new Runnable()

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
                while (true) {

                    String listUser = in.readLine();
                    System.out.println(list);
                    if(listUser.contains("#usersList")){
                        String[] args = listUser.split(" ");
                        list.delete(0,list.length());
                        for(int i=1;i<args.length;i++){
                            addUser(args[i]);
                        }

                    }
                    Thread.yield();
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }

        };
        threadService.execute(runnable);
    }*/

}
