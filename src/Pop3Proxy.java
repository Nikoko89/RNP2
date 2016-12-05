import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Pop3Proxy {


    private final String user;
    private final String pass;
    private final int port;
    private MessageStore allMSG;
    private ServerSocket server;
    private boolean serverAlive;
    private List<ProxyHelper> listenerSockets;
    private final int MAX_CLIENTS;

    public Pop3Proxy(MessageStore allMSG) {
        this.allMSG = allMSG;
        PropReader prop = new PropReader("pop3.properties");
        Properties pop3 = prop.getProp();
        this.user = pop3.getProperty("user");
        this.pass = pop3.getProperty("password");
        this.port = Integer.valueOf(pop3.getProperty("port"));
        MAX_CLIENTS = 3;
        listenerSockets = new ArrayList<>();

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

    public synchronized void remove() {
        for (int i = 0; i < listenerSockets.size(); i++) {
            if (listenerSockets.get(i).alive == false) {
                listenerSockets.remove(i);
                System.out.println("A client sad BYE and left");
                System.out.println("Clients left: " + listenerSockets.size());
            }
        }
    }

    public class ProxyHelper extends Thread {
        private Socket clientSocket;
        private InputStream inputStream;
        private InputStreamReader inputStreamReader;
        private BufferedReader bufferedReader;

        private OutputStream outputStream;
        private OutputStreamWriter outputStreamWriter;

        private boolean alive = true;
        private boolean transactionState = false;

        public ProxyHelper(Socket socket) {
            super("ProxyHelper");
            clientSocket = socket;
            try {
                clientSocket.setKeepAlive(true);
            } catch (SocketException e) {
                System.err.println("Could not set to keep the socket alive");
            }
        }

        public void run() {
            try {
                System.out.println("Bin jetzt vorm stream");
                inputStream = clientSocket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                bufferedReader = new BufferedReader(inputStreamReader);

                outputStream = clientSocket.getOutputStream();
                outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                write("+OK POP3 server ready");
                System.out.println("kurz vorm auth");
                authenticate();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void authenticate() {
            try {
                String input = bufferedReader.readLine();
                if (input.contains("CAPA") || input.contains("AUTH")) {
                    capa();
                    authenticate();
                } else if (input.contains("USER")) {

                    String[] userName = input.split(" ");
                    if (userName[1].equals(user)) {
                        write("+OK Please enter password");

                        input = bufferedReader.readLine();
                        String[] password = input.split(" ");
                        if (password[1].equals(pass)) {
                            write("+OK mailbox locked and ready");
                            transaction();
                        } else {

                            write("-ERR Wrong Password");
                        }
                    } else {
                        write("-ERR wrong username");
                    }

                } else if (input.contains("QUIT")) {
                    write("+OK dewey POP3 server signing off");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void capa() {
            write("-ERR");
        }


        private synchronized void transaction() {

            while (alive) {
                System.out.println(allMSG.getMessages().size() + " Server");
                try {
                    String inputLine = bufferedReader.readLine();
                    //System.out.print(inputLine);
                    if (inputLine == null) {

                        break;
                    }
                    String[] inputArray = inputLine.split(" ");
                    int octetSize = 0;
                    int messageAmount = 0;
                    for (MailObject msg : allMSG.getMessages()) {
                        try {
                            if (!msg.getDeleteFlag()) {
                                octetSize += msg.getFileSize();
                                messageAmount++;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    int cursor;
                    System.out.println(Arrays.toString(inputArray));
                    switch (inputArray[0]) {

                        case "STAT":
                            write("+OK " + messageAmount + " " + octetSize);
                            break;

                        case "UIDL":
                            if (inputArray.length == 1) {
                                write("+OK");
                                for (cursor = 0; cursor < messageAmount; cursor++) {
                                    System.out.println((cursor + 1) + " " + allMSG.getMessages().get(cursor).getID());
                                    write((cursor + 1) + " " + allMSG.getMessages().get(cursor).getID());
                                }
                                write(".");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMSG.getMessages().get(cursor - 1) == null || allMSG.getMessages().get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                }
                                write("+OK " + cursor + " " + allMSG.getMessages().get(cursor - 1).getID());
                            }
                            break;

                        case "LIST":

                            if (inputArray.length == 1) {
                                write("+OK " + messageAmount + " messages (" + octetSize + " octets)");

                                for (cursor = 0; cursor < messageAmount; cursor++) {
                                    write("" + (cursor + 1) + " " + allMSG.getMessages().get(cursor).getFileSize());
                                }
                                write(".");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMSG.getMessages().get(cursor - 1) == null || allMSG.getMessages().get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                }
                                write("+OK " + cursor + " " + allMSG.getMessages().get(cursor - 1).getFileSize());
                            }
                            break;

                        case "RETR":
                            if (inputArray.length == 1) {
                                write("-ERR pls enter message");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMSG.getMessages().get(cursor - 1) == null || allMSG.getMessages().get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                } else {
                                    write("+OK " + allMSG.getMessages().get(cursor - 1).getFileSize() + " octets");
                                    write("From: " + "" + allMSG.getMessages().get(cursor - 1).getFrom());
                                    write("Date: " + "" + allMSG.getMessages().get(cursor - 1).getDate());
                                    write("Message-ID: " + "" + allMSG.getMessages().get(cursor - 1).getID());
                                    write("Subject: " + "" + allMSG.getMessages().get(cursor - 1).getSubject());
                                    write("To: " + "" + allMSG.getMessages().get(cursor - 1).getTo());
                                    write("");
                                    write(allMSG.getMessages().get(cursor - 1).getContent());
                                    write(".");
                                }

                            }
                            break;

                        case "DELE":
                            if (inputArray.length == 1) {
                                write("-ERR pls enter message");
                            } else {
                                cursor = Integer.valueOf(inputArray[1]);
                                if (allMSG.getMessages().get(cursor - 1) == null) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                } else if (allMSG.getMessages().get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR message " + cursor + " already deleted");
                                } else {
                                    allMSG.getMessages().get(cursor - 1).setDeleteFlag(true);
                                    write("+OK message " + cursor + " deleted");
//                                    allMsgs.sort((m1, m2) -> {
//                                        int result = 0;
//                                        if (m2.getDeleteFlag()) {
//                                            result = -1;
//                                        }
//                                        return result;
//                                    });
                                }
                            }
                            break;

                        case "NOOP":
                            write("+OK");
                            break;

                        case "RSET":
                            for (MailObject msg : allMSG.getMessages()) {
                                if (msg.getDeleteFlag()) {
                                    msg.setDeleteFlag(false);
                                }
                            }
                            write("+OK maildrop has " + allMSG.getMessages().size() + " messages");
                            break;

                        case "QUIT":
                            alive = false;
                            update();
                            break;

                        default:
                            write("-ERR command not found: " + inputArray[0]);
                    }

                } catch (Exception e) {
                    System.err.println("Client disconnected itself");
                    try {
                        clientSocket.setKeepAlive(false);
                        remove();
                        try {
                            bufferedReader.close();
                            inputStream.close();
                            inputStreamReader.close();
                            outputStream.close();
                            outputStreamWriter.close();
                            clientSocket.close();
                        } catch (IOException e1) {
                            System.err.println("Could not close Stream");
                        }

                    } catch (SocketException e1) {
                        System.err.println("Could not set to keep the socket not alive");
                    }

                }
            }
        }

        private void update() {
            for (int i = 0; i < allMSG.getMessages().size(); i++) {
                if (allMSG.getMessages().get(i).getDeleteFlag()) {
                    allMSG.getMessages().remove(i);
                }
            }
            if (allMSG.getMessages().isEmpty()) {
                write("+OK dewey POP3 server signing off (maildrop empty)");
            } else {
                write("+OK dewey POP3 server signing off (" + allMSG.getMessages().size() + " messages left)");
            }
            try {
                clientSocket.setKeepAlive(false);
                remove();
                bufferedReader.close();
                inputStream.close();
                inputStreamReader.close();
                outputStream.close();
                outputStreamWriter.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void write(String line) {
            line = line + "\r\n";
            try {
                System.out.println(line);
                outputStreamWriter.write(line);
                outputStreamWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}