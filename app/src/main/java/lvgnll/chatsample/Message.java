package lvgnll.chatsample;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private String sender;
    private String message;
    private long timeStamp;
    private boolean self = false;
    private boolean pusherOrNot = true;
    public Message(String sender, String message, long timeStamp, boolean self) {
        this.sender = sender;
        this.message = message;
        this.timeStamp = timeStamp;
        this.self = self;
    }

    public String getSender()  {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public boolean isSelf() {
        return this.self;
    }

    public boolean isPusher() {
        return pusherOrNot;
    }

    public void setApiFromPusher(boolean api) {
        this.pusherOrNot = api;
    }

    public JSONObject toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("sender", getSender());
            jsonObject.put("message", getMessage());
            jsonObject.put("timeStamp", getTimeStamp());

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}