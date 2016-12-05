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
    private List<MailObject> allMsgs = new ArrayList<>();
    private ServerSocket server;
    private boolean serverAlive;
    private List<ProxyHelper> listenerSockets;
    private final int MAX_CLIENTS;
    private Pop3ProxyClient prox;

    public Pop3Proxy(Pop3ProxyClient prox) {
        this.prox = prox;
        PropReader prop = new PropReader("pop3.properties");
        Properties pop3 = prop.getProp();
        this.user = pop3.getProperty("user");
        this.pass = pop3.getProperty("password");
        this.port = Integer.valueOf(pop3.getProperty("port"));
        allMsgs = prox.getNewMails();

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
                    System.out.println(allMsgs.size() + "hallo");
                    Socket clientS = server.accept();
                    System.out.println("hallsasasssssssssssssaxccccvvvvvvvvvvvvvvvvp");
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
                try {
                    String inputLine = bufferedReader.readLine();
                    System.out.print(inputLine);
                    if(inputLine == null){

                        break;
                    }
                    String[] inputArray = inputLine.split(" ");
                    int octetSize = 0;
                    int messageAmount = 0;
                    for (MailObject msg : allMsgs) {
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

                        case "LIST":

                            if (inputArray.length == 1) {
                                write("+OK " + messageAmount + " messages (" + octetSize + " octets)");

                                for (cursor = 0; cursor < messageAmount; cursor++) {
                                    write("" + (cursor + 1) + " " + allMsgs.get(cursor).getFileSize());
                                }
                                write(".");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMsgs.get(cursor - 1) == null || allMsgs.get(cursor-1).getDeleteFlag()) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                }
                                write("+OK " + cursor + " " + allMsgs.get(cursor - 1).getFileSize());
                            }
                            break;

                        case "RETR":
                            if (inputArray.length == 1) {
                                write("-ERR pls enter message");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMsgs.get(cursor - 1) == null || allMsgs.get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                } else {
                                    write("+OK " + allMsgs.get(cursor - 1).getFileSize() + " octets");
                                    write(allMsgs.get(cursor - 1).getContent());
                                }
                            }
                            break;

                        case "DELE":
                            if (inputArray.length == 1) {
                                write("-ERR pls enter message");
                            } else {
                                cursor = Integer.parseInt(inputArray[1]);
                                if (allMsgs.get(cursor - 1) == null) {
                                    write("-ERR no such message, only " + messageAmount + " messages in maildrop");
                                } else if (allMsgs.get(cursor - 1).getDeleteFlag()) {
                                    write("-ERR message " + cursor + " already deleted");
                                } else {
                                    allMsgs.get(cursor - 1).setDeleteFlag(false);
                                    write("+OK message " + cursor + " deleted");
                                    allMsgs.sort((m1, m2) -> {
                                        int result = 0;
                                        if (m2.getDeleteFlag()) {
                                            result = -1;
                                        }
                                        return result;
                                    });
                                }
                            }
                            break;

                        case "NOOP":
                            write("+OK");
                            break;

                        case "RSET":
                            for (MailObject msg : allMsgs) {
                                if (msg.getDeleteFlag()) {
                                    msg.setDeleteFlag(false);
                                }
                            }
                            write("+OK maildrop has " + allMsgs.size() + " messages");
                            break;

                        case "QUIT":
                            alive = false;

                            update();
                            break;

                        default:
                            write("-ERR command not found: " + inputArray[0]);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private synchronized void update() {
            for (int i = 0; i < allMsgs.size(); i++) {
                if (allMsgs.get(i).getDeleteFlag()) {
                    allMsgs.remove(i);
                }
            }

            if (allMsgs.isEmpty()) {
                write("+OK dewey POP3 server signing off (maildrop empty)");
            } else {
                write("+OK dewey POP3 server signing off (" + allMsgs.size() + " messages left)");
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

        public void setAllMsg(List<MailObject> newMsg){
            allMsgs = newMsg;
        }
    }
}