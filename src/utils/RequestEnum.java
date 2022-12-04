package utils;

import java.io.Serial;
import java.io.Serializable;

public enum MessageEnum implements Serializable {
    MSG_CONNECT_SERVER("connect_server"),
    MSG_UPDATE_DATABASE("update_database");

    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;

    MessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
