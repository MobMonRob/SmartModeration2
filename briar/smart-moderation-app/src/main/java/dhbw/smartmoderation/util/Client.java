package dhbw.smartmoderation.util;

import static android.content.Context.WIFI_SERVICE;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.ModerationCard;
import okhttp3.HttpUrl;
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
    SmartModerationApplicationImpl app = (SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp();

    public Client() {
    }


    public void updateModerationCard(ModerationCard moderationCard) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject putJSON = new JSONObject();
        try {
            putJSON.put("cardId", moderationCard.getCardId());
            putJSON.put("content", moderationCard.getContent());
            putJSON.put("backgroundColor",  String.format("#%06X", (0xFFFFFF & moderationCard.getBackgroundColor())));
            putJSON.put("fontColor",  String.format("#%06X", (0xFFFFFF & moderationCard.getFontColor())));
            putJSON.put("meetingId", moderationCard.getMeetingId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(mediaType, putJSON.toString());

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(ipAddress)
                .port(port)
                .addPathSegment("moderationcards")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Thread thread = new Thread(() -> {
            try {
                client.newCall(request).execute();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
    }

    public void addModerationCard(ModerationCard moderationCard) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");

        JSONObject putJSON = new JSONObject();
        try {
            putJSON.put("cardId", moderationCard.getCardId());
            putJSON.put("content", moderationCard.getContent());
            putJSON.put("backgroundColor",  String.format("#%06X", (0xFFFFFF & moderationCard.getBackgroundColor())));
            putJSON.put("fontColor",  String.format("#%06X", (0xFFFFFF & moderationCard.getFontColor())));
            putJSON.put("meetingId", moderationCard.getMeetingId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(mediaType, putJSON.toString());

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(ipAddress)
                .port(port)
                .addPathSegment("moderationcards")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .method("PUT", body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Thread thread = new Thread(() -> {
            try {
                client.newCall(request).execute();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
    }

    public void deleteModerationCard(long cardId) {
        System.out.println("Delete card with ID: " + cardId);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(ipAddress)
                .port(port)
                .addPathSegment("moderationcards")
                .addQueryParameter("cardId", String.valueOf(cardId))
                .build();

        System.out.println("URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .method("DELETE", body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println("Delete request: " + request);

        Thread thread = new Thread(() -> {
            try {
                client.newCall(request).execute();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        thread.start();
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
