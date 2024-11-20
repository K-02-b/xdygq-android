// Contact.java
package com.example.xdygq3;

import android.app.Activity;
import android.content.Context;

public class Contact {
    private final int avatar;
    private final String name;
    private final Context context;
    private final Class<? extends Activity> page;

    public Contact(int avatar, String name, Context context, Class<? extends Activity> page) {
        this.avatar = avatar;
        this.name = name;
        this.context = context;
        this.page = page;
    }

    public int getAvatar() {
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
}
