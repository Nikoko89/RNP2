
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Client extends Thread {

    private Socket clientSocket = null;
    private String host;
    private String port;

    private Client(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
            clientSocket.setKeepAlive(true);
        } catch (SocketException e) {
            System.err.println("Could not set to keep the socket alive");
        }

        try {

            clientSocket = new Socket(host, Integer.parseInt(port));
            clientSocket.setSoTimeout(3000);

            //Nachricht vom Client an den Server
            OutputStream outputStream = clientSocket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            // Nachricht vom Server an den Client
            InputStream inputStream = clientSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader serverInput = new BufferedReader(inputStreamReader);

            // Nachricht vom Benutzer an den Client
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

            boolean alive = true;
            while(alive) {

                //Nachricht senden
                if (System.in.available() > 0) {
                    String eingabe = userInput.readLine() + '\n';
                    bufferedWriter.write(eingabe);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();


                }

                //Nachricht lesen
                if (inputStream.available() > 0) {
                    String serverOut;
                    serverOut = serverInput.readLine();
                    System.out.println(serverOut);

                    if (serverOut.equals("OK BYE")) {
                        outputStream.close();
                        outputStreamWriter.close();
                        bufferedWriter.close();

                        inputStream.close();
                        inputStreamReader.close();
                        serverInput.close();
                        userInput.close();
                        alive = false;

                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Argumente betrachten!");
        }
        Thread clientthread = new Client(args[0], args[1]);
        clientthread.run();

    }



}