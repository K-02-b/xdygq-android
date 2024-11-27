package com.example.xdygq3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ShowAThread extends AppCompatActivity {
    public static String poCookie;
    public static ConcurrentMap<String, Reply> replyMap = new ConcurrentHashMap<>();
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;
    private ArrayList<Reply> replies;
    private Boolean changed;
    private Boolean onlyPo;
    private int now;
    private int lastNow;
    private ArrayList<Classes.Word> searchResult;
    private TextView resultCount;
    private String tag;
    private String searchWord;
    private Classes.Word currentWord;
    private ArrayList<Integer> uniqueSearchResult;
    private JsonObject nowProcess;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.recycler_layout);

        Toolbar toolbar = findViewById(R.id.toolbar_recycler);
        setSupportActionBar(toolbar);
        toolbar.setTitle("查看缓存至本地的串");
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_OK, null);
            finish();
        });

        tag = Functions.getFile(this, "currentTag.txt");
        poCookie = null;

        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);

        recyclerView.addItemDecoration(dividerItemDecoration);

        new Thread(this::drawUI).start();
        changed = false;
        onlyPo = false;
        now = 0;
        lastNow = 0;

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
            lastNow = now;
            if (changed) {
                String word = input.getText().toString();
                searchWord = word;
                currentWord = null;
                runOnUiThread(() -> adapter.unWrap());
                search(word);
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
            new Thread(this::updateUI).start();
        });
        ImageButton downButton = findViewById(R.id.search_button_down);
        downButton.setOnClickListener(v -> {
            lastNow = now;
            if (changed) {
                String word = input.getText().toString();
                searchWord = word;
                currentWord = null;
                runOnUiThread(() -> adapter.unWrap());
                search(word);
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
            new Thread(this::updateUI).start();
        });
        nowProcess = new JsonObject();
        try {
            if (Functions.checkFileExists(this, tag + "_process.json")) {
                String process = Functions.getFile(this, tag + "_process.json");
                nowProcess = new Gson().fromJson(process, JsonObject.class);
                if (nowProcess == null) {
                    nowProcess = new JsonObject();
                }
                int out = 0, inner = 0;
                if (nowProcess.has("out")) {
                    out = nowProcess.get("out").getAsInt();
                }
                if (nowProcess.has("inner")) {
                    inner = nowProcess.get("inner").getAsInt();
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(out, inner);
                }
            }
        } catch (Exception e) {
            Log.e("ShowAThread", "onCreate", e);
        }
    }

    private void drawUI() {
        replies = readAndMergeReplies(tag);
        for (Reply reply : replies) {
            replyMap.put(reply.getId(), reply);
        }
        adapter = new ReplyAdapter(replies);
        runOnUiThread(() -> recyclerView.setAdapter(adapter));
    }

    private int getCurrentPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            return layoutManager.findFirstVisibleItemPosition();
        }
        return 0;
    }

    private int getCurrentOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
