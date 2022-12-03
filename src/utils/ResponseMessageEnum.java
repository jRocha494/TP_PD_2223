package utils;

import java.io.Serial;
import java.io.Serializable;

public enum ResponseMessageEnum implements Serializable {
    USER_NOT_FOUND(404, "User not found"),
    SUCCESS(200, "Success");

    @Serial
    private final static long serialVersionUID = 1L;
    private final int code;
    private final String description;

    private ResponseMessageEnum(int code, String description) {
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
