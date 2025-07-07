package analyzer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class SeleniumRiskAnalyzerCLI {
    // Patterns to detect common Selenium anti-patterns & risks
    private static final List<RiskPattern> PATTERNS = List.of(
        new RiskPattern("Thread.sleep usage (flaky test risk)", Pattern.compile("Thread\\.sleep\\(")),
        new RiskPattern("Hardcoded credentials", Pattern.compile("\"(username|password)\"\\s*[:=]\\s*\".*\"")),
        new RiskPattern("Missing explicit wait before click", Pattern.compile("\\.click\\(\\)")),
        new RiskPattern("Brittle locator (By.id with digits)", Pattern.compile("By\\.id\\(\".*\\d+.*\"\\)")),
        new RiskPattern("Empty catch block", Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}"))
    );

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java SeleniumRiskAnalyzerCLI <file-or-folder>");
            return;
        }

        Path path = Paths.get(args[0]);
        if (!Files.exists(path)) {
            System.out.println("❌ File or folder not found.");
            return;
        }

        List<Path> javaFiles = new ArrayList<>();
        if (Files.isDirectory(path)) {
            Files.walk(path)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
        } else {
            javaFiles.add(path);
        }

        for (Path file : javaFiles) {
            System.out.println("\nAnalyzing: " + file);
            List<String> lines = Files.readAllLines(file);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                for (RiskPattern pattern : PATTERNS) {
                    if (pattern.pattern.matcher(line).find()) {
                        System.out.printf("⚠️  Line %d [%s]: %s%n", i + 1, pattern.description, line.trim());
                        // No AI suggestions here
                    }
                }
            }
        }
    }

    private static class RiskPattern {
        String description;
        Pattern pattern;

        RiskPattern(String description, Pattern pattern) {
            this.description = description;
            this.pattern = pattern;
        }
    }
}

    

