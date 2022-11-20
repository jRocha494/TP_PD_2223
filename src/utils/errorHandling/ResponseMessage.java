package utils.errorHandling;

import java.io.Serial;
import java.io.Serializable;

public enum ResponseMessage implements Serializable {
    USER_NOT_FOUND(404, "User not found"),
    O_PEDRO_E_PARVO(200, "E verdade sim senhor");

    @Serial
    private final static long serialVersionUID = 1L;
    private final int code;
    private final String description;

    private ResponseMessage(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
