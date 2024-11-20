package com.example.xdygq3;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SaveThread extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savethread_activity);

        Toolbar toolbar = findViewById(R.id.toolbar_saveThread);
        toolbar.setTitle("缓存串至本地");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });

        TextView textView = findViewById(R.id.InputHint);
        if (textView != null) {
            textView.setTextSize(shareData.getConfig().textSize);
        }
        EditText editText = findViewById(R.id.InputValue);
        if (editText != null) {
            editText.setTextSize(shareData.getConfig().textSize);
        }
        Switch switch1 = findViewById(R.id.OnlyPo);
        if (switch1 != null) {
            switch1.setTextSize(shareData.getConfig().textSize);
        }
        Button button = findViewById(R.id.InputButton);
        if (button != null) {
            button.setTextSize(shareData.getConfig().textSize);
            button.setOnClickListener(item -> {
                Classes.Post post = new Classes.Post();
                if (editText != null) {
                    post.Id = Integer.parseInt(editText.getText().toString());
                }
                if(switch1 != null) {
                    post.OnlyPo = switch1.isChecked();
                }
                try {
                    fetchPostData(post);
                } catch (InterruptedException e) {
                    Log.e("SaveThread", "fetchPostData", e);
                }
            });
        }
    }

    private void fetchPostData(Classes.Post post) throws InterruptedException {
        String api = post.OnlyPo ? "https://api.nmb.best/api/po/page/337845818/id/" : "https://api.nmb.best/api/thread/page/337845818/id/";
        String url = api + post.Id;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "userhash=" + shareData.getConfig().UserHash)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleRequestFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleResponse(post, response);
            }
        });
    }

    private void handleRequestFailure(IOException e) {
        runOnUiThread(()->{Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();});
        Log.e("SaveThread", "请求错误", e);
    }

    private void handleResponse(Classes.Post post, Response response) throws IOException {
        String responseBody = response.body().string();
        try {
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class).getAsJsonObject();
            Log.i("SaveThread", jsonObject.toString());

        } catch (Exception e) {
            Log.e("SaveThread", "响应错误", e);
        }
    }
}
