package com.example.xdygq3;

public class Reply {
    private String title;
    private String name;
    private String cookie;
    private String content;
    private String timestamp;
    private String id;

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
}
