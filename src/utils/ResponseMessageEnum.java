package utils;

import java.io.Serial;
import java.io.Serializable;

public enum ResponseMessageEnum implements Serializable {
    NOT_FOUND(404, "Not found"),
    SUCCESS(200, "Success"),
    FAILED_DEPENDENCY(424, "Error creating dependencies"),
    UNEXPECTED_DATA(403, "Unexpected data received");

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
