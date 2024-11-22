package com.example.xdygq3;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ReplyViewHolder extends RecyclerView.ViewHolder {
    private TextView titleView;
    private TextView nameView;
    private TextView cookieView;
    private TextView contentView;
    private TextView timestampView;
    private TextView idView;
    private ClipboardManager clipboardManager;

    public ReplyViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title_unit);
        nameView = itemView.findViewById(R.id.name_unit);
        cookieView = itemView.findViewById(R.id.cookie_unit);
        contentView = itemView.findViewById(R.id.content_unit);
        timestampView = itemView.findViewById(R.id.timestamp_unit);
        idView = itemView.findViewById(R.id.id_unit);
        clipboardManager = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void bind(Reply reply) {
        titleView.setText(reply.getTitle());
        titleView.setTextSize(shareData.getConfig().textSize);
        nameView.setText(reply.getName());
        nameView.setTextSize(shareData.getConfig().textSize);
        final String cookie = reply.getContent();
        cookieView.setText(cookie);
        cookieView.setTextSize(shareData.getConfig().textSize);
        cookieView.setOnLongClickListener(v -> {
            ClipData clip = ClipData.newPlainText("content", cookie);
            clipboardManager.setPrimaryClip(clip);
            return true;
        });
        final String content = reply.getContent();
        contentView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        contentView.setTextSize(shareData.getConfig().textSize);
        contentView.setOnLongClickListener(v -> {
            Spanned spannedContent = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT);
            String plainTextContent = spannedContent.toString();
            ClipData clip = ClipData.newPlainText("content", plainTextContent);
            clipboardManager.setPrimaryClip(clip);
            return true;
        });
        timestampView.setText(reply.getTimestamp());
        timestampView.setTextSize(shareData.getConfig().textSize);
        idView.setText(reply.getId());
        idView.setTextSize(shareData.getConfig().textSize);
    }
}
