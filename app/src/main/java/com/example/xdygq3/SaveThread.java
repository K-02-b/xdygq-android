package com.example.xdygq3;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SaveThread extends AppCompatActivity {
    public static final String SAVED_THREADS_FILE = "savedThreads.json";
    public static final String FILE_PREFIX = "saveThread_";
    public static final int MAX_RETRIES = shareData.getConfig().retryTimes;
    private static final String API_URL_PO = "https://api.nmb.best/api/po/id/";
    private static final String API_URL_THREAD = "https://api.nmb.best/api/thread/id/";
    ConcurrentMap<Integer, CompletableFuture<Void>> futureMap = new ConcurrentHashMap<>();
    ConcurrentMap<Integer, Integer> retryMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, ArrayList<Reply>> map = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Boolean> map2 = new ConcurrentHashMap<>();
    private JsonArray replies = new JsonArray();
    private int totalPage = 0;
    private int savedPage = -1;
    private String poCookie = null;
    private Classes.Post post = null;
    private String filePath = null;
    /**
     * @noinspection FieldCanBeLocal
     */
    private RecyclerView recyclerView;
    private ReplyAdapter adapter;
    Runnable task1 = this::processReplies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.savethread_activity);

        setupToolbar();
        setupViews();
        setupButtonClickListener();
        setupRecyclerView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_saveThread);
        setSupportActionBar(toolbar);
        toolbar.setTitle("缓存串至本地");
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
        toolbar.setContentInsetStartWithNavigation(10);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
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
    }

    private void setupButtonClickListener() {
        Button button = findViewById(R.id.InputButton);
        if (button != null) {
            button.setTextSize(shareData.getConfig().textSize);
            button.setOnClickListener(this::onButtonClick);
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.RecyclerTools);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplyAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void onButtonClick(View v) {
        EditText editText = findViewById(R.id.InputValue);
        if (editText == null) {
            Log.e("SaveThread", "onButtonClick: ", new Exception("editText is null"));
            runOnUiThread(() -> Toast.makeText(this, "视图发生了错误", Toast.LENGTH_SHORT).show());
            return;
        }
        String text = editText.getText().toString();
        if (!text.matches("\\d{8}")) {
            runOnUiThread(() -> Toast.makeText(this, "请输入8位数字串号", Toast.LENGTH_SHORT).show());
            return;
        }
        runOnUiThread(() -> Toast.makeText(this, "正在获取数据，请等待至成功或报错", Toast.LENGTH_SHORT).show());
        map = new ConcurrentHashMap<>();
        map2 = new ConcurrentHashMap<>();
        retryMap = new ConcurrentHashMap<>();
        futureMap = new ConcurrentHashMap<>();
        savedPage = -1;
        runOnUiThread(adapter::clearAll);
        replies = new JsonArray();
        Classes.Post post = new Classes.Post();
        post.Id = Integer.parseInt(text);
        SwitchMaterial switch1 = findViewById(R.id.OnlyPo);
        if (switch1 != null) {
            post.OnlyPo = switch1.isChecked();
        }
        this.post = post;
        try {
            String jsonString = Functions.getFile(this, SAVED_THREADS_FILE);
            JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);
            for (JsonElement jsonElement : jsonArray) {
                int id = jsonElement.getAsJsonObject().get("id").getAsInt();
                if (id == this.post.Id) {
                    this.savedPage = jsonElement.getAsJsonObject().get("savedPage").getAsInt();
                    break;
                }
            }
            if (this.savedPage != -1) {
                ArrayList<Reply> replies = readAndMergeReplies(this.post.Id + (this.post.OnlyPo ? "_po" : ""));
                for (Reply reply : replies) {
                    map2.put(reply.getId(), true);
                }
                runOnUiThread(() -> adapter.setReplies(replies));
            }
        } catch (Exception e) {
            this.savedPage = -1;
            Log.e("SaveThread", "onButtonClick: ", e);
        }
        filePath = getAvailableFilePath(this.post.Id + (this.post.OnlyPo ? "_po" : ""));
        try {
            fetchPostData(0);
        } catch (InterruptedException e) {
            Log.e("SaveThread", "fetchPostData", e);
        }
        new Thread(task1).start();
    }

    private void processReplies() {
        for (int i = savedPage == -1 ? 0 : savedPage; i <= totalPage; i++) {
            futureMap.putIfAbsent(i, new CompletableFuture<>());
            try {
                Objects.requireNonNull(futureMap.get(i)).get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("SaveThread", "processReplies", e);
                try {
                    //noinspection BusyWait
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Log.e("SaveThread", "processReplies", ex);
                }
                runOnUiThread(() -> Toast.makeText(this, "发生错误，进程已结束", Toast.LENGTH_LONG).show());
                return;
            }
            ArrayList<Reply> array = map.get(i);
            if (array == null) {
                runOnUiThread(() -> Toast.makeText(this, "似乎发生了错误", Toast.LENGTH_LONG).show());
            } else {
                if (i >= savedPage) {
                    for (Reply reply : array) {
                        if (!map2.containsKey(reply.getId())) {
                            replies.add(reply.toJsonObject());
                            runOnUiThread(() -> adapter.add(reply));
                        }
                    }
                }
            }
            if (i != 0 && i % 10 == 0) {
                saveRepliesToFile(i);
                updateSavedThreadsList(i);
                replies = new JsonArray();
                filePath = getAvailableFilePath(post.Id + (post.OnlyPo ? "_po" : ""));
            }
        }
        saveRepliesToFile();
        updateSavedThreadsList();
    }

    public ArrayList<Reply> readAndMergeReplies(String Id) {
        Map<Integer, Reply> uniqueReplies = new HashMap<>();
        int j = 1;
        String filePath = FILE_PREFIX + Id + "_" + j + ".json";
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
            filePath = FILE_PREFIX + Id + "_" + j + ".json";
            jsonString = Functions.getFile(this, filePath);
        }

        ArrayList<Reply> sortedReplies = new ArrayList<>(uniqueReplies.values());
        sortedReplies.sort(Comparator.comparingInt(r -> Integer.parseInt(r.getId())));

        return sortedReplies;
    }

    private void saveRepliesToFile() {
        try {
            String jsonString = new Gson().toJson(replies);
            Functions.PutFile(this, filePath, jsonString);
            runOnUiThread(() -> Toast.makeText(this, "保存全串成功", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Log.e("SaveThread", "saveThread", e);
            runOnUiThread(() -> Toast.makeText(this, "保存全串失败", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveRepliesToFile(int i) {
        try {
            String jsonString = new Gson().toJson(replies);
            Functions.PutFile(this, filePath, jsonString);
            runOnUiThread(() -> Toast.makeText(this, "保存前 " + i + " 页成功", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Log.e("SaveThread", "saveThread", e);
            runOnUiThread(() -> Toast.makeText(this, "保存前 " + i + " 页失败", Toast.LENGTH_SHORT).show());
        }
    }

    private String getAvailableFilePath(String postId) {
        int i = 1;
        String filePath = FILE_PREFIX + postId + "_" + i + ".json";
        while (Boolean.TRUE.equals(Functions.checkFileExists(this, filePath))) {
            i++;
            filePath = FILE_PREFIX + postId + "_" + i + ".json";
        }
        return filePath;
    }

    private void updateSavedThreadsList() {
        updateSavedThreadsList(totalPage - 1);
    }

    private void updateSavedThreadsList(int i) {
        try {
            String savedThreads = Functions.getFile(this, SAVED_THREADS_FILE);
            JsonArray savedThreadsJsonArray = new Gson().fromJson(savedThreads, JsonArray.class);
            if (savedThreadsJsonArray == null) {
                savedThreadsJsonArray = new JsonArray();
            }
            boolean flag = false;
            if (!isPostInList(savedThreadsJsonArray, post.Id + (post.OnlyPo ? "_po" : ""))) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", post.Id + (post.OnlyPo ? "_po" : ""));
                jsonObject.addProperty("savedPage", i);
                savedThreadsJsonArray.add(jsonObject);
                flag = true;
            } else {
                Integer position = getThreadPosition(savedThreadsJsonArray, post.Id + (post.OnlyPo ? "_po" : ""));
                JsonObject jsonObject = savedThreadsJsonArray.get(position).getAsJsonObject();
                if (!jsonObject.has("savedPage") || jsonObject.get("savedPage").getAsInt() != i) {
                    jsonObject.addProperty("savedPage", i);
                    savedThreadsJsonArray.set(position, jsonObject);
                    flag = true;
                }
            }
            if (flag) {
                String jsonString = new Gson().toJson(savedThreadsJsonArray);
                Functions.PutFile(this, SAVED_THREADS_FILE, jsonString);
            }
        } catch (Exception e) {
            Log.e("SaveThread", "saveThread", e);
            runOnUiThread(() -> Toast.makeText(this, "保存列表失败", Toast.LENGTH_SHORT).show());
        }
    }

    private boolean isPostInList(JsonArray list, String postId) {
        if (list == null) return false;
        for (int j = 0; j < list.size(); j++) {
            JsonObject jsonObject = list.get(j).getAsJsonObject();
            if (Objects.equals(jsonObject.get("id").getAsString(), postId)) {
                return true;
            }
        }
        return false;
    }

    private Integer getThreadPosition(JsonArray list, String postId) {
        if (list == null) return null;
        for (int j = 0; j < list.size(); j++) {
            JsonObject jsonObject = list.get(j).getAsJsonObject();
            if (Objects.equals(jsonObject.get("id").getAsString(), postId)) {
                return j;
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "查看已缓存的串").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, ShowThread.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchPostData(Integer i) throws InterruptedException {
        String api = post.OnlyPo ? API_URL_PO : API_URL_THREAD;
        String url = api + post.Id + "/page/" + i;
        Log.d("SaveThread", "url: " + url);
        Call client = Functions.buildCall(url, "userhash=" + shareData.getConfig().UserHash);
        futureMap.putIfAbsent(i, new CompletableFuture<>());
        retryMap.putIfAbsent(i, 0);
        retryMap.put(i, Objects.requireNonNull(retryMap.get(i)) + 1);
        client.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleRequestFailure(i, e);
                if (Objects.requireNonNull(retryMap.get(i)) >= MAX_RETRIES) {
                    Objects.requireNonNull(futureMap.get(i)).completeExceptionally(e);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                handleResponse(i, response);
            }
        });
    }

    private void handleRequestFailure(Integer i, IOException e) {
        runOnUiThread(() -> Toast.makeText(this, Html.fromHtml("第" + i + "页内容请求失败<br />" + e.getMessage(), Html.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT).show());
        Log.e("SaveThread", "handleRequestFailure", e);
        try {
            fetchPostData(i);
        } catch (InterruptedException e1) {
            Log.e("SaveThread", "fetchPostData", e1);
        }
    }

    private void handleResponse(Integer i, Response response) {
        try {
            int statusCode = response.code();
            if (statusCode == 429) {
                handleRateLimitError(i);
                return;
            }
            if (!response.isSuccessful()) {
                handleUnknownError(i);
                return;
            }
        } catch (Exception e) {
            Log.e("SaveThread", "响应错误", e);
            runOnUiThread(() -> Toast.makeText(this, "响应错误", Toast.LENGTH_SHORT).show());
            Objects.requireNonNull(futureMap.get(i)).completeExceptionally(e);
            return ;
        }
        try {
            String responseBody = response.body().string();
            Log.d("SaveThread", "responseBody: " + responseBody);
            JsonObject jsonObject;
            try {
                jsonObject = new Gson().fromJson(responseBody, JsonObject.class).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                Log.e("SaveThread", "响应错误", e);
                try {
                    String message = new Gson().fromJson(responseBody, JsonElement.class).getAsJsonPrimitive().getAsString();
                    runOnUiThread(() -> Toast.makeText(this, "服务端响应解析失败，已停止缓存：" + message, Toast.LENGTH_SHORT).show());
                } catch (Exception e1) {
                    Log.e("SaveThread", "响应错误", e1);
                    runOnUiThread(() -> Toast.makeText(this, "服务端响应解析错误，已停止缓存", Toast.LENGTH_SHORT).show());
                }
                Objects.requireNonNull(futureMap.get(i)).completeExceptionally(e);
                return;
            } catch (Exception e) {
                Log.e("SaveThread", "响应错误", e);
                runOnUiThread(() -> Toast.makeText(this, "服务端响应解析错误，已停止缓存", Toast.LENGTH_SHORT).show());
                Objects.requireNonNull(futureMap.get(i)).completeExceptionally(e);
                return;
            }
            ArrayList<Reply> array1 = parseReplies(i, jsonObject);
            map.put(i, array1);
            Objects.requireNonNull(futureMap.get(i)).complete(null);
            if (i == 0) {
                if (savedPage >= 5) {
                    if (savedPage % 2 == 0) {
                        fetchNextPages(savedPage - 2);
                    } else {
                        fetchNextPages(savedPage - 1);
                    }
                } else {
                    fetchNextPages(0);
                }
            } else if (i % 2 == 0) {
                fetchNextPages(i);
            }
        } catch (Exception e) {
            Log.e("SaveThread", "响应错误", e);
            runOnUiThread(() -> Toast.makeText(this, "响应错误", Toast.LENGTH_SHORT).show());
        }
    }

    private void handleRateLimitError(int page) {
        runOnUiThread(() -> Toast.makeText(this, Html.fromHtml("第" + page + "页内容请求失败<br />" + "请求过于频繁，将重试", Html.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT).show());
        Log.e("SaveThread", "请求过于频繁，状态码: 429");
        try {
            if (Objects.requireNonNull(retryMap.get(page)) >= MAX_RETRIES) {
                Objects.requireNonNull(futureMap.get(page)).completeExceptionally(new Exception("请求过于频繁，状态码: 429"));
                return;
            }
            fetchPostData(page);
        } catch (InterruptedException e) {
            Log.e("SaveThread", "fetchPostData", e);
        }
    }

    private void handleUnknownError(int page) {
        runOnUiThread(() -> Toast.makeText(this, Html.fromHtml("第" + page + "页内容请求失败<br />" + "未知错误，将重试", Html.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT).show());
        Log.e("SaveThread", "请求错误");
        try {
            fetchPostData(page);
        } catch (InterruptedException e) {
            Log.e("SaveThread", "fetchPostData", e);
        }
    }

    private ArrayList<Reply> parseReplies(int page, JsonObject jsonObject) {
        ArrayList<Reply> array1 = new ArrayList<>();
        if (page == 0) {
            int replyCount = jsonObject.get("ReplyCount").getAsInt();
            if (replyCount == 0) {
                replyCount = 1;
            }
            totalPage = (int) Math.ceil((double) replyCount / 19);
            String cookie = jsonObject.get("user_hash").getAsString();
            poCookie = cookie;
            Reply reply = new Reply(
                    jsonObject.get("title").getAsString(),
                    jsonObject.get("name").getAsString(),
                    cookie,
                    true,
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
                    String cookie = object.get("user_hash").getAsString();
                    array1.add(new Reply(
                            object.get("title").getAsString(),
                            object.get("name").getAsString(),
                            cookie,
                            Objects.equals(cookie, poCookie),
                            object.get("content").getAsString(),
                            object.get("now").getAsString(),
                            object.get("id").getAsString()
                    ));
                }
            }
        }
        return array1;
    }

    private void fetchNextPages(int page) {
        for (int j = page + 1; j <= page + 2; j++) {
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
}
