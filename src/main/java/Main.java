

import java.nio.file.*;
import java.util.*;

import analyzer.JavaRiskAnalyzer;

public class Main {
    public static void main(String[] args) {
        try {
            Path path = Paths.get("src/main/java/codetoscan/SampleVulnerableClass.java");
            List<String> results = JavaRiskAnalyzer.analyzeFile(path);
            results.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
