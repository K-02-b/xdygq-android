package com.example.xdygq3;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ReplyViewHolder extends RecyclerView.ViewHolder {
    private final TextView titleView;
    private final TextView nameView;
    private final ImageView ifPo;
    private final TextView cookieView;
    private final TextView contentView;
    private final TextView timestampView;
    private final TextView idView;
    private final Context context;
    private final ClipboardManager clipboardManager;
    private final int textSize = shareData.getConfig().textSize;

    public ReplyViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title_unit);
        nameView = itemView.findViewById(R.id.name_unit);
        ifPo = itemView.findViewById(R.id.if_po);
        cookieView = itemView.findViewById(R.id.cookie_unit);
        contentView = itemView.findViewById(R.id.content_unit);
        timestampView = itemView.findViewById(R.id.timestamp_unit);
        idView = itemView.findViewById(R.id.id_unit);
        context = itemView.getContext();
        clipboardManager = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void bind(Reply reply) {
        final String title = reply.getTitle();
        titleView.setText(title);
        titleView.setTextSize(textSize);
        titleView.setOnLongClickListener(v -> {
            copy(title, "标题");
            return true;
        });

        final String name = reply.getName();
        nameView.setText(name);
        nameView.setTextSize(textSize);
        nameView.setOnLongClickListener(v -> {
            copy(name, "名称");
            return true;
        });

        if (reply.is_po()) {
            ifPo.setVisibility(View.VISIBLE);
        }

        final String cookie = reply.getCookie();
        cookieView.setText(cookie);
        cookieView.setTextSize(textSize);
        cookieView.setOnLongClickListener(v -> {
            copy(cookie, "饼干");
            return true;
        });

        final String content = reply.getContent();
        final Spanned spannedContent = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT);
        Pattern pattern = Pattern.compile(">>(No\\.)?\\d{8}");
        Matcher matcher = pattern.matcher(spannedContent.toString());
        SpannableString spannableString = new SpannableString(spannedContent);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ClickableSpan clickableSpan = getClickableSpan(matcher);
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        contentView.setMovementMethod(LinkMovementMethod.getInstance());
        contentView.setText(spannableString);
        contentView.setTextSize(textSize);
        contentView.setOnLongClickListener(v -> {
            copy(spannedContent.toString(), "内容");
            return true;
        });

        final String timestamp = reply.getTimestamp();
        timestampView.setText(timestamp);
        timestampView.setTextSize(textSize);
        timestampView.setOnLongClickListener(v -> {
            copy(timestamp, "时间");
            return true;
        });

        final String Id = reply.getId();
        idView.setText(Id);
        idView.setTextSize(textSize);
        idView.setOnLongClickListener(v -> {
            copy(Id, "串号");
            return true;
        });
    }

    private @NonNull ClickableSpan getClickableSpan(Matcher matcher) {
        String matchedText = matcher.group();
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String id;
                if (matchedText.startsWith(">>No.")) {
                    id = matchedText.substring(5);
                    Log.i("onClick", "点击了 >>No." + id);
                    if (ShowAThread.replyMap.containsKey(id) && ShowAThread.replyMap.get(id) != null) {
                        showReplyDialog(Objects.requireNonNull(ShowAThread.replyMap.get(id)));
                    } else {
                        hint("未在本串的回复中找到引用串，正在在线加载引用...");
                        buildReplyDialog(id);
                    }
                } else if (matchedText.startsWith(">>")) {
                    id = matchedText.substring(2);
                    Log.i("onClick", "点击了 >>" + id);
                    if (ShowAThread.replyMap.containsKey(id) && ShowAThread.replyMap.get(id) != null) {
                        showReplyDialog(Objects.requireNonNull(ShowAThread.replyMap.get(id)));
                    } else {
                        hint("未在本串的回复中找到引用串，正在在线加载引用...");
                        buildReplyDialog(id);
                    }
                }
            }
        };
    }

    private void buildReplyDialog(String id) {
        try {
            fetchPostData(id, new OnFetchPostCallback() {
                @Override
                public void onFetchSuccess(Reply reply) {
                    if (reply != null) {
                        showReplyDialog(reply);
                    }
                }

                @Override
                public void onFetchFailure(String errorMessage) {
                    hint("获取引用失败: " + errorMessage);
                }
            });
        } catch (InterruptedException e) {
            Log.e("ReplyViewHolder", "fetchPostData: ", e);
            hint("获取引用失败: " + e.getMessage());
        }
    }

    private void showReplyDialog(Reply reply) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.reply_unit, null);
        builder.setView(dialogView);
        TextView titleView = dialogView.findViewById(R.id.title_unit);
        TextView nameView = dialogView.findViewById(R.id.name_unit);
        ImageView ifPo = dialogView.findViewById(R.id.if_po);
        TextView cookieView = dialogView.findViewById(R.id.cookie_unit);
        TextView contentView = dialogView.findViewById(R.id.content_unit);
        TextView timestampView = dialogView.findViewById(R.id.timestamp_unit);
        TextView idView = dialogView.findViewById(R.id.id_unit);

        final String title = reply.getTitle();
        titleView.setText(title);
        titleView.setTextSize(textSize);
        titleView.setOnLongClickListener(v -> {
            copy(title, "标题");
            return true;
        });

        final String name = reply.getName();
        nameView.setText(name);
        nameView.setTextSize(textSize);
        nameView.setOnLongClickListener(v -> {
            copy(name, "名称");
            return true;
        });

        if (reply.is_po()) {
            ifPo.setVisibility(View.VISIBLE);
        }

        final String cookie = reply.getCookie();
        cookieView.setText(cookie);
        cookieView.setTextSize(textSize);
        cookieView.setOnLongClickListener(v -> {
            copy(cookie, "饼干");
            return true;
        });

        final String content = reply.getContent();
        final Spanned spannedContent = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT);
        Pattern pattern = Pattern.compile(">>(No\\.)?\\d{8}");
        Matcher matcher = pattern.matcher(spannedContent.toString());
        SpannableString spannableString = new SpannableString(spannedContent);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ClickableSpan clickableSpan = getClickableSpan(matcher);
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        contentView.setMovementMethod(LinkMovementMethod.getInstance());
        contentView.setText(spannableString);
        contentView.setTextSize(textSize);
        contentView.setOnLongClickListener(v -> {
            copy(spannedContent.toString(), "内容");
            return true;
        });

        final String timestamp = reply.getTimestamp();
        timestampView.setText(timestamp);
        timestampView.setTextSize(textSize);
        timestampView.setOnLongClickListener(v -> {
            copy(timestamp, "时间");
            return true;
        });

        final String Id = reply.getId();
        idView.setText(Id);
        idView.setTextSize(textSize);
        idView.setOnLongClickListener(v -> {
            copy(Id, "串号");
            return true;
        });

        runOnMainThread(() -> {
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void fetchPostData(String id, final OnFetchPostCallback callback) throws InterruptedException {
        String api = "https://api.nmb.best/api/ref";
        String url = api + "/id/" + id;
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(shareData.getSSLContext().getSocketFactory(), shareData.trustAllCerts)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", "userhash=" + shareData.getConfig().UserHash)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ReplyViewHolder", "onFailure: ", e);
                callback.onFetchFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Reply reply = parseResponseToReply(responseBody);
                        callback.onFetchSuccess(reply);
                    } catch (IOException | JSONException e) {
                        Log.e("ReplyViewHolder", "onResponse: ", e);
                        callback.onFetchFailure(e.getMessage());
                    }
                } else {
                    Log.e("ReplyViewHolder", "onResponse: " + response.code());
                    callback.onFetchFailure("请求失败，状态码: " + response.code());
                }
            }
        });
    }

    void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    void hint(String text) {
        runOnMainThread(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }

    private Reply parseResponseToReply(String responseBody) throws JSONException {
        JSONObject jsonObject = new JSONObject(responseBody);
        String title = jsonObject.getString("title");
        String name = jsonObject.getString("name");
        String cookie = jsonObject.getString("user_hash");
        Boolean isPo = ShowAThread.poCookie != null && Objects.equals(cookie, ShowAThread.poCookie);
        String content = jsonObject.getString("content");
        String timestamp = jsonObject.getString("now");
        String id = jsonObject.getString("id");

        return new Reply(title, name, cookie, isPo, content, timestamp, id);
    }

    private void copy(String text) {
        copy(text, "已复制到剪贴板");
    }

    private void copy(String text, String title) {
        ClipData clip = ClipData.newPlainText("content", text);
        clipboardManager.setPrimaryClip(clip);
        hint(title + "已复制到剪贴板");
    }

    public interface OnFetchPostCallback {
        void onFetchSuccess(Reply reply);

        void onFetchFailure(String errorMessage);
    }
}
