// Contact.java
package com.example.xdygq3;

import android.app.Activity;
import android.content.Context;

public class Contact {
    private final Integer avatar;
    private final String name;
    private final Context context;
    private final Class<? extends Activity> page;
    private final String tag;
    private String mark;

    public Contact(Integer avatar, String name, Context context, Class<? extends Activity> page) {
        this.avatar = avatar;
        this.name = name;
        this.context = context;
        this.page = page;
        this.tag = name;
        this.mark = "";
    }

    public Contact(Integer avatar, String name, Context context, Class<? extends Activity> page, String mark) {
        this.avatar = avatar;
        this.name = name;
        this.context = context;
        this.page = page;
        this.tag = name;
        this.mark = mark;
    }

    public Integer getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Activity> getPage() {
        return page;
    }

    public Context getContext() {
        return context;
    }

    public String getTag() {
        return tag;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
