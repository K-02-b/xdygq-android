package com.example.xdygq3;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ShowThread extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.recycler_layout);

        Toolbar toolbar = findViewById(R.id.toolbar_recycler);
        toolbar.setTitle("查看缓存至本地的串");
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });
        recyclerView = findViewById(R.id.RecyclerTools);

        String savedThreads = Functions.getFile(this, "savedThreads.json");
        JsonArray savedThreadsJson = new Gson().fromJson(savedThreads, JsonArray.class);
        if (savedThreadsJson == null) {
            runOnUiThread(() -> Toast.makeText(this, "你还没有缓存过串哦", Toast.LENGTH_SHORT).show());
            finish();
            return;
        }
        List<Contact> contactList = new ArrayList<>();
        try {
//            for (int i = 0; i < savedThreadsJson.size(); i++) {
//                String Id = savedThreadsJson.get(i).getAsJsonObject().get("id").getAsString();
//                int j = 1;
//                String filePath = "saveThread_" + Id + "_" + j + ".json";
//                String content = Functions.getFile(this, filePath);
//                while (!content.isEmpty()) {
//                    contactList.add(new Contact(null, Id + "_" + j, this, ShowAThread.class, "saveThread_" + Id + "_" + j + ".json"));
//                    j++;
//                    filePath = "saveThread_" + Id + "_" + j + ".json";
//                    content = Functions.getFile(this, filePath);
//                }
//            }
            for (int i = 0; i < savedThreadsJson.size(); i++) {
                try {
                    JsonObject jsonObject = savedThreadsJson.get(i).getAsJsonObject();
                    String Id = jsonObject.get("id").getAsString();
                    String Mark = "";
                    if (jsonObject.has("mark")) {
                        Mark = jsonObject.get("mark").getAsString();
                    }
                    contactList.add(new Contact(null, Id, this, ShowAThread.class, Mark));
                } catch (Exception e) {
                    Log.e("ShowThread", "Error: " + e);
                }
            }
        } catch (Exception e) {
            Log.e("ShowThread", "Error: " + e);
            runOnUiThread(() -> Toast.makeText(this, "出现了一些问题", Toast.LENGTH_SHORT).show());
            Functions.PutFile(this, "savedThreads.json", "");
            finish();
            return;
        }
        Log.i("ShowThread", "contactList size: " + contactList.size());
        Log.i("ShowThread", "contactList: " + contactList);
        contactAdapter = new ContactAdapter(contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(contactAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 判断字符串是否以指定前缀开始，并解析其后的下划线后的内容。
     *
     * @param input  输入字符串
     * @param prefix 前缀
     * @return 解析后的字符串，如果没有匹配则返回原字符串
     */
    private String parseThreadId(String input, String prefix) {
        if (input.startsWith(prefix)) {
            int underscoreIndex = input.indexOf('_', prefix.length());
            if (underscoreIndex != -1) {
                return input.substring(underscoreIndex + 1);
            }
        }
        return input;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        String title = message.getMessage();
        Log.i("ShowThread", "Received message: " + title);
        String tag = parseThreadId(title, "THREAD_DELETED");
        if (!title.equals(tag)) {
            contactAdapter.deleteId(tag);
        }
        tag = parseThreadId(title, "THREAD_CHANGED");
        if (!title.equals(tag)) {
            String finalTag = tag;
            Runnable task = () -> {
                String mark = "";
                try {
                    String savedThreads = Functions.getFile(this, "savedThreads.json");
                    JsonArray savedThreadsJson = new Gson().fromJson(savedThreads, JsonArray.class);
                    for (int i = 0; i < savedThreadsJson.size(); i++) {
                        try {
                            JsonObject jsonObject = savedThreadsJson.get(i).getAsJsonObject();
                            if(Objects.equals(jsonObject.get("id").getAsString(), finalTag)) {
                                if (jsonObject.has("mark")) {
                                    mark = jsonObject.get("mark").getAsString();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("ShowThread", "Error: " + e);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ShowThread", "Error: " + e);
                }
                String finalMark = mark;
                runOnUiThread(() -> contactAdapter.changeMark(finalTag, finalMark));
            };
            new Thread(task).start();
        }
    }
}
