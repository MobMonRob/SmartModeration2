package dhbw.smartmoderation.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dhbw.smartmoderation.data.model.ModerationCard;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {
    private boolean isRunning;
    private String ipAddress;
    private String port;
    private String apiKey;
    private WebServer webServer;
    private long meetingId;

    public Client() {
    }


    public void updateModerationCard(ModerationCard moderationCard) {

    }

    public void addModerationCard(ModerationCard moderationCard) {

    }

    public void deleteModerationCard(long cardId) {

    }

    public void sendLoginInformation(WebServer webServer, long meetingId) throws IOException, JSONException {
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("meetingId", meetingId);
        loginJSON.put("ipAddress", webServer.getIpAddress());
        loginJSON.put("port", WebServer.getPort());

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, loginJSON.toString());
        Request request = new Request.Builder()
                .url(ipAddress + ":" + port + "/login")
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null && response.body().toString().equals("OK") && response.code() == 200) {
            isRunning = true;
        }
        System.out.println("loginjson");
        System.out.println(loginJSON.toString());

    }

    public void startClient(String ipAddress, String port, String apiKey, WebServer webServer, long meetingId) throws IOException, JSONException {
        // create http client
        this.ipAddress = ipAddress;
        this.port = port;
        this.apiKey = apiKey;
        sendLoginInformation(webServer, meetingId);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
