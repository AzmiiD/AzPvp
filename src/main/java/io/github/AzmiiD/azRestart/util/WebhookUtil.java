package io.github.AzmiiD.azRestart.util;

import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookUtil {

    /**
     * Sends a message to a Discord webhook asynchronously
     * @param webhookUrl The Discord webhook URL
     * @param message The message to send
     */
    public static void sendWebhookMessage(String webhookUrl, String message) {
        // Run async to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(
                Bukkit.getPluginManager().getPlugin("AutoRestart"),
                () -> sendWebhookSync(webhookUrl, message)
        );
    }

    /**
     * Sends a message to a Discord webhook synchronously
     * @param webhookUrl The Discord webhook URL
     * @param message The message to send
     */
    private static void sendWebhookSync(String webhookUrl, String message) {
        try {
            // Validate webhook URL
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                return;
            }

            // Create URL connection
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configure request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "AutoRestart-Plugin/1.0");
            connection.setDoOutput(true);

            // Create JSON payload
            String jsonPayload = createDiscordPayload(message);

            // Send request
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
                outputStream.flush();
            }

            // Get response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                Bukkit.getLogger().info("Successfully sent webhook message to Discord");
            } else {
                Bukkit.getLogger().warning("Failed to send webhook message. Response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error sending webhook message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a JSON payload for Discord webhook
     * @param message The message content
     * @return JSON string payload
     */
    private static String createDiscordPayload(String message) {
        // Escape special characters for JSON
        String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // Create simple Discord webhook JSON
        return String.format(
                "{" +
                        "\"content\": \"%s\"," +
                        "\"username\": \"AutoRestart Bot\"," +
                        "\"avatar_url\": \"https://cdn.discordapp.com/attachments/placeholder/server-icon.png\"" +
                        "}",
                escapedMessage
        );
    }

    /**
     * Sends an embed message to Discord webhook (advanced version)
     * @param webhookUrl The Discord webhook URL
     * @param title The embed title
     * @param message The embed description
     * @param color The embed color (hex without #)
     */
    public static void sendWebhookEmbed(String webhookUrl, String title, String message, String color) {
        // Run async to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(
                Bukkit.getPluginManager().getPlugin("AutoRestart"),
                () -> sendWebhookEmbedSync(webhookUrl, title, message, color)
        );
    }

    /**
     * Sends an embed message to Discord webhook synchronously
     */
    private static void sendWebhookEmbedSync(String webhookUrl, String title, String message, String color) {
        try {
            // Validate webhook URL
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                return;
            }

            // Create URL connection
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configure request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "AutoRestart-Plugin/1.0");
            connection.setDoOutput(true);

            // Create JSON payload with embed
            String jsonPayload = createDiscordEmbedPayload(title, message, color);

            // Send request
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
                outputStream.flush();
            }

            // Get response
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
                Bukkit.getLogger().info("Successfully sent webhook embed to Discord");
            } else {
                Bukkit.getLogger().warning("Failed to send webhook embed. Response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Bukkit.getLogger().severe("Error sending webhook embed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a JSON payload with embed for Discord webhook
     */
    private static String createDiscordEmbedPayload(String title, String message, String color) {
        // Escape special characters for JSON
        String escapedTitle = title.replace("\"", "\\\"");
        String escapedMessage = message.replace("\"", "\\\"").replace("\n", "\\n");

        // Convert color to decimal if it's hex
        int colorInt = 16711680; // Default red color
        try {
            if (color != null && !color.isEmpty()) {
                colorInt = Integer.parseInt(color, 16);
            }
        } catch (NumberFormatException e) {
            // Use default color if parsing fails
        }

        return String.format(
                "{" +
                        "\"username\": \"AutoRestart Bot\"," +
                        "\"embeds\": [" +
                        "{" +
                        "\"title\": \"%s\"," +
                        "\"description\": \"%s\"," +
                        "\"color\": %d," +
                        "\"timestamp\": \"%s\"" +
                        "}" +
                        "]" +
                        "}",
                escapedTitle,
                escapedMessage,
                colorInt,
                java.time.Instant.now().toString()
        );
    }
}
