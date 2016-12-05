import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikoko on 05.12.16.
 */
public class MessageStore {

    List<MailObject> messages = new ArrayList<>();

    public List<MailObject> getMessages(){
        return messages;
    }

    public void setMessages(List<MailObject> mess){
        messages = mess;
    }
}
