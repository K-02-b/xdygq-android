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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ShowAThread extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;
    private ArrayList<Reply> replies;
    private Boolean changed = false;
    private Integer now = 0;
    private ArrayList<Integer> searchResult;
    private TextView resultCount;
    private String poCookie = null;
    private String tag;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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

        tag = Functions.getFile(this, "currentTag.txt");

        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);

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
                String word = input.getText().toString();
                searchResult = search(word);
                if (searchResult.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "没有找到内容", Toast.LENGTH_SHORT).show());
                    return;
                }
                now = 1;
                changed = false;
                runOnUiThread(() -> adapter.wrap(word));
            } else {
                if (now > 1) {
                    --now;
                } else {
                    now = searchResult.size();
                }
            }
            updateUI();
        });
        ImageButton downButton = findViewById(R.id.search_button_down);
        downButton.setOnClickListener(v -> {
            if (changed) {
                String word = input.getText().toString();
                searchResult = search(word);
                if (searchResult.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "没有找到内容", Toast.LENGTH_SHORT).show());
                    return;
                }
                now = searchResult.size();
                changed = false;
                runOnUiThread(() -> adapter.wrap(word));
            } else {
                if (now < searchResult.size()) {
                    ++now;
                } else {
                    now = 1;
                }
            }
            updateUI();
        });
    }

    public void updateUI() {
        runOnUiThread(() -> {
            resultCount.setText(getString(R.string.result_count_format, now, searchResult.size()));
//            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//            if (layoutManager != null) {
//                layoutManager.ScrollToPositionWithOffset(searchResult.get(now - 1), 0);
//            }
            smoothScrollToPositionWithOffset(recyclerView, searchResult.get(now - 1), 0);
        });
    }

    private void smoothScrollToPositionWithOffset(RecyclerView recyclerView, int position, int offset) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                    return boxStart - viewStart + offset;
                }
            };
            smoothScroller.setTargetPosition(position);
            layoutManager.startSmoothScroll(smoothScroller);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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
                            String cookie = jsonObject.get("cookie").getAsString();
                            if (poCookie == null) {
                                poCookie = cookie;
                            }
                            uniqueReplies.put(id, new Reply(
                                    jsonObject.get("title").getAsString(),
                                    jsonObject.get("name").getAsString(),
                                    cookie,
                                    Objects.equals(cookie, poCookie),
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
        menu.add(0, 0, 0,
                Functions.menuIconWithText(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.jump)), "跳转")).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 1, 1,
                Functions.menuIconWithText(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.search)), "搜索")).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 2, 2,
                Functions.menuIconWithText(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.information)), "说明")).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 3, 3, "添加备注");
        menu.add(0, 4, 4,
                Functions.menuIconWithText(Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.delete)), "删除"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            showInputDialog("跳转", R.layout.jump_dialog2);
        } else if (item.getItemId() == 1) {
            LinearLayout linearLayout = findViewById(R.id.search_bar);
            if (linearLayout.getVisibility() == View.GONE) {
                linearLayout.setVisibility(View.VISIBLE);
            } else {
                linearLayout.setVisibility(View.GONE);
            }
        } else if (item.getItemId() == 4) {

            int j = 0;
            String filePath = SaveThread.FILE_PREFIX + tag + "_" + j + ".json";
            while (Functions.checkFileExists(this, filePath)) {
                Functions.deleteFile(this, filePath);
                j++;
                filePath = SaveThread.FILE_PREFIX + tag + "_" + j + ".json";
            }
            String jsonString = Functions.getFile(this, SaveThread.SAVED_THREADS_FILE);
            JsonArray threads = new Gson().fromJson(jsonString, JsonArray.class);
            for (int i = 0; i < threads.size(); i++) {
                JsonObject thread = threads.get(i).getAsJsonObject();
                if (thread.get("id").getAsInt() == Integer.parseInt(tag)) {
                    threads.remove(i);
                    break;
                }
            }
            Functions.PutFile(this, SaveThread.SAVED_THREADS_FILE, new Gson().toJson(threads));
            String message = "THREAD_DELETED_" + tag;
            Log.i("ShowAThread", "Posted message: " + message);
            EventBus.getDefault().post(new EventMessage(1, message));
            finish();
        } else if (item.getItemId() == 2) {
            String guide = "可能发生卡顿\n" +
                    "长按可以复制\n" +
                    "暂不提供一键导出\n" +
                    "搜索功能以串为单位，即一条含有关键词若干次的回复均计为一次找到\n" +
                    "搜索内容改变后点按箭头视为开始新的搜索";
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("说明：")
                    .setMessage(guide)
                    .setIcon(R.drawable.information)
                    .setCancelable(true)
                    .show();
            try {
                final int textSize = shareData.getConfig().textSize;
                Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                mAlert.setAccessible(true);
                Object mAlertController = mAlert.get(dialog);
                Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                mTitle.setAccessible(true);
                TextView mTitleView = (TextView) mTitle.get(mAlertController);
                mTitleView.setTextSize(textSize + 4);
                Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                mMessage.setAccessible(true);
                TextView mMessageView = (TextView) mMessage.get(mAlertController);
                mMessageView.setTextSize(textSize);
            } catch (Exception e) {
                Log.e("ShowAThread", "onOptionsItemSelected", e);
            }
        } else if (item.getItemId() == 3) {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.input_dialog, null);

            EditText input = dialogView.findViewById(R.id.input_text);

            new AlertDialog.Builder(this)
                    .setTitle("添加备注")
                    .setView(dialogView)
                    .setPositiveButton("确定", (dialog, which) -> {
                        String inputText = input.getText().toString();
                        if (!inputText.isEmpty()) {
                            handleInputText(inputText);
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleInputText(String inputText) {
        String jsonString = Functions.getFile(this, SaveThread.SAVED_THREADS_FILE);
        JsonArray threads = new Gson().fromJson(jsonString, JsonArray.class);
        for (int i = 0; i < threads.size(); i++) {
            JsonObject thread = threads.get(i).getAsJsonObject();
            if (thread.get("id").getAsInt() == Integer.parseInt(tag)) {
                if (!thread.has("mark")) {
                    thread.addProperty("mark", inputText);
                } else {
                    thread.remove("mark");
                    thread.addProperty("mark", inputText);
                }
                break;
            }
        }
        Functions.PutFile(this, SaveThread.SAVED_THREADS_FILE, new Gson().toJson(threads));
        EventBus.getDefault().post(new EventMessage(1, "THREAD_CHANGED_" + tag));
    }

    @Subscribe
    public void onEvent(EventMessage event) {
    }

    public void showInputDialog(String title, int layout_id) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(layout_id, null);
        LinearLayout linearLayout = (LinearLayout) dialogView;
        EditText input = null;
        TextView pageCountText = dialogView.findViewById(R.id.page_count_text); // 获取TextView
        int totalPages = calculateTotalPages(); // 计算总页数
        pageCountText.setText("共 " + totalPages + " 页"); // 设置TextView文本

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

    private int calculateTotalPages() {
        // 计算总页数的逻辑，假设每页19条记录
        int totalItems = replies.size();
        int itemsPerPage = 19;
        return (int) Math.ceil((double) totalItems / itemsPerPage);
    }


    private void handleInputNumber(int number) {
        int position = 19 * (number - 1);
        if (recyclerView != null && adapter != null && position >= 0 && position < adapter.getItemCount()) {
//            recyclerView.smoothScrollToPosition(position);
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
