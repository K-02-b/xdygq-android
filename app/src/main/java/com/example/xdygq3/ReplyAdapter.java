package com.example.xdygq3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyViewHolder> {
    private List<Reply> replies;

    public ReplyAdapter(List<Reply> replies) {
        this.replies = replies;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reply_unit, parent, false);
        return new ReplyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replies.get(position);
        holder.bind(reply);
    }

    public void add(Reply newData) {
        int position = replies.size();
        this.replies.add(newData);
        notifyItemInserted(position);
    }

    public void clearAll() {
        replies.clear();
        notifyDataSetChanged();
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    public void wrap(String word) {
        if (!word.isEmpty()) {
            for (int i = 0; i < replies.size(); i++) {
                String content = replies.get(i).getContent();
                if (content.contains(word)) {
                    content = content.replace(word, "<b><font style=\"background-color: #ADD8E6\">" + word + "</font></b>");
                    replies.get(i).setContent(content);
                    notifyItemChanged(i);
                }
            }
        }
    }
}
