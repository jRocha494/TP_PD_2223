package utils.errorHandling;

public enum Errors {
    USER_NOT_FOUND(404, "User not found");

    private final int code;
    private final String description;

    private Errors(int code, String description) {
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
