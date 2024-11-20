package com.example.xdygq3;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;

public class SaveThread extends Activity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.savethread_activity);

            Toolbar toolbar = findViewById(R.id.toolbar_saveThread);
            toolbar.setTitle("缓存串至本地");
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_ios_24));
            toolbar.setContentInsetStartWithNavigation(10);
            toolbar.setNavigationOnClickListener(v -> {
                setResult(RESULT_OK, null);
                finish();
            });

            TextView textView = findViewById(R.id.InputHint);
            if(textView != null) {
                textView.setTextSize(shareData.config.textSize);
            }
            EditText editText = findViewById(R.id.InputValue);
            if(editText != null) {
                editText.setTextSize(shareData.config.textSize);
            }
            Button button = findViewById(R.id.InputButton);
            if(button != null) {
                button.setTextSize(shareData.config.textSize);
                button.setOnClickListener(item->{

                });
            }
        }
}
