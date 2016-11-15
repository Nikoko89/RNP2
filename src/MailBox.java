import javax.mail.Folder;
import javax.mail.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.*;


public class MailBox {
    private List<MailAccount> accounts;
    private ArrayList<Message> msg;

    public MailBox() {
        accounts = new ArrayList<>();
        Properties data = new PropReader("account1.properties").getProp();
        while (data != null) {
            String user = data.getProperty("user");
            String pass = data.getProperty("pass");
            String serverAdress = data.getProperty("server");
            int port = Integer.parseInt(data.getProperty("port"));
            MailAccount account = new MailAccount(user, pass, serverAdress, port);
            accounts.add(account);
        }
        accountsMessages();
    }

    public void fetchMails(MailAccount acc) {
        Properties properties = new Properties();
        properties.put("mail.pop3.host", acc.getHost());
        properties.put("mail.pop3.port", acc.getPort());
        properties.put("mail.pop3.starttls.enable", "true");
        Session session = Session.getDefaultInstance(properties);

        Store store = null;
        try {
            store = session.getStore( "pop3" );
        } catch (NoSuchProviderException e) {
            System.err.println("Could not find provider");
        }
        try {
            store.connect(acc.getHost(),acc.getPort(), acc.getUser(), acc.getPassword());
        } catch (MessagingException e) {
            System.err.println("Could not connect to store");
        }

        Folder folder = null;
        try {
            folder = store.getFolder( "INBOX" );
            folder.open( Folder.READ_ONLY );
            Message[] messages = folder.getMessages();
            for (int i = 0; i < messages.length; i++){
                msg.add(messages[i]);
            }
            folder.close(false);
            store.close();
        } catch (MessagingException e) {
            System.err.println("Could not open folder");
        }
    }

    public void accountsMessages(){
        msg = new ArrayList<>();
        for(MailAccount acc: accounts){
            fetchMails(acc);
        }
    }

    public ArrayList<Message> getAllMails(){
        return msg;
    }
}
