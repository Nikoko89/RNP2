
public class MailAccount {

    private final String user;
    private final String password;
    private final String host;
    private final int port;

    public MailAccount(String user, String password, String host, int port) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public String getUser() {

        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {

        return host;
    }

    public int getPort() {

        return port;
    }
}
