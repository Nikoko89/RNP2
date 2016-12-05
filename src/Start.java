import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Start {

    public static void main(String[] args){
        Properties acc1 = new PropReader("account1.properties").getProp();
        Properties acc2 = new PropReader("account2.properties").getProp();
        List<MailAccount> accounts = new ArrayList<>();
        accounts.add(new MailAccount(acc1));
        accounts.add(new MailAccount(acc2));
        Pop3ProxyClient proxy = new Pop3ProxyClient(accounts);
        Thread client = new Thread(proxy);
        client.start();
        Pop3Proxy pop = new Pop3Proxy(proxy);
    }
}
