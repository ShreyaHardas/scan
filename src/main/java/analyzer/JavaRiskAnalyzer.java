package analyzer;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class JavaRiskAnalyzer {

    private static final List<Pattern> riskyPatterns = List.of(
        Pattern.compile("(?i)password\\s*=\\s*\".*\""),
        Pattern.compile("Statement\\s+.*=\\s*conn.createStatement\\(\\)"),
        Pattern.compile("SELECT\\s+\\*\\s+FROM\\s+.*\\s+WHERE\\s+.*\\+"),
        Pattern.compile("new\\s+Random\\(")
    );

    public static List<String> analyzeFile(Path filePath) throws IOException {
        List<String> output = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (Pattern pattern : riskyPatterns) {
                if (pattern.matcher(line).find()) {
                    String suggestion = fetchSuggestion(line);
                    output.add("⚠️  Risk at line " + (i + 1) + ": " + line.trim());
                    output.add("💡 Suggestion: " + suggestion);
                }
            }
        }

        return output;
    }

    private static String fetchSuggestion(String codeLine) {
        try {
            URL url = new URL("http://127.0.0.1:8000/predict");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String json = "{\"code\":\"" + codeLine.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes("utf-8"));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            String response = sb.toString();
            int start = response.indexOf("suggestion") + 13;
            int end = response.lastIndexOf("\"");
            return response.substring(start, end);
        } catch (Exception e) {
            return "No suggestion available (API error)";
        }
    }

    private static void analyzePath(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> paths = Files.walk(path)) {
                paths.filter(p -> p.toString().endsWith(".java"))
                     .forEach(JavaRiskAnalyzer::analyzeAndPrint);
            }
        } else if (path.toString().endsWith(".java")) {
            analyzeAndPrint(path);
        } else {
            System.out.println("⚠️  Skipping non-Java file: " + path);
        }
    }

    private static void analyzeAndPrint(Path file) {
        System.out.println("\n📄 Analyzing: " + file);
        try {
            List<String> results = analyzeFile(file);
            if (results.isEmpty()) {
                System.out.println("✅ No obvious security risks found.");
            } else {
                results.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("Error analyzing file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java analyzer.JavaRiskAnalyzer <file_or_directory> [more_files_or_dirs...]");
            return;
        }

        for (String arg : args) {
            Path path = Paths.get(arg);
            if (Files.exists(path)) {
                try {
                    analyzePath(path);
                } catch (IOException e) {
                    System.err.println("Error processing " + path + ": " + e.getMessage());
                }
            } else {
                System.out.println("❌ File not found: " + arg);
            }
        }
    }
}
