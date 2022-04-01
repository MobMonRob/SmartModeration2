package dhbw.smartmoderation.util;

import static android.content.Context.WIFI_SERVICE;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.data.model.ModerationCard;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {
    private static final String TAG = "Client";
    private boolean isRunning;
    private String ipAddress;
    private int port;
    private String apiKey;
    SmartModerationApplication app = (SmartModerationApplication) SmartModerationApplication.getApp();

    public Client() {
    }


    public void updateModerationCard(ModerationCard moderationCard) {

    }

    public void addModerationCard(ModerationCard moderationCard) {

    }

    public void deleteModerationCard(long cardId) {

    }

    public void sendLoginInformation(String androidIpAddress, long meetingId) throws JSONException {
        System.out.println("Send login information");
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("meetingId", meetingId);
        loginJSON.put("ipAddress", androidIpAddress);
        loginJSON.put("port", WebServer.getPort());
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, loginJSON.toString());
        Request request = new Request.Builder()
                .url("http://" + ipAddress + ":" + port + "/login")
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Thread thread = new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.body() != null && response.body().string().equals("OK") && response.code() == 200) {
                    System.out.println("Login OK!");
                    isRunning = true;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
    }

    public void startClient(String ipAddress, int port, String apiKey, long meetingId) throws IOException, JSONException {
        System.out.println("Start client.");
        this.ipAddress = ipAddress;
        this.port = port;
        this.apiKey = apiKey;
        app.startWebServer();
        WifiManager wifiManager = (WifiManager) app.getApplicationContext().getSystemService(WIFI_SERVICE);
        String androidIpAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        System.out.println("IP-Address: " + androidIpAddress);
        sendLoginInformation(androidIpAddress, meetingId);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
