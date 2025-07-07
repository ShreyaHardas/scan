package analyzer;

import java.util.regex.Pattern;

public class RiskPattern {
    public final String name;
    public final Pattern pattern;
    public final String suggestion;

    public RiskPattern(String name, String regex, String suggestion) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
        this.suggestion = suggestion;
    }
}
