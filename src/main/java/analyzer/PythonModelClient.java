package analyzer;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import 

public class PythonModelClient {
    public static void main(String[] args) throws Exception {
        String inputPath = args[0];
        JSONArray inputMessages = new JSONArray(new String(Files.readAllBytes(Paths.get(inputPath))));
        JSONArray results = new JSONArray();

        for (int i = 0; i < inputMessages.length(); i++) {
            JSONObject item = inputMessages.getJSONObject(i);
            String message = item.getString("message");

            // Send to FastAPI
            URL url = new URL("http://localhost:8000/predict");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInput = "{\"message\": \"" + message.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = con.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
            JSONObject result = new JSONObject(response.toString());
            result.put("message", message);
            results.put(result);
        }

        // Save suggestions
        try (PrintWriter out = new PrintWriter("suggestions.json")) {
            out.print(results.toString(2));
        }
    }
}

    

