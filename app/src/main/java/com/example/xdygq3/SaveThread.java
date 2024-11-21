package com.example.xdygq3;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SaveThread extends Activity {
    ConcurrentMap<Integer, ArrayList<Reply>> map = new ConcurrentHashMap<>();
    JsonArray replies = new JsonArray();
    int totalPage = 0;
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;
    Classes.Post Post = null;
    Runnable task1 = () -> {
        int cnt = 0;
        for (int i = 0; i <= totalPage; i++) {
            while (map.get(i) == null && cnt <= 20) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
                ++cnt;
            }
            cnt = 0;
            ArrayList<Reply> array = map.get(i);
            if (array == null) {
                Toast.makeText(this, "获取数据超时", Toast.LENGTH_SHORT);
                return;
            }
            for (Reply reply : array) {
                replies.add(reply.toJsonObject());
                runOnUiThread(() -> adapter.add(reply));
            }
        }
        boolean flag1 = false, flag2 = false;
        try {
            String jsonString = new Gson().toJson(replies);
            Log.i("jsonString", jsonString);
            int i = 1;
            String filePath = "saveThread_" + Post.Id + "_" + i + ".json";
            String content = Functions.getFile(this, filePath);
            while (!content.isEmpty()) {
                i++;
                filePath = "saveThread_" + Post.Id + "_" + i + ".json";
                content = Functions.getFile(this, filePath);
            }
            Functions.PutFile(this, filePath, jsonString);
            Log.i("PutFile", filePath);
            flag1 = true;
        } catch (Exception e) {
            Log.e("SaveThread", "saveThread", e);
            runOnUiThread(() -> Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show());
        }
        try {
            String savedThreads = Functions.getFile(this, "savedThreads.json");
            JsonArray savedThreadsJsonArray = new Gson().fromJson(savedThreads, JsonArray.class);
            boolean found = false;
            if(savedThreadsJsonArray != null ) {
                for (int j = 0; j < savedThreadsJsonArray.size(); j++) {
                    JsonObject jsonObject = savedThreadsJsonArray.get(j).getAsJsonObject();
                    int id = jsonObject.get("id").getAsInt();
                    if (id == Post.Id) {
                        found = true;
                        break;
                    }
                }
            }
            if(savedThreadsJsonArray == null) {
                savedThreadsJsonArray = new JsonArray();
            }
            if (!found) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", Post.Id);
                savedThreadsJsonArray.add(jsonObject);
                String jsonString = new Gson().toJson(savedThreadsJsonArray);
                Functions.PutFile(this, "savedThreads.json", jsonString);
            }
            flag2 = true;
        } catch (Exception e) {
            Log.e("SaveThread", "saveThread", e);
            runOnUiThread(() -> Toast.makeText(this, "保存列表失败", Toast.LENGTH_SHORT).show());
        }
        if(flag1 && flag2) {
            runOnUiThread(() -> Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show());
        }
    };

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
        SwitchMaterial switch1 = findViewById(R.id.OnlyPo);
        if (switch1 != null) {
            switch1.setTextSize(shareData.getConfig().textSize);
        }
        Button button = findViewById(R.id.InputButton);
        if (button != null) {
            button.setTextSize(shareData.getConfig().textSize);
            button.setOnClickListener(item -> {
                runOnUiThread(() -> Toast.makeText(this, "正在获取数据，请等待至成功或报错", Toast.LENGTH_SHORT).show());
                map = new ConcurrentHashMap<>();
                runOnUiThread(() -> adapter.clearAll());
                replies = new JsonArray();
                Classes.Post post = new Classes.Post();
                if (editText != null) {
                    post.Id = Integer.parseInt(editText.getText().toString());
                }
                if (switch1 != null) {
                    post.OnlyPo = switch1.isChecked();
                }
                Post = post;
                try {
                    fetchPostData(0);
                } catch (InterruptedException e) {
                    Log.e("SaveThread", "fetchPostData", e);
                }
                new Thread(task1).start();
            });
        }
        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplyAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void fetchPostData(Integer i) throws InterruptedException {
        String api = Post.OnlyPo ? "https://api.nmb.best/api/po/id/" : "https://api.nmb.best/api/thread/id/";
        String url = api + Post.Id + "/page/" + i;
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(shareData.getSSLContext().getSocketFactory(), shareData.trustAllCerts)
                .build();
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
                handleResponse(i, response);
            }
        });
    }

    private void handleRequestFailure(IOException e) {
        runOnUiThread(() -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        Log.e("SaveThread", "请求错误", e);
    }

    private void handleResponse(Integer i, Response response) throws IOException {
        String responseBody = response.body().string();
        try {
            JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class).getAsJsonObject();
            ArrayList<Reply> array1 = new ArrayList<>();
            if (i == 0) {
                totalPage = (int) Math.ceil((double) jsonObject.get("ReplyCount").getAsInt() / 19);
                Reply reply = new Reply(
                        jsonObject.get("title").getAsString(),
                        jsonObject.get("name").getAsString(),
                        jsonObject.get("user_hash").getAsString(),
                        jsonObject.get("content").getAsString(),
                        jsonObject.get("now").getAsString(),
                        jsonObject.get("id").getAsString()
                );
                array1.add(reply);
            } else {
                JsonArray array = jsonObject.get("Replies").getAsJsonArray();
                for (int j = 0; j < array.size(); j++) {
                    JsonObject object = array.get(j).getAsJsonObject();
                    if (!Objects.equals(object.get("user_hash").getAsString(), "Tips")) {
                        array1.add(new Reply(
                                object.get("title").getAsString(),
                                object.get("name").getAsString(),
                                object.get("user_hash").getAsString(),
                                object.get("content").getAsString(),
                                object.get("now").getAsString(),
                                object.get("id").getAsString()
                        ));
                    }
                }
            }
            map.put(i, array1);
            if (i % 2 == 0) {
                for (int j = i + 1; j <= i + 2; j++) {
                    if (j <= totalPage) {
                        int finalJ = j;
                        new Thread(() -> {
                            try {
                                fetchPostData(finalJ);
                            } catch (InterruptedException e) {
                                Log.e("SaveThread", "fetchPostData", e);
                            }
                        }).start();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SaveThread", "响应错误", e);
            runOnUiThread(() -> Toast.makeText(this, "响应错误", Toast.LENGTH_SHORT).show());
        }
    }
}
