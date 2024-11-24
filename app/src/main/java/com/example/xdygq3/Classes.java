package com.example.xdygq3;

import android.view.View;

import java.util.List;

public class Classes {

    public static class Word {
        public String word;
        public Integer outPosition;
        public Integer position;

        public Word(String word, Integer outPosition, Integer position) {
            this.word = word;
            this.outPosition = outPosition;
            this.position = position;
        }

        public Word(Integer outPosition, Integer position) {
            this.word = "";
            this.outPosition = outPosition;
            this.position = position;
        }
    }

    public static class Post {
        public int Id;
        public String Mark;
        public boolean OnlyPo;
        public int ReplyCount;
        public int NewCount;
    }

    public static class SettingsData {
        public String UserHash = "";
        public List<Post> Posts;
        public int DelayTime = 30000;
        public int InnerDelayTime = 1000;
        public int textSize = 15;
        public boolean submitToServer = false;
        public boolean popWarning = true;
    }

    public static class Count {
        public int ReplyCount;
        public int NewCount;
        public int latest;

        public Count(int _r, int _n) {
            ReplyCount = _r;
            NewCount = _n;
            latest = _n;
        }

        public Count(int _r, int _n, int _l) {
            ReplyCount = _r;
            NewCount = _n;
            latest = _l;
        }
    }

    public static class Compat {
        public String content;
        public int id;
        public String tag;
        public int textSize;

        public Compat(String _c) {
            content = _c;
            id = View.generateViewId();
            tag = "";
            textSize = shareData.config != null ? shareData.config.textSize : 15;
        }

        public Compat(String _c, int _i) {
            content = _c;
            id = _i;
            tag = "";
            textSize = shareData.config != null ? shareData.config.textSize : 15;
        }

        public Compat(String _c, int _i, String _t) {
            content = _c;
            id = _i;
            tag = _t;
            textSize = shareData.config != null ? shareData.config.textSize : 15;
        }

        public Compat(String _c, int _i, int _s) {
            content = _c;
            id = _i;
            tag = "";
            textSize = _s;
        }

        public Compat(String _c, int _i, String _t, int _s) {
            content = _c;
            id = _i;
            tag = _t;
            textSize = _s;
        }
    }
}
