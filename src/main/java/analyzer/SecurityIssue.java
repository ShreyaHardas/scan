package analyzer;

public class SecurityIssue {
    private final String type;
    private final int line;
    private final String code;
    private final String suggestion;

    public SecurityIssue(String type, int line, String code, String suggestion) {
        this.type = type;
        this.line = line;
        this.code = code;
        this.suggestion = suggestion;
    }

    public String getType() { return type; }
    public String getCode() { return code; }

    @Override
    public String toString() {
        return String.format("[%s] at line %d: %s\n  ➤ Suggestion: %s", type, line, code, suggestion);
    }
}
