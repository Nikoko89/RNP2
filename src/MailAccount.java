import java.util.Properties;

public class MailAccount {

    private final String user;
    private final String password;
    private final String host;
    private final int port;

    public MailAccount(Properties acc) {
        this.user = acc.getProperty("user");
        this.password = acc.getProperty("password");
        this.host = acc.getProperty("host");
        this.port = Integer.parseInt(acc.getProperty("port"));
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