//            return layoutManager.findFirstVisibleItemPosition();
            View firstVisibleItemView = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition());
            return firstVisibleItemView == null ? 0 : firstVisibleItemView.getTop();
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ShowAThread", "onDestroy");
        save_process();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ShowAThread", "onPause");
        save_process();
    }

    private void save_process() {
        if(onlyPo) {
            updateProcess("out_po", getCurrentPosition());
            updateProcess("inner_po", getCurrentOffset());
        } else {
            updateProcess("out", getCurrentPosition());
            updateProcess("inner", getCurrentOffset());
        }
        Functions.PutFile(this, tag + "_process.json",
                new Gson().toJson(nowProcess)
        );
    }

    public void updateUI() {
        runOnUiThread(() -> {
            resultCount.setText(getString(R.string.result_count_format, now, searchResult.size()));
            if (lastNow >= 1 && lastNow <= searchResult.size()) {
                adapter.unWrap(searchResult.get(lastNow - 1));
            }
            if (now >= 1 && now <= searchResult.size()) {
                currentWord = searchResult.get(now - 1);
                adapter.wrap(currentWord);
            }
        });
        Classes.Word word = searchResult.get(now - 1);
        int position = word.outPosition;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            View itemView = layoutManager.findViewByPosition(position);
            if (itemView == null) {
                Log.d("ShowAThread", "itemView is null");
                runOnUiThread(() -> recyclerView.scrollToPosition(position));
            }
            int RETRY_COUNT = 60;
            while (RETRY_COUNT > 0) {
                RETRY_COUNT--;
                itemView = layoutManager.findViewByPosition(position);
                if (itemView != null) {
                    TextView textView = itemView.findViewById(R.id.content_unit);
                    Layout layout = textView.getLayout();
                    int startIndex = word.idx;
                    int line = layout.getLineForOffset(startIndex);
                    int baseline = layout.getLineBaseline(line);
                    int topline = layout.getLineForOffset(0);
                    int offset = baseline - topline;
                    Log.d("ShowAThread", "line: " + line + ", baseline: " + baseline + ", topline: " + topline + ", offset: " + offset);
                    runOnUiThread(() -> smoothScrollToPositionWithOffset(recyclerView, position, -offset + 100));
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.e("ShowAThread", "updateUI", e);
                    }
                }
            }
        }
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

    public ArrayList<Reply> readAndMergeReplies(String Id) {
        Map<String, Reply> uniqueReplies = new HashMap<>();
        int j = 1;
        String filePath = SaveThread.FILE_PREFIX + Id + "_" + j + ".json";
        while (Functions.checkFileExists(this, filePath)) {
            try {
                String jsonString = Functions.getFile(this, filePath);
                j++;
                filePath = SaveThread.FILE_PREFIX + Id + "_" + j + ".json";
                JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);
                if (jsonArray == null) continue;
                for (JsonElement jsonElement : jsonArray) {
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        String id = jsonObject.get("id").getAsString();
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
        menu.add(0, 4, 4, "只看Po");
        menu.add(0, 5, 5,
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
                if (searchWord != null && !searchWord.isEmpty()) {
                    runOnUiThread(() -> adapter.wrap(searchWord));
                }
                if (currentWord != null) {
                    runOnUiThread(() -> adapter.wrap(currentWord));
                }
            } else {
                linearLayout.setVisibility(View.GONE);
                runOnUiThread(() -> adapter.unWrap());
            }
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
        } else if (item.getItemId() == 4) {
            if (nowProcess == null) {
                nowProcess = new JsonObject();
            }
            if (!onlyPo) {
                updateProcess("out", getCurrentPosition());
                updateProcess("inner", getCurrentOffset());
                adapter.onlyPo();
                onlyPo = true;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(
                            getProcessValue("out_po", 0),
                            getProcessValue("inner_po", 0)
                    );
                }
            } else {
                updateProcess("out_po", getCurrentPosition());
                updateProcess("inner_po", getCurrentOffset());
                adapter.unOnlyPo();
                onlyPo = false;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(
                            getProcessValue("out", 0),
                            getProcessValue("inner", 0)
                    );
                }
            }
        } else if (item.getItemId() == 5) {
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
            Log.d("ShowAThread", "Posted message: " + message);
            EventBus.getDefault().post(new EventMessage(1, message));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private int getProcessValue(String key, int defaultValue) {
        try {
            if (nowProcess.has(key)) {
                return nowProcess.get(key).getAsInt();
            }
        } catch (Exception e) {
            Log.e("ShowAThread", "onOptionsItemSelected", e);
        }
        return defaultValue;
    }

    private void updateProcess(String key, int value) {
        if (nowProcess.has(key)) {
            nowProcess.remove(key);
        }
        nowProcess.addProperty(key, value);
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
        pageCountText.setText(getString(R.string.page_count_format, totalPages)); // 使用资源字符串设置TextView文本

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
                        try {
                            int number = Integer.parseInt(inputText);
                            handleInputNumber(number);
                        } catch (Exception e) {
                            Toast.makeText(this, "输入的内容无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                .show();
    }

    private int calculateTotalPages() {
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

    public void search(String word) {
        searchResult = new ArrayList<>();
        uniqueSearchResult = new ArrayList<>();
        if (word.isEmpty()) {
            return;
        }
        for (int i = 0; i < replies.size(); i++) {
            String content = replies.get(i).getContent();
            int idx = 0;
            if (content.contains(word)) {
                uniqueSearchResult.add(i);
            }
            int count = 0;
            while ((idx = content.indexOf(word, idx)) != -1) {
                count++;
                searchResult.add(new Classes.Word(word, i, count, idx));
                idx += word.length();
            }
        }
    }

}
