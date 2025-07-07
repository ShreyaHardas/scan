package ai;

import okhttp3.*;
import com.google.gson.*;

public class GptHelper {
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    public static String suggestFix(String codeSnippet, String riskType) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        JsonObject body = new JsonObject();
        body.addProperty("model", "gpt-4o");

        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", "You are a secure Java coding assistant.");
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", "This code has a " + riskType + " issue:\n" + codeSnippet + "\nFix it securely.");
        messages.add(user);

        body.add("messages", messages);

        Request request = new Request.Builder()
            .url(ENDPOINT)
            .post(RequestBody.create(
                    gson.toJson(body),
                    MediaType.parse("application/json")))
            .addHeader("Authorization", "Bearer " + API_KEY)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "API error: " + response.code();
            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            return json.getAsJsonArray("choices")
                       .get(0).getAsJsonObject()
                       .getAsJsonObject("message")
                       .get("content").getAsString();
        } catch (Exception e) {
            return "GPT API error: " + e.getMessage();
        }
    }
}
