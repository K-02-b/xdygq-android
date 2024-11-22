package com.example.xdygq3;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class ShowAThread extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;
    private ArrayList<Reply> replies;
    private Boolean changed = false;
    private Integer now = 0;
    private ArrayList<Integer> searchResult;
    private TextView resultCount;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_layout);

        Toolbar toolbar = findViewById(R.id.toolbar_recycler);
        setSupportActionBar(toolbar);
        toolbar.setTitle("查看缓存至本地的串");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });

        String tag = Functions.getFile(this, "currentTag.txt");

        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        replies = readAndMergeReplies(Integer.parseInt(tag));
        adapter = new ReplyAdapter(replies);
        recyclerView.setAdapter(adapter);

        EditText input = findViewById(R.id.search_text);
        resultCount = findViewById(R.id.result_counter);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                changed = true;
            }
        });
        ImageButton upButton = findViewById(R.id.search_button_up);
        upButton.setOnClickListener(v -> {
            if (changed) {
                searchResult = search(input.getText().toString());
                if (searchResult.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "没有找到内容", Toast.LENGTH_SHORT).show());
                    return;
                }
                now = 1;
            } else {
                if (now > 1) {
                    --now;
                }
            }
            updateUI();
        });
        ImageButton downButton = findViewById(R.id.search_button_down);
        downButton.setOnClickListener(v -> {
            if (changed) {
                searchResult = search(input.getText().toString());
                if (searchResult.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "没有找到内容", Toast.LENGTH_SHORT).show());
                    return;
                }
                now = searchResult.size();
            } else {
                if (now < searchResult.size()) {
                    ++now;
                }
            }
            updateUI();
        });
    }

    public void updateUI() {
        runOnUiThread(() -> {
            resultCount.setText(getString(R.string.result_count_format, now, searchResult.size()));
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(searchResult.get(now - 1), 0);
            }
        });
    }

    public ArrayList<Reply> readAndMergeReplies(Integer Id) {
        Map<Integer, Reply> uniqueReplies = new HashMap<>();
        int j = 1;
        String filePath = SaveThread.FILE_PREFIX + Id + "_" + j + ".json";
        String jsonString = Functions.getFile(this, filePath);
        while (!jsonString.isEmpty()) {
            try {
                JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);
                for (JsonElement jsonElement : jsonArray) {
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        int id = jsonObject.get("id").getAsInt();
                        if (!uniqueReplies.containsKey(id)) {
                            uniqueReplies.put(id, new Reply(
                                    jsonObject.get("title").getAsString(),
                                    jsonObject.get("name").getAsString(),
                                    jsonObject.get("cookie").getAsString(),
                                    jsonObject.get("content").getAsString(),
                                    jsonObject.get("timestamp").getAsString(),
                                    jsonObject.get("id").getAsString()
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SaveThread", "readAndMergeReplies", e);
            }
            j++;
            filePath = SaveThread.FILE_PREFIX + Id + "_" + j + ".json";
            jsonString = Functions.getFile(this, filePath);
        }

        ArrayList<Reply> sortedReplies = new ArrayList<>(uniqueReplies.values());
        sortedReplies.sort(Comparator.comparingInt(r -> Integer.parseInt(r.getId())));

        return sortedReplies;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "跳转").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 1, 1, "搜索").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            showInputDialog("跳转", R.layout.jump_dialog);
        } else if (item.getItemId() == 1) {
            LinearLayout linearLayout = findViewById(R.id.search_bar);
            if (linearLayout.getVisibility() == View.GONE) {
                linearLayout.setVisibility(View.VISIBLE);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showInputDialog(String title, int layout_id) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layout_id, null);
        LinearLayout linearLayout = (LinearLayout) dialogView;
        EditText input = null;
        int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = linearLayout.getChildAt(i);
            if (childView instanceof EditText) {
                input = (EditText) childView;
            }
        }
        if (input == null) {
            return;
        }
        EditText finalInput = input;
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String inputText = finalInput.getText().toString();
                    if (!inputText.isEmpty()) {
                        int number = Integer.parseInt(inputText);
                        handleInputNumber(number);
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void handleInputNumber(int number) {
        int position = 19 * (number - 1);
        if (recyclerView != null && adapter != null && position >= 0 && position < adapter.getItemCount()) {
            recyclerView.scrollToPosition(position);
        } else {
            Toast.makeText(this, "输入的数字无效或超出范围", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<Integer> search(String word) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < replies.size(); i++) {
            String content = replies.get(i).getContent();
            if (content.contains(word)) {
                result.add(i);
            }
        }
        return result;
    }
}
