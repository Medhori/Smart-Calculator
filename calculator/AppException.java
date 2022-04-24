package calculator;

public class AppException extends RuntimeException {
    private String description;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, String description) {
        super(message);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
