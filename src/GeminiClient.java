import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class GeminiClient {
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    private final String apiKey;

    public GeminiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String analyzeMessage(String message) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String prompt = """
            You are an empathetic journaling assistant.
            Analyze the following journal entry and respond ONLY with valid JSON (no markdown, no code fences).

            Format:
            {
              "emotion": "positive" | "neutral" | "sad" | "angry",
              "tip": "a short, gentle one-sentence message that fits this mood"
            }

            Examples:
            Entry: "I feel great about how today went."
            -> {"emotion": "positive", "tip": "Keep embracing what makes you feel accomplished!"}

            Entry: "I'm really upset about how my day went."
            -> {"emotion": "angry", "tip": "Take a few deep breaths and give yourself space before reacting."}

            Entry: "I'm tired and drained."
            -> {"emotion": "sad", "tip": "Be kind to yourself and rest if you need it."}

            Entry: "It was an average day."
            -> {"emotion": "neutral", "tip": "It's okay to have quiet, uneventful days too."}

            Journal entry: "%s"
            """.formatted(message);

        String body = """
            {"contents": [{"parts": [{"text": "%s"}]}]}
            """.formatted(prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Gemini API error: " + response.body());

        String raw = response.body();
        String cleaned = raw.replaceAll("(?s)```json", "")
                            .replaceAll("(?s)```", "")
                            .trim();

        int start = cleaned.indexOf("{\"emotion\"");
        int end = cleaned.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end);
        }

        return cleaned;
    }
}
