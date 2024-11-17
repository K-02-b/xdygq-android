package com.example.xdygq3;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsActivity extends AppCompatActivity {

    public Classes.SettingsData config = shareData.config;

    private RecyclerView recyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setToolbar();
        List<MyAdapter2.tuple> data = new ArrayList<>();
        data.add(new MyAdapter2.tuple("批次请求间隔（毫秒）", Integer.toString(config.DelayTime), "DelayTime"));
        data.add(new MyAdapter2.tuple("单次请求间隔（毫秒）", Integer.toString(config.InnerDelayTime), "InnerDelayTime"));
        data.add(new MyAdapter2.tuple("字体大小", Integer.toString(config.textSize), "textSize"));
        data.add(new MyAdapter2.tuple("将请求提交到服务器", config.submitToServer, "submitToServer", "（暂未实现）\n客户端将追更列表提交给中转服务器，服务端整合所有客户端提交的列表并进行去重，确保相同的串不会被多次请求。\n理论上可以有效降低重复请求的数量，减轻岛服务器的压力，同时提升整体的响应速度。"));
        data.add(new MyAdapter2.tuple("网络请求错误时弹出提示", config.popWarning, "popWarning"));
        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter2 adapter = new MyAdapter2(this, data);
        recyclerView.setAdapter(adapter);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setTitle("编辑");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, Functions.menuIconWithText(AppCompatResources.getDrawable(this, R.drawable.save), "保存")).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int count = ((LinearLayoutManager) layoutManager).getItemCount();
            for (int i = 0; i < count; i++) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    MyAdapter2.MyViewHolder holder = new MyAdapter2.MyViewHolder(viewHolder.itemView);
                    String tag = holder.textView.getTag().toString();
                    switch (tag) {
                        case "DelayTime":
                            config.DelayTime = Integer.parseInt(holder.editText.getText().toString());
                            break;
                        case "InnerDelayTime":
                            config.InnerDelayTime = Integer.parseInt(holder.editText.getText().toString());
                            break;
                        case "textSize":
                            config.textSize = Integer.parseInt(holder.editText.getText().toString());
                            break;
                        case "submitToServer":
                            config.submitToServer = holder.switchCompat.isChecked();
                            break;
                        case "popWarning":
                            config.popWarning = holder.switchCompat.isChecked();
                            break;
                    }
                }
            }
        }
        shareData.config = config;
        String contentData = new Gson().toJson(config);
        Functions.PutFile(this, "data.json", contentData);
        hint("部分设置需要重启后生效");
        finish();
    }

    protected void hint(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

}
