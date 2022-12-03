package utils;

import java.io.Serial;
import java.io.Serializable;

public class Response implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    ResponseMessageEnum message;
    Object data;

    public Response(ResponseMessageEnum message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ResponseMessageEnum getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setMessage(ResponseMessageEnum message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
