package lvgnll.chatsample;

public class LamReq {
    private String room;
    private String sender;
    private String message;
    private String from;
    private long timeStamp;
    public LamReq(String room, String message, String from, long timeStamp) {
        this.room = room;
        this.sender = from;
        this.message = message;
        this.from = from;
        this.timeStamp = timeStamp;
    }

    public String getRoom() {
        return room;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
