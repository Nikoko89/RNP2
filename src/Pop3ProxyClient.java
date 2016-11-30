import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Pop3ProxyClient extends Thread{

    private Socket clientSocket;
    private final String user;
    private final String pass;
    private final String server;
    private final int port;
    private List<MailObject> mailObjectList;
    private OutputStream outputStream;
    private OutputStreamWriter outputStreamWriter;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;


    public Pop3ProxyClient() {
        PropReader prop = new PropReader("account1.properties");
        Properties pop3 = prop.getProp();
        this.user = pop3.getProperty("user");
        this.pass = pop3.getProperty("password");
        this.server = pop3.getProperty("server");
        this.port = Integer.parseInt(pop3.getProperty("port"));
        this.mailObjectList = new ArrayList<>();
    }

    public void run() {
        try {
            clientSocket = new Socket(server, port);

            outputStream = clientSocket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            inputStream = clientSocket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            authToMailServer();
            checkInbox();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void authToMailServer() {
        writeAndRead("USER " + user);
        writeAndRead("PASS " + pass);

    }

    public void checkInbox() {
        String[] inputArray = writeAndRead("STAT").split(" ");
        int mailboxSize = Integer.parseInt(inputArray[1]);

        for (int i = 1; i < mailboxSize; i++) {
            String nextLine;
            String content = "";
            int size;
            writeAndRead("RETR " + i);
            try {

                String[] informationArray = bufferedReader.readLine().split(" ");
                size = Integer.parseInt(informationArray[1]);

                while (!(nextLine = bufferedReader.readLine()).equals(".")) {
                    content += nextLine;
                }

                MailObject mailObject = new MailObject(content, size);
                mailObjectList.add(mailObject);

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }



    private String writeAndRead(String line) {
        line = line + "\r\n";
        String answer = "";
        try {
            outputStreamWriter.write(line);
            outputStreamWriter.flush();

            answer = bufferedReader.readLine();
            System.out.println(answer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public static void main(String[] args) {

        Thread clientthread = new Pop3ProxyClient();
        clientthread.run();

    }

}
