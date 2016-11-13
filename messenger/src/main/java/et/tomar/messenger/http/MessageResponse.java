package et.tomar.messenger.http;

/**
 * Created by anteneh on 9/27/16.
 */
public class MessageResponse {

    private String from;

    private String to;

    private String body;

    public MessageResponse(){

    }

    public MessageResponse(String from, String to, String body){

        this.from = from;
        this.to = to;
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
