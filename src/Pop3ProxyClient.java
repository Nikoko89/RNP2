import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Pop3ProxyClient extends Thread{

    private Socket clientSocket;
    private final String user;
    private final String pass;
    private final String server;
    private final int port;
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
    }

    public void run() {
        try {
            clientSocket = new Socket("127.0.0.1", port);

            outputStream = clientSocket.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            inputStream = clientSocket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            authToMailServer();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void authToMailServer() {
        writeAndRead("USER " + user);
        writeAndRead("PASS " + pass);

    }

    public void getMails() {
        String[] inputArray = writeAndRead("STAT").split(" ");
        int mailboxSize = Integer.parseInt(inputArray[1]);

        for (int i = 0; i < mailboxSize; i++) {

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
