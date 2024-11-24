package com.example.xdygq3;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    protected final static int REQUEST_CODE_NOTIFICATION_PERMISSION = 100;
    protected final static String NotificationChannelID = "NotificationID";
    protected final static int JUMP_PAGE_RESULT_ID = 337845818;
    protected final static int REQUEST_CODE_PICK_COOKIE_IMAGE = 101;
    private static final int REQUEST_CODE_NOTIFICATIONS = 1;
    private static final int REQUEST_CODE_IGNORE_BATTERY_OPTIMIZATIONS = 2;
    public Context context = this;
    public Classes.SettingsData config = null;
    public ConcurrentHashMap<Integer, Classes.Count> Counts = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, IdPair> Ids = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Boolean> published = new ConcurrentHashMap<>();
    public Thread thread = null;
    public NotificationManager notificationManager = null;
    int InnerDelayTime;
    String UserHash;
    public Runnable task = () -> {
        try {
            String data = Functions.getFile(context, shareData.DATAFILE);
            config = new Gson().fromJson(data, Classes.SettingsData.class);
        } catch (Exception e) {
            config = new Classes.SettingsData();
            config.Posts = new ArrayList<>();
            String data = new Gson().toJson(config);
            Functions.PutFile(context, shareData.DATAFILE, data);
        }
        shareData.config = config;
        InnerDelayTime = config.InnerDelayTime;
        UserHash = config.UserHash;
        if (config.Posts == null) config.Posts = new ArrayList<>();
        List<Classes.Post> Posts = config.Posts;
        for (int i = 0; i < Posts.size(); i++) {
            Classes.Post item = Posts.get(i);
            Counts.put(item.Id, new Classes.Count(item.ReplyCount, item.NewCount));
        }
        runOnUiThread(() -> {
            try {
                TableLayout tableLayout = findViewById(R.id.TableLayout);
                while (tableLayout.getChildCount() > 0) {
                    tableLayout.removeViewAt(0);
                }
                TableRow top = new TableRow(context);
                top.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tableLayout.addView(top);
                Classes.Compat[] tmp_combats = new Classes.Compat[]{new Classes.Compat("串号"), new Classes.Compat("备注"), new Classes.Compat("回复"), new Classes.Compat("新")};
                TableRow tableRow0 = findViewById(R.id.TableRow1);
                for (int i = 0; i < tableRow0.getChildCount() && i < tmp_combats.length; i++) {
                    TextView childView = (TextView) tableRow0.getChildAt(i);
                    Classes.Compat item = tmp_combats[i];
                    if (childView != null) {
                        childView.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.textSize);
                        childView.setId(item.id);
                    }
                }
                for (int i = 0; i < Posts.size(); i++) {
                    Classes.Post item = Posts.get(i);
                    TableRow tableRow = new TableRow(context);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    int[] uniqueId = new int[]{View.generateViewId(), View.generateViewId()};
                    Ids.put(item.Id, new IdPair(uniqueId[0], uniqueId[1]));
                    Functions.addTextRow(context, tableRow, new Classes.Compat[]{
                            new Classes.Compat(Integer.toString(item.Id)),
                            new Classes.Compat(item.Mark),
                            new Classes.Compat(String.format(Locale.getDefault(), "%d", item.ReplyCount), uniqueId[0]),
                            new Classes.Compat(String.format(Locale.getDefault(), "%d", item.NewCount == shareData.NewThreadFlag ? 0 : item.NewCount), uniqueId[1]),
                            new Classes.Compat(item.OnlyPo ? "只看Po" : "", View.generateViewId(), shareData.getConfig().textSize - 4)
                    });
                    Button button = new Button(this);
                    button.setText("已读");
                    button.setTextSize(shareData.getConfig().textSize);
                    button.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    button.setOnClickListener(v -> hasRead(item.Id, uniqueId[1]));
                    tableRow.addView(button);
                    tableLayout.addView(tableRow);
                }
            } catch (NullPointerException e) {
                if (shareData.config == null) config = new Classes.SettingsData();
                if (shareData.config.Posts == null) shareData.config.Posts = new ArrayList<>();
                if (config == null) config = shareData.getConfig();
                if (config.Posts == null) config.Posts = shareData.getConfig().Posts;
            } catch (IndexOutOfBoundsException e) {
                Log.e("MainActivity", "索引越界", e);
            } catch (Exception e) {
                Log.e("MainActivity", "其他错误", e);
            }
        });
        try {
            update();
        } catch (Exception e) {
            Log.e("MainActivity", "更新错误", e);
        }
        Context appContext = getApplicationContext();
        JobScheduler scheduler = (JobScheduler) appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(appContext, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(shareData.JobInfo_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(config.DelayTime)
                .setBackoffCriteria(config.DelayTime * 2L, JobInfo.BACKOFF_POLICY_LINEAR)
                .build();
        scheduler.schedule(jobInfo);
    };

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void requestBatteryOptimize() {
//        hint("请开启忽略电池优化（可选）", Toast.LENGTH_LONG);
//        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
//        startActivityForResult(intent, REQUEST_CODE_IGNORE_BATTERY_OPTIMIZATIONS);
//    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String param = intent.getStringExtra("id");
        if (param != null) {
            int id = Integer.parseInt(param);
            if (Counts != null && Ids != null) {
                Counts.compute(id, (k, count) -> new Classes.Count(count != null ? count.ReplyCount : 0, 0));
                if (Boolean.TRUE.equals(published.get(id)))
                    notificationManager.cancel(id);
                TextView newReplyTextView = findViewById(Objects.requireNonNull(Ids.get(id)).second);
                runOnUiThread(() -> newReplyTextView.setText("0"));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            setToolbar();
        } catch (NullPointerException e) {
            Log.e("MainActivity", "布局或工具栏资源错误", e);
        }
        try {
            File path = getFilesDir();
            File file = new File(path, shareData.DATAFILE);
            if (!file.exists()) {
                FileOutputStream fos = openFileOutput(shareData.DATAFILE, Context.MODE_PRIVATE);
                config = new Classes.SettingsData();
                config.Posts = new ArrayList<>();
                String data = new Gson().toJson(config);
                fos.write(data.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            Log.e("MainActivity", "数据文件操作错误", e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                try {
                    requestNotificationPermission();
                } catch (SecurityException e) {
                    Log.e("MainActivity", "请求通知权限失败", e);
                    hint("请求通知权限失败");
                }
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!isBatteryOptimizeStatus()){
//                requestBatteryOptimize();
//            }
//        }
        try {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomView_main);
            bottomNavigationView.getMenu().findItem(R.id.navigation_item1).setChecked(true);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int action = shareData.getActionFromItemId(item.getItemId());
                switch (action) {
                    case shareData.ACTION_HOME:
                        return true;
                    case shareData.ACTION_OTHER:
                        Intent otherIntent = new Intent(this, OtherToolActivity.class);
                        startActivity(otherIntent);
                        return false;
                    case shareData.ACTION_ABOUT:
                        Intent aboutIntent = new Intent(this, AboutActivity.class);
                        startActivity(aboutIntent);
                        return false;
                    default:
                        return false;
                }
            });
        } catch (NullPointerException e) {
            Log.e("MainActivity", "底部导航栏视图错误", e);
        }
        try {
            EventBus.getDefault().register(this);
        } catch (EventBusException e) {
            Log.e("MainActivity", "EventBus 注册错误", e);
        }
        try {
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // 尚未获得通知权限，请求权限
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
                    return; // 等待权限请求结果
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "通知管理器错误", e);
        }
        try {
            thread = new Thread(task);
            thread.start();
        } catch (Exception e) {
            Log.e("MainActivity", "线程启动错误", e);
        }
    }

    boolean isBatteryOptimizeStatus() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(this.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        hint("请手动开启通知权限和弹出通知权限（可选）", Toast.LENGTH_LONG);
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, "my_channel_id");
        startActivityForResult(intent, REQUEST_CODE_NOTIFICATIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == JUMP_PAGE_RESULT_ID) {
            if (thread.isAlive())
                thread.interrupt();
            thread = new Thread(task);
            thread.start();
        } else if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    Toast.makeText(this, "未授予通知权限", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_IGNORE_BATTERY_OPTIMIZATIONS) {
            if (!isBatteryOptimizeStatus()) {
                Toast.makeText(this, "未允许忽略电池优化", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PICK_COOKIE_IMAGE) {
            Uri imageUri = data.getData();
            importCookie(imageUri);
        }
    }

    protected void importCookie(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            String cookieJson = scanQRCode(inputStream);
            Log.d("MainActivity", cookieJson);
            JsonObject object = new Gson().fromJson(cookieJson, JsonObject.class).getAsJsonObject();
            config.UserHash = object.get("cookie").getAsString();
            shareData.config = config;
            String contentData = new Gson().toJson(config);
            Functions.PutFile(context, shareData.DATAFILE, contentData);
            String name = object.get("name").getAsString();
            hint("导入饼干 " + name + " 成功");
        } catch (Exception e) {
            Log.e("MainActivity", "图片导入错误", e);
            hint("导入饼干失败");
        }
    }

    protected String scanQRCode(InputStream inputStream) {
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            Log.e("MainActivity", "二维码扫描错误", e);
        }
        return "";
    }

    public void sendNotification(Context context, int id, String title, String content) {
        try {
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "通知管理器错误", e);
            return;
        }
        NotificationChannel channel;
        try {
            channel = notificationManager.getNotificationChannel(NotificationChannelID);
        } catch (Exception e) {
            Log.e("MainActivity", "获取通知频道错误", e);
            return;
        }
        try {
            if (channel == null) {
                CharSequence name = "新消息通知";
                String description = "当有新消息时通知";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                channel = new NotificationChannel(NotificationChannelID, name, importance);
                channel.setDescription(description);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.enableLights(true);
                notificationManager.createNotificationChannel(channel);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "创建通知频道错误", e);
            return;
        }
        try {
            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.setAction("NOTIFICATION_CLICKED");
            resultIntent.putExtra("id", Integer.toString(id));
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, id, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannelID);
            builder.setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent)
                    .setColor(Color.BLUE);
            if (channel.shouldShowLights()) {
                builder.setLights(Color.BLUE, 1000, 0);
            }
            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        } catch (Exception e) {
            Log.e("MainActivity", "发送通知错误", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hint("获取通知权限成功");
            } else {
                hint("其实我还是挺好奇你不开启通知权限为什么要用这玩意、、、");
            }
        }
    }

    protected void update() {
        try {
            new Thread(this::fetchAndProcessPosts).start();
        } catch (Exception e) {
            Log.e("MainActivity", "更新线程启动错误", e);
        }
    }

    private void fetchAndProcessPosts() {
        try {
            List<Classes.Post> posts = config.Posts;
            for (Classes.Post post : posts) {
                fetchPostData(post);
                Thread.sleep(InnerDelayTime);
            }
        } catch (InterruptedException e) {
            Log.e("MainActivity", "线程运行错误", e);
        } catch (Exception e) {
            Log.e("MainActivity", "线程运行未知错误", e);
        } finally {
            saveCount();
        }
    }

    private void fetchPostData(Classes.Post post) throws InterruptedException {
        String api = post.OnlyPo ? "https://api.nmb.best/api/po/page/337845818/id/" : "https://api.nmb.best/api/thread/page/337845818/id/";
        String url = api + post.Id;
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(shareData.getSSLContext().getSocketFactory(), shareData.trustAllCerts)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "userhash=" + UserHash)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleRequestFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handleResponse(post, response);
            }
        });
    }

    private void handleRequestFailure(IOException e) {
        Log.e("MainActivity", "请求错误", e);
        if (config.popWarning) {
            runOnUiThread(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void handleResponse(Classes.Post post, Response response) throws IOException {
        String responseBody = response.body().string();
        try {
            int newReplyCount = new Gson().fromJson(responseBody, JsonObject.class).getAsJsonObject().get("ReplyCount").getAsInt();
            Classes.Count tmp = Counts.get(post.Id);
            if (tmp == null) {
                tmp = new Classes.Count(0, 0, 0);
            }
            if (tmp.NewCount == shareData.NewThreadFlag) {
                tmp = new Classes.Count(newReplyCount, 0, 0);
                Counts.put(post.Id, tmp);
                Classes.Count finalTmp = tmp;
                runOnUiThread(() -> updateUI(post, finalTmp, false));
            } else {
                tmp = new Classes.Count(
                        newReplyCount == 0 ? tmp.ReplyCount : newReplyCount,
                        tmp.NewCount + newReplyCount - tmp.ReplyCount,
                        tmp.latest
                );
                boolean shouldUpdate = tmp.NewCount != tmp.latest;
                tmp.latest = tmp.NewCount;
                Counts.put(post.Id, tmp);
                Classes.Count finalTmp = tmp;
                runOnUiThread(() -> updateUI(post, finalTmp, shouldUpdate));
            }
        } catch (Exception e) {
            Log.e("MainActivity", "响应错误", e);
        }
    }

    private void updateUI(Classes.Post post, Classes.Count count, boolean shouldUpdate) {
        IdPair ip = Ids.get(post.Id);
        TextView newReplyTextView = findViewById(ip != null ? ip.first : 0);
//        newReplyTextView.setText(Integer.toString(count.ReplyCount));
        newReplyTextView.setText(String.format(Locale.getDefault(), "%d", count.ReplyCount));
        if (shouldUpdate) {
            sendNotification(context, post.Id, post.Mark, "共有" + count.NewCount + "条新消息");
            TextView newCountTextView = findViewById(ip != null ? ip.second : 0);
//            newCountTextView.setText(Integer.toString(count.NewCount));
            newCountTextView.setText(String.format(Locale.getDefault(), "%d", count.NewCount));
            published.put(post.Id, true);
        }
    }

    protected void hint(String message) {
        hint(message, Toast.LENGTH_SHORT);
    }

    protected void hint(String message, int length) {
        runOnUiThread(() -> Toast.makeText(context, message, length).show());
    }

    protected void hasRead(int PostId, int CompatId) {
        runOnUiThread(() -> {
            TextView newReplyTextView = findViewById(CompatId);
            newReplyTextView.setText("0");
            if (Boolean.TRUE.equals(published.get(PostId))) {
                notificationManager.cancel(PostId);
            }
        });
        Counts.compute(PostId, (k, count) -> new Classes.Count(count != null ? count.ReplyCount : 0, 0));
        saveCount();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("养鸽器");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "编辑");
        menu.add(0, 1, 1, "设置");
        menu.add(0, 2, 2, "导出配置");
        menu.add(0, 3, 3, "导入配置");
        menu.add(0, 4, 4, "从二维码导入饼干");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog dialog = null;
        int textSize = shareData.getConfig().textSize;
        switch (item.getItemId()) {
            case 0:
                if (thread.isAlive()) {
                    thread.interrupt();
                }
                saveCount();
                Intent editIntent = new Intent(this, EditionActivity.class);
                startActivityForResult(editIntent, JUMP_PAGE_RESULT_ID);
                break;
            case 1:
                if (thread.isAlive()) {
                    thread.interrupt();
                }
                saveCount();
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingIntent, JUMP_PAGE_RESULT_ID);
                break;
            case 2:
                saveCount();
                String content = new Gson().toJson(config);
                hint("已复制到剪贴板", Toast.LENGTH_SHORT);
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, content);
                manager.setPrimaryClip(clipData);
                TextView showText = new TextView(context);
                showText.setText(content);
                showText.setTextIsSelectable(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                dialog = builder.setView(showText)
                        .setTitle("导出配置：")
                        .setIcon(R.drawable.information)
                        .setCancelable(true)
                        .show();
                break;
            case 3:
                EditText editText = new EditText(context);
                String content2 = new Gson().toJson(config);
                editText.setText(content2);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                dialog = builder2.setView(editText)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            if (thread.isAlive())
                                thread.interrupt();
                            try {
                                String data = editText.getText().toString();
                                config = new Gson().fromJson(data, Classes.SettingsData.class);
                                Functions.PutFile(context, shareData.DATAFILE, data);
                                shareData.config = config;
                            } catch (JsonSyntaxException e) {
                                hint("解析json时出现错误", Toast.LENGTH_SHORT);
                                Log.e("MainActivity", "解析json时出现错误", e);
                            } catch (Exception e) {
                                hint("导入配置时出现错误", Toast.LENGTH_SHORT);
                                Log.e("MainActivity", "导入配置时出现错误", e);
                            }
                            thread = new Thread(task);
                            thread.start();
                        })
                        .setNeutralButton("取消", null)
                        .setTitle("导入配置：")
                        .setIcon(R.drawable.information)
                        .setCancelable(true)
                        .show();
                break;
            case 4:
                importCookie();
                break;
        }
        if (dialog != null) {
            try {
                Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                mAlert.setAccessible(true);
                Object mAlertController = mAlert.get(dialog);
                Field mTitle = null;
                if (mAlertController != null) {
                    mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                }
                if (mTitle != null) {
                    mTitle.setAccessible(true);
                }
                TextView mTitleView = null;
                if (mTitle != null) {
                    mTitleView = (TextView) mTitle.get(mAlertController);
                }
                if (mTitleView != null) {
                    mTitleView.setTextSize(textSize + 4);
                }
                Field mMessage = null;
                if (mAlertController != null) {
                    mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                }
                if (mMessage != null) {
                    mMessage.setAccessible(true);
                }
                TextView mMessageView = null;
                if (mMessage != null) {
                    mMessageView = (TextView) mMessage.get(mAlertController);
                }
                if (mMessageView != null) {
                    mMessageView.setTextSize(textSize);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "配置错误", e);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void importCookie() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1);
        } else {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        }
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_COOKIE_IMAGE);
    }

    @Override
    protected void onDestroy() {
        if (thread.isAlive()) {
            thread.interrupt();
        }
        saveCount();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventMessage message) {
        if (Objects.equals(message.getMessage(), "UPDATE")) {
            try {
                update();
            } catch (Exception e) {
                Log.e("MainActivity", "更新错误", e);
            }
        }
    }

    public void saveCount() {
        try {
            for (Classes.Post post : config.Posts) {
                Classes.Count tmp = Counts.get(post.Id);
                post.ReplyCount = tmp != null ? tmp.ReplyCount : 0;
                post.NewCount = tmp != null ? tmp.NewCount : 0;
            }
            shareData.config = config;
            String contentData = new Gson().toJson(config);
            Functions.PutFile(context, shareData.DATAFILE, contentData);
        } catch (NullPointerException e) {
            Log.e("saveCount", "NullPointerException: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            Log.e("saveCount", "JsonSyntaxException: " + e.getMessage());
        } catch (Exception e) {
            Log.e("saveCount", "Unexpected Exception: " + e.getMessage());
        }
    }

    public static class IdPair {
        public int first;
        public int second;

        public IdPair(int _f, int _s) {
            first = _f;
            second = _s;
        }
    }

}