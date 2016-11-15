
import javax.mail.Message;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pop3Proxy {


    private final String user;
    private final String pass;
    private final int port;
    private ArrayList<Message> allMsgs = new ArrayList<>();
    private MailBox mailBox;
    private ServerSocket server;
    private boolean serverAlive;
    private List<ProxyHelper> listenerSockets;
    private final int MAX_CLIENTS;

    public Pop3Proxy(String user, String pass, int port) {
        this.user = user;
        this.pass = pass;
        this.port = port;
        mailBox = new MailBox();
        allMsgs = mailBox.getAllMails();
        MAX_CLIENTS = 3;
        startServer();

    }

    public void startServer() {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not connect to Serversocket");
        }
        serverAlive = true;
        while (serverAlive) {
            if (listenerSockets.size() < MAX_CLIENTS) {
                try {
                    Socket clientS = server.accept();
                    ProxyHelper clientListener;
                    clientListener = new ProxyHelper(clientS);
                    clientListener.start();
                    listenerSockets.add(clientListener);

                } catch (IOException e) {
                    System.err.println("Could not create Socket to listen for client");
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class ProxyHelper extends Thread {
        private Socket clientSocket = null;
        private InputStream inputStream;
        private InputStreamReader inputStreamReader;
        private BufferedReader buffereReader;

        private OutputStream outputStream;
        private OutputStreamWriter outputStreamWriter;

        private boolean alive = true;
        private boolean transactionState = false;

        public ProxyHelper(Socket socket) {
            super("ProxyHelper");
            clientSocket = socket;
        }

        public void run() {
            try {
                clientSocket.setKeepAlive(true);
            } catch (SocketException e) {
                System.err.println("Could not set to keep the socket alive");
            }

            try {
                inputStream = clientSocket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                buffereReader = new BufferedReader(inputStreamReader);

                outputStream = clientSocket.getOutputStream();
                outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                write("+OK POP3 server ready");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void authenticate() {
            try {
                String input = buffereReader.readLine();

                if (input.contains("CAPA") || input.contains("AUTH")) {
                    capa();
                } else if (input.contains("USER")){
                    String[] userName = input.split(" ");
                    if (userName[1].equals(user)) {
                        write("+OK Please enter password");

                        input = buffereReader.readLine();
                        String[] password = input.split(" ");
                        if (password[1].equals(pass)) {
                            write("+OK mailbox locked and ready");
                        } else {
                            write("-ERR Wrong Password");
                        }
                    } else {
                        write("-ERR wrong username");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void capa() {
            write("-ERR");
        }


        private synchronized void transaction() {


            while(alive) {
                try {
                    String inputLine = buffereReader.readLine();
                    String[] inputArray = inputLine.split(" ");

                    switch (inputArray[0]) {
                        case "USER":
                            break;
                        case "Hey":
                    }

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        private void write(String line) {
            line = line + "\r\n";
            try {
                outputStreamWriter.write(line);
                outputStreamWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
