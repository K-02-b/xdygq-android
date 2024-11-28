package com.example.xdygq3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class OtherToolActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);
        Toolbar toolbar = findViewById(R.id.toolbar_other);
        toolbar.setTitle("养鸽器");

        RecyclerView recyclerViewContacts = findViewById(R.id.recyclerView_contacts);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setHasFixedSize(true);

        List<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact(R.drawable.ic_avatar, "缓存串至本地", this, SaveThread.class));
        contactList.add(new Contact(R.drawable.ic_avatar, "以时间排序查看板块", this, null));
        contactList.add(new Contact(R.drawable.ic_avatar, "敬请期待", this, null));

        ContactAdapter contactAdapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(contactAdapter);

        try {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomView_other);
            bottomNavigationView.getMenu().findItem(R.id.navigation_item2).setChecked(true);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int action = shareData.getActionFromItemId(item.getItemId());
                switch (action) {
                    case shareData.ACTION_HOME:
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                        return false;
                    case shareData.ACTION_OTHER:
                        return true;
                    case shareData.ACTION_ABOUT:
                        Intent aboutIntent = new Intent(this, AboutActivity.class);
                        startActivity(aboutIntent);
                        finish();
                        return false;
                    default:
                        return false;
                }
            });
        } catch (NullPointerException e) {
            Log.e("OtherToolActivity", "底部导航栏视图错误", e);
        }
    }
}
