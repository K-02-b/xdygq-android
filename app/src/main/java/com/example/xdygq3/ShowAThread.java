package com.example.xdygq3;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ShowAThread extends Activity {
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savethread_activity);

        Toolbar toolbar = findViewById(R.id.toolbar_saveThread);
        toolbar.setTitle("查看缓存至本地的串");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });
        findViewById(R.id.Tools).setVisibility(View.GONE);


        String tag = Functions.getFile(this, "currentTag.txt");
        String savedThread = Functions.getFile(this, tag);

        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        JsonArray jsonArray = new Gson().fromJson(savedThread, JsonArray.class);
        ArrayList<Reply> replies = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            Reply reply = new Reply(jsonArray.get(i).getAsJsonObject().get("title").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("name").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("cookie").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("content").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("timestamp").getAsString(),
                    jsonArray.get(i).getAsJsonObject().get("id").getAsString()
            );
            replies.add(reply);
        }
        adapter = new ReplyAdapter(replies);
        recyclerView.setAdapter(adapter);
    }
}
