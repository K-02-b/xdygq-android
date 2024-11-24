package com.example.xdygq3;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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
        contentView.setText(spannedContent);
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

    private void copy(String text) {
        copy(text, "已复制到剪贴板");
    }

    private void copy(String text, String title) {
        ClipData clip = ClipData.newPlainText("content", text);
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(context, title + "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
}
