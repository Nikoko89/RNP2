import javax.mail.Folder;
import javax.mail.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.*;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailBox {
    private List<MailAccount> accounts;
    private Message[] messages;
    private HashMap<MailAccount, Message[]> accountMails = new HashMap<>();

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
            messages = folder.getMessages();
        } catch (MessagingException e) {
            System.err.println("Could not open folder");
        }

        accountMails.put(acc, messages);



    }
}
