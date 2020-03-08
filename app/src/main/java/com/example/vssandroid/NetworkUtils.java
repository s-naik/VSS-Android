package com.example.vssandroid;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    public static String uuid = "0";
    public static OkHttpClient client;
    public static String url = "0";

    // Creates a valid URL with a date passed in
    public static String buildUrl(Date date, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Seconds are used here so that previous timestamps can be obtained.
        cal.add(Calendar.SECOND, (second  - 2));

        // Formats the timestamp for the query
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String constTimeStamp = format.format(cal.getTime());

        System.out.println("Fetching at " + constTimeStamp);

        // Building the URL with the queries
        String baseUrl = url + "/high_res";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl).newBuilder();
        urlBuilder.addQueryParameter("uuid", uuid);
        urlBuilder.addQueryParameter("timestamp", constTimeStamp);
        return urlBuilder.build().toString();
    }

    // Function to create the image URL list
    public static ArrayList<String> createUrlList(Date date) {
        ArrayList<String> urlList = new ArrayList<>();

        // Loops buildUrl six times to get the urls corresponding to the previous 5 images
        for (int i = 1; i <= 3; i++) {
            String iUrl = buildUrl(date, i);
            urlList.add(iUrl);
        }

        return urlList;
    }

    // Function to get the camera uuid associated with the IP address
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getCamera() {
        client = new OkHttpClient();

        // Requests the JSON data from the url
        final Request request = new Request.Builder()
                .url(url + "/list")
                .build();

        System.out.println(url + "/list");

        ArrayList<String> uuidList = new ArrayList<>();

        Response response = null;

        CallbackFuture future = new CallbackFuture();
        client.newCall(request).enqueue(future);
        try {
            response = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Checks to see if any data
        try {
            if (response == null) {
                return "null";
            }

            String jsonData = response.body().string();

            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("cameras");

            // Gets the camera uuid
            for (int i = 0; i < jsonArray.length(); i++) {
                uuidList.add(jsonArray.getJSONObject(i).getString("uuid"));
            }

        } catch (JSONException e ) { e.printStackTrace(); }
        catch (IOException e ) { e.printStackTrace(); }

        // returns uuid if there is one in the first place
        if (uuidList.size() != 0) {
            return uuidList.get(0);
        }

        return "null";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static
    class CallbackFuture extends CompletableFuture<Response> implements Callback {
        public void onResponse(Call call, Response response) {
            super.complete(response);
        }
        public void onFailure(Call call, IOException e){
            super.completeExceptionally(e);
        }
    }
}