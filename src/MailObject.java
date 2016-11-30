

public class MailObject {

    private String subject;
    private String content;
    private int fileSize;

    public MailObject(String content, int fileSize) {
        this.content = content;
        this.fileSize = fileSize;
    }


    public String getContent() {
        return content;
    }
    public int getFileSize() {
        return fileSize;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }


}
