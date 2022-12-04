package utils;

import java.io.Serial;
import java.io.Serializable;

public class Response implements Serializable {
    @Serial
    private final static long serialVersionUID = 2L;
    ResponseMessageEnum responseMessage;
    Object responseData;

    public Response(ResponseMessageEnum responseMessage, Object responseData) {
        this.responseMessage = responseMessage;
        this.responseData = responseData;
    }

    public ResponseMessageEnum getResponseMessage() {
        return responseMessage;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseMessage(ResponseMessageEnum message) {
        this.responseMessage = message;
    }

    public void setResponseData(Object data) {
        this.responseData = data;
    }
}
