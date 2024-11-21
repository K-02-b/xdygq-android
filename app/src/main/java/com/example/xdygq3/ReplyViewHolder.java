package com.example.xdygq3;

import android.text.Html;
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

    public ReplyViewHolder(View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title_unit);
        nameView = itemView.findViewById(R.id.name_unit);
        cookieView = itemView.findViewById(R.id.cookie_unit);
        contentView = itemView.findViewById(R.id.content_unit);
        timestampView = itemView.findViewById(R.id.timestamp_unit);
        idView = itemView.findViewById(R.id.id_unit);
    }

    public void bind(Reply reply) {
        titleView.setText(reply.getTitle());
        titleView.setTextSize(shareData.getConfig().textSize);
        nameView.setText(reply.getName());
        nameView.setTextSize(shareData.getConfig().textSize);
        cookieView.setText(reply.getCookie());
        cookieView.setTextSize(shareData.getConfig().textSize);
        contentView.setText(Html.fromHtml(reply.getContent(), Html.FROM_HTML_MODE_COMPACT));
        contentView.setTextSize(shareData.getConfig().textSize);
        timestampView.setText(reply.getTimestamp());
        timestampView.setTextSize(shareData.getConfig().textSize);
        idView.setText(reply.getId());
        idView.setTextSize(shareData.getConfig().textSize);
    }
}
