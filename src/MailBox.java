import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailBox {
    private List<MailAccount> accounts;

    public MailBox() {
        accounts = new ArrayList<>();
        Properties data = new PropReader("account1.properties").getProp();
        while (data != null) {
            String user = data.getProperty("user");
            String pass = data.getProperty("pass");
            String serverAdress = data.getProperty("server");
            int port = Integer.parseInt(data.getProperty("port"));
            MailAccount account = new MailAccount(user, pass, serverAdress, port);
        }
    }
}
