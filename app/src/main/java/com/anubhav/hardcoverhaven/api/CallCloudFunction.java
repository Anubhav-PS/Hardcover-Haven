package com.anubhav.hardcoverhaven.api;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CallCloudFunction {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private CloudFunctionCallback cloudFunctionCallback;

    public CallCloudFunction(CloudFunctionCallback callback) {
        this.cloudFunctionCallback = callback;
    }

    public void callVerifyRegisterNumber(String url, String registerNumber) {
        OkHttpClient client = new OkHttpClient();

        String urlWithParams = url + "?registerNumber=" + registerNumber;

        Request request = new Request.Builder()
                .url(urlWithParams)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("CloudFunctionCaller", "Failed to call Cloud Function", e);
                cloudFunctionCallback.onError();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                int code = response.code();
                String result = response.body().string();
                response.close();
                cloudFunctionCallback.onSuccess(code,result);
            }
        });
    }

    public interface CloudFunctionCallback {
        void onSuccess(int code,String result);

        void onError();
    }

}
