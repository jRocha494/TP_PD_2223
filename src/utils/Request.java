package utils;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 2L;
    RequestEnum requestMessage;
    Object requestData;

    public Request(RequestEnum requestMessage, Object requestData) {
        this.requestMessage = requestMessage;
        this.requestData = requestData;
    }

    public RequestEnum getRequestMessage() {
        return requestMessage;
    }

    public Object getRequestData() {
        return requestData;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + requestMessage + '\'' +
                '}';
    }
}
