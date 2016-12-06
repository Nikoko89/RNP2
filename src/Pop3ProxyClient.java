import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Pop3ProxyClient extends Thread {

    private MessageStore allMessages;
    private Socket clientSocket;
    private List<MailAccount> accounts;
    private OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;


    public Pop3ProxyClient(List<MailAccount> acc, MessageStore allMessages) {
        this.allMessages = allMessages;
        this.accounts = acc;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (MailAccount account : accounts) {
                        connectAccount(account);
                        serverOutput();
                        authToMailServer(account);
                        checkInbox();
                        quit();
                        closeSocket();
                    }
                } catch (IOException e) {
                    System.err.println("Could not get messages from Mailserver");
                }
            }

        }, 0, 10 * 3000);
    }

    public void connectAccount(MailAccount acc) {
        try {
            clientSocket = new Socket(acc.getHost(), acc.getPort());
            outputStream = clientSocket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            inputStream = clientSocket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authToMailServer(MailAccount acc) throws IOException {
        clientInput("USER " + acc.getUser());
        clientInput("PASS " + acc.getPassword());
    }

    private void deleteMail(int index) throws IOException {
        clientInput("DELE " + index);
    }

    private int countMails() throws IOException {
        String[] result = clientInput("STAT").split(" ");
        int totalMails = Integer.valueOf(result[1]);
        return totalMails;
    }

    private void closeSocket() throws IOException {
        clientSocket.close();
    }

    private void quit() throws IOException {
        clientInput("QUIT");
    }

    private void checkInbox() throws IOException {
        int mailboxSize = countMails();
        for (int i = 1; i <= mailboxSize; i++) {
            HashMap<String, String> message = new HashMap<>();
            String nextLine;
            String content = "";
            String[] informationArray = clientInput("RETR " + i).split(" ");
            try {
                message.put("Size", informationArray[1]);
                boolean startLine = true;
                while (!(nextLine = bufferedReader.readLine()).equals(".")) {
                    System.out.println(nextLine + "diese");
                    String msg[] = nextLine.split(": ");
                    if (msg.length > 1) {
                        message.put(msg[0], msg[1]);
                    }

                    if ((msg[0].equals("") || msg.length == 1) && startLine == false) {
                        content = content + msg[0] + "\r\n";
                    }
                    if (msg[0].equals("")){
                        startLine = false;
                    }
                }
                message.put("Content", content);
                allMessages.getMessages().add(new MailObject(message));
                deleteMail(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String clientInput(String line) throws IOException {
        line = line + "\r\n";
        outputStreamWriter.write(line);
        outputStreamWriter.flush();
        return serverOutput();
    }

    private String serverOutput() throws IOException {
        String input = "";
        input = bufferedReader.readLine();
        if (input == null) {
        }
        System.out.println(input);
        return input;
    }
}
