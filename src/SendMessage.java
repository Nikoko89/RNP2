import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendMessage {


    public static void main(String[] args) {

        PropReader p = new PropReader("smtp.properties");
        Properties smtp = p.getProp();
        final String userName = smtp.getProperty("senderMail");
        final String password = smtp.getProperty("password");
        final String receipent = smtp.getProperty("receipent");
        final String host = smtp.getProperty("host");
        final String port = smtp.getProperty("port");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setHeader("Content-type", "text/HTML; charset=UTF-8");
            message.setFrom(new InternetAddress(userName));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(receipent));
            message.setSubject("Test");
            message.setText("Hellau");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

