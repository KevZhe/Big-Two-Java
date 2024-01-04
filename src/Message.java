import java.io.Serializable;

public class Message implements Serializable {
    
	/**
	 * class that represents a message sent between client and server
	 */
	private static final long serialVersionUID = -8712826544308069491L;

    
    private String messageType; //type of message
    private Object data; //data to be sent
    private int from; //client id of sender
    
    public Message(String messageType, Object data) {
        this.messageType = messageType;
        this.data = data;
    }
    
    public Message(String messageType, int from, Object data) {
        this.messageType = messageType;
        this.data = data;
        this.from = from;
    }

    public String getMessageType() {
        return messageType;
    }
    
    public int from() {
        return from;
    }

    public Object getData() {
        return data;
    }
}
