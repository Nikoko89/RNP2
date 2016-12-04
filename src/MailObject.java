import java.util.HashMap;

public class MailObject {

    private String encoding;
    private String from;
    private String mime;
    private String contentType;
    private String to;
    private String date;
    private String messageID;
    private String userAgent;
    private String subject;
    private String content;
    private int fileSize;
    private boolean deleteFlag = false;
    private boolean updateFlag = false;

    public MailObject(HashMap<String,String> message) {
        this.content = message.get("Content");
        this.fileSize = Integer.valueOf(message.get("Size"));
        this.subject = message.get("Subject");
        this.userAgent = message.get("User-Agent");
        this.messageID = message.get("Message-ID");
        this.date = message.get("Date");
        this.to = message.get("To");
        this.contentType = message.get("Content-Type");
        this.mime = message.get("MIME-Version");
        this.from = message.get("From");
        this.encoding = message.get("Content-Transfer-Encoding");


    }


    public String getContent() {

        return content;
    }

    public int getFileSize() {

        return fileSize;
    }

    public String getEncoding(){
        return encoding;
    }

    public String getFrom(){
        return from;
    }

    public String getTo(){
        return to;
    }

    public String getID(){
        return messageID;
    }

    public String getMime(){
        return mime;
    }

    public String getDate(){
        return date;
    }

    public String getUserAgent(){
        return userAgent;
    }

    public String getContentType(){
        return contentType;
    }

    public String getSubject(){
        return subject;
    }

    public boolean getDeleteFlag(){
        return deleteFlag;
    }

    public void setDeleteFlag(boolean del){
        deleteFlag = del;
    }

}
