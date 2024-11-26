package com.example.xdygq3;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyViewHolder> {
    private List<Reply> replies;
    private final List<Reply> unfilteredReplies;

    public ReplyAdapter(List<Reply> newReplies) {
        replies = new ArrayList<>(newReplies);
        unfilteredReplies = new ArrayList<>(newReplies);
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
        this.replies = new ArrayList<>(replies);
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
                    content = content.replace(word, wrapTag("<font color=\"#ADD8E6\">") + word + wrapTag("</font>"));
                    replies.get(i).setContent(content);
                    notifyItemChanged(i);
                }
            }
        }
    }

    private String wrapTag(String text) {
        return "<!--begin-tag-->" + text + "<!--end-tag-->";
    }

    public void wrap(Classes.Word word) {
        if(word != null) {
            String content = replies.get(word.outPosition).getContent();
            String regex = "<!--begin-tag-->(.*?)<!--end-tag-->";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "");
            }
            matcher.appendTail(sb);
            String result = sb.toString();
            int count = 0;
            pattern = Pattern.compile(Pattern.quote(word.word), Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(result);
            sb = new StringBuffer();
            while (matcher.find()) {
                count++;
                if(count == word.position) {
                    matcher.appendReplacement(sb, wrapTag("<font color=\"red\">") + matcher.group() + wrapTag("</font>"));
                } else {
                    matcher.appendReplacement(sb, wrapTag("<font color=\"#ADD8E6\">") + matcher.group() + wrapTag("</font>"));
                }
            }
            matcher.appendTail(sb);
            content = sb.toString();
            replies.get(word.outPosition).setContent(content);
            notifyItemChanged(word.outPosition);
        }
    }

    public void unWrap(Classes.Word word) {
        if(word != null) {
            String content = replies.get(word.outPosition).getContent();
            String regex = wrapTag("(.*?)");
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "");
            }
            matcher.appendTail(sb);
            String result = sb.toString();
            content = result.replace(word.word, wrapTag("<font color=\"#ADD8E6\">") + word.word + wrapTag("</font>"));
            replies.get(word.outPosition).setContent(content);
            notifyItemChanged(word.outPosition);
        }
    }

    public void unWrap() {
        for (int i = 0; i < replies.size(); i++) {
            String content = replies.get(i).getContent();
            if(content.contains("<!--begin-tag-->") && content.contains("<!--end-tag-->")) {
                String regex = wrapTag("(.*?)");
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, "");
                }
                matcher.appendTail(sb);
                String result = sb.toString();
                replies.get(i).setContent(result);
                notifyItemChanged(i);
            }
        }
    }

    public void onlyPo() {
        List<Reply> temp = new ArrayList<>();
        for (int i = 0; i < replies.size(); i++) {
            if(replies.get(i).is_po()) {
                temp.add(replies.get(i));
            }
        }
        replies = temp;
        notifyDataSetChanged();
    }

    public void unOnlyPo() {
        replies = new ArrayList<>(unfilteredReplies);
        notifyDataSetChanged();
    }
}
