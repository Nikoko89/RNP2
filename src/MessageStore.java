import java.util.ArrayList;
import java.util.List;

public class MessageStore {

    List<MailObject> messages = new ArrayList<>();

    public List<MailObject> getMessages(){
        return messages;
    }

}
