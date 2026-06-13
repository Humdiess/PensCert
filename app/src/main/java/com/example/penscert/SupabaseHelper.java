package com.example.penscert;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseHelper {
    private static final String TAG = "SupabaseHelper";
    private static final String SUPABASE_URL = "https://lcodkccmawxrnhduadwg.supabase.co"; // e.g. https://xxxx.supabase.co
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxjb2RrY2NtYXd4cm5oZHVhZHdnIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3ODQ1NzczNCwiZXhwIjoyMDk0MDMzNzM0fQ.yFFwd-ThTd7EUEQ7udQ9-UDHzGnSSRIjgmi9ZZZYTG4"; // copy from Supabase Settings → API
    
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void fetchCertificate(String input, Callback<Certificate> callback) {
        String filterValue = "(cert_id.eq." + input + ",certificate_number.eq." + input + ",qr_token.eq." + input + ")";
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/certificates")
                .newBuilder()
                .addQueryParameter("or", filterValue)
                .addQueryParameter("select", "*")
                .build();
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.body() == null) throw new IOException("Empty response body");
                    String data = response.body().string();
                    if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
                    List<Certificate> certs = gson.fromJson(data, new TypeToken<List<Certificate>>(){}.getType());
                    mainHandler.post(() -> {
                        if (certs != null && !certs.isEmpty()) callback.onSuccess(certs.get(0));
                        else callback.onError(new Exception("Dokumen tidak ditemukan."));
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    public void createCertificate(Certificate cert, Callback<String> callback) {
        String url = SUPABASE_URL + "/rest/v1/certificates";
        String json = gson.toJson(cert);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess("Berhasil"));
                } else {
                    Log.e(TAG, "Create error: " + responseBody);
                    mainHandler.post(() -> callback.onError(new Exception(responseBody)));
                }
            }
        });
    }

    public void updateCertificate(String id, Map<String, Object> updates, Callback<String> callback) {
        String url = SUPABASE_URL + "/rest/v1/certificates?id=eq." + id;
        String json = gson.toJson(updates);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess("Berhasil"));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("Gagal update data.")));
                }
            }
        });
    }

    public void fetchCertificatesByStatus(String status, Callback<List<Certificate>> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SUPABASE_URL + "/rest/v1/certificates").newBuilder().addQueryParameter("select", "*");
        if (status != null) urlBuilder.addQueryParameter("verification_status", "eq." + status);
        urlBuilder.addQueryParameter("order", "created_at.desc");
        Request request = new Request.Builder().url(urlBuilder.build()).addHeader("apikey", SUPABASE_KEY).addHeader("Authorization", "Bearer " + SUPABASE_KEY).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { mainHandler.post(() -> callback.onError(e)); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.body() == null) throw new IOException("Empty response body");
                    String data = response.body().string();
                    List<Certificate> list = gson.fromJson(data, new TypeToken<List<Certificate>>(){}.getType());
                    mainHandler.post(() -> callback.onSuccess(list != null ? list : new java.util.ArrayList<>()));
                } catch (Exception e) { mainHandler.post(() -> callback.onError(e)); }
            }
        });
    }

    public void revokeCertificate(String id, Callback<String> callback) {
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "REVOKED");
        updates.put("verification_status", "REVOKED");
        updateCertificate(id, updates, callback);
    }

    public void fetchRecentCertificates(int limit, Callback<List<Certificate>> callback) {
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/certificates")
                .newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("order", "issued_at.desc")
                .addQueryParameter("limit", String.valueOf(limit))
                .build();
        Request request = new Request.Builder().url(url).addHeader("apikey", SUPABASE_KEY).addHeader("Authorization", "Bearer " + SUPABASE_KEY).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { mainHandler.post(() -> callback.onError(e)); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.body() == null) throw new IOException("Empty response body");
                    String data = response.body().string();
                    List<Certificate> list = gson.fromJson(data, new TypeToken<List<Certificate>>(){}.getType());
                    mainHandler.post(() -> callback.onSuccess(list != null ? list : new java.util.ArrayList<>()));
                } catch (Exception e) { mainHandler.post(() -> callback.onError(e)); }
            }
        });
    }

    public void getCertificateCount(String status, Callback<Integer> callback) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SUPABASE_URL + "/rest/v1/certificates")
                .newBuilder()
                .addQueryParameter("select", "id");
        if (status != null) urlBuilder.addQueryParameter("verification_status", "eq." + status);
        Request request = new Request.Builder().url(urlBuilder.build())
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Prefer", "count=exact")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { mainHandler.post(() -> callback.onError(e)); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String range = response.header("Content-Range");
                    int count = 0;
                    if (range != null && range.contains("/")) {
                        count = Integer.parseInt(range.substring(range.indexOf("/") + 1));
                    }
                    int finalCount = count;
                    mainHandler.post(() -> callback.onSuccess(finalCount));
                } catch (Exception e) { mainHandler.post(() -> callback.onError(e)); }
            }
        });
    }

    public void uploadFile(String bucket, String path, byte[] fileData, String mimeType, Callback<String> callback) {
        String url = SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + path;
        RequestBody body = RequestBody.create(fileData, MediaType.parse(mimeType));
        Request request = new Request.Builder().url(url).post(body).addHeader("apikey", SUPABASE_KEY).addHeader("Authorization", "Bearer " + SUPABASE_KEY).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) { mainHandler.post(() -> callback.onError(e)); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess(SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("Upload gagal.")));
                }
            }
        });
    }
}
