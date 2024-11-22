package com.example.xdygq3;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Reply {
    private final String title;
    private final String name;
    private final String cookie;
    private final String content;
    private final String timestamp;
    private final String id;

    public Reply(String title, String name, String cookie, String content, String timestamp, String id) {
        this.title = title;
        this.name = name;
        this.cookie = cookie;
        this.content = content;
        this.timestamp = timestamp;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getCookie() {
        return cookie;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    @NonNull
    public String toString() {
        return new Gson().toJson(this);
    }

    public JsonObject toJsonObject() {
        return new Gson().fromJson(toString(), JsonObject.class);
    }
}
