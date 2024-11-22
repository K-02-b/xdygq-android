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

import java.util.ArrayList;
import java.util.List;


public class ShowThread extends AppCompatActivity {
    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_layout);

        Toolbar toolbar = findViewById(R.id.toolbar_recycler);
        toolbar.setTitle("查看缓存至本地的串");
        toolbar.setTitleTextColor(Color.WHITE);
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
                String Id = savedThreadsJson.get(i).getAsJsonObject().get("id").getAsString();
                contactList.add(new Contact(null, Id, this, ShowAThread.class, Id));
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
        ContactAdapter contactAdapter = new ContactAdapter(contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(contactAdapter);
    }
}
