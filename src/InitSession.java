import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InitSession {

    public static void main(String[] args){
        Properties acc1 = new PropReader("account1.properties").getProp();
        Properties acc2 = new PropReader("account2.properties").getProp();
        List<MailAccount> accounts = new ArrayList<>();
        accounts.add(new MailAccount(acc1));
        accounts.add(new MailAccount(acc2));
        MessageStore allMessages = new MessageStore();
        Pop3ProxyClient proxy = new Pop3ProxyClient(accounts, allMessages);
        Thread client = new Thread(proxy);
        client.start();
        new Pop3Proxy(allMessages);
    }
}
