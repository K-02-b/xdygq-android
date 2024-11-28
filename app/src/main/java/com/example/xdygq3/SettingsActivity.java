package com.example.xdygq3;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    public Classes.SettingsData config = shareData.getConfig();

    private RecyclerView recyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setToolbar();
        config = shareData.getConfig();
        if (config == null) {
            config = new Classes.SettingsData();
        }
        List<MyAdapter2.tuple> data = new ArrayList<>();
        data.add(new MyAdapter2.tuple("批次请求间隔（毫秒）", Integer.toString(config.DelayTime), "DelayTime"));
        data.add(new MyAdapter2.tuple("单次请求间隔（毫秒）", Integer.toString(config.InnerDelayTime), "InnerDelayTime"));
        data.add(new MyAdapter2.tuple("字体大小", Integer.toString(config.textSize), "textSize"));
        data.add(new MyAdapter2.tuple("将请求提交到服务器", config.submitToServer, "submitToServer", "（暂未实现）\n客户端将追更列表提交给中转服务器，服务端整合所有客户端提交的列表并进行去重，确保相同的串不会被多次请求。\n理论上可以有效降低重复请求的数量，减轻岛服务器的压力，同时提升整体的响应速度。"));
        data.add(new MyAdapter2.tuple("网络请求错误时弹出提示", config.popWarning, "popWarning"));
        data.add(new MyAdapter2.tuple("单次请求超时时间（毫秒）", Integer.toString(config.callTimeout), "callTimeout", "请求超时：指整个HTTP请求的超时时间，包括连接建立、请求发送、响应接收等所有过程。一旦超过这个时间，整个请求将被取消。\n" +
                "连接超时：指尝试与服务器建立连接的时间限制。如果在这个时间内无法建立连接，将会抛出连接超时异常。\n" +
                "读取超时：指从服务器读取响应数据的时间限制。如果服务器已经建立了连接并且开始发送响应，但在指定时间内没有接收到足够的数据，将会抛出读取超时异常。"));
        data.add(new MyAdapter2.tuple("单次连接超时时间（毫秒）", Integer.toString(config.connectTimeout), "connectTimeout"));
        data.add(new MyAdapter2.tuple("单次读取超时时间（毫秒）", Integer.toString(config.readTimeout), "readTimeout"));
        data.add(new MyAdapter2.tuple("缓存串重试次数", Integer.toString(config.retryTimes), "retryTimes"));
        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter2 adapter = new MyAdapter2(this, data);
        recyclerView.setAdapter(adapter);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.setTitle("编辑");
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, Functions.menuIconWithText(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.save)), "保存")).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            save();
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int count = layoutManager.getItemCount();
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
        Functions.PutFile(this, shareData.DATAFILE, contentData);
        hint("部分设置需要重启后生效");
        finish();
    }

    /**
     * @noinspection SameParameterValue
     */
    protected void hint(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

}
