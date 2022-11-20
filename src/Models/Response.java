package Models;

import utils.errorHandling.ResponseMessage;

import java.io.Serial;
import java.io.Serializable;

public class Response implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    ResponseMessage message;
    Object data;

    public Response(ResponseMessage message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ResponseMessage getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public void setMessage(ResponseMessage message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
