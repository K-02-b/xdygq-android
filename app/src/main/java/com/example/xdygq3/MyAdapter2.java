package com.example.xdygq3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.List;

public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.MyViewHolder> {

    private final List<tuple> data;
    private final Context context;

    public MyAdapter2(Context context, List<tuple> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item2_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int textSize = shareData.getConfig().textSize;
        holder.textView.setTextSize(textSize);
        holder.editText.setTextSize(textSize);
        tuple tmp = data.get(position);
        holder.textView.setText(tmp.first);
        holder.textView.setTag(tmp.third);
        if (tmp.CompatFlag == 1) {
            holder.editText.setText(tmp.second_1);
            holder.editText.setVisibility(EditText.VISIBLE);
        } else if (tmp.CompatFlag == 2) {
            holder.switchCompat.setChecked(tmp.second_2);
            holder.switchCompat.setVisibility(SwitchCompat.VISIBLE);
        }
        if (tmp.hasInformation) {
            holder.imageButton.setVisibility(ImageButton.VISIBLE);
            holder.imageButton.setOnClickListener(view -> {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("关于：")
                        .setMessage(tmp.information)
                        .setIcon(R.drawable.information)
                        .setCancelable(true)
                        .show();
                try {
                    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                    mAlert.setAccessible(true);
                    Object mAlertController = mAlert.get(dialog);
                    Field mTitle = null;
                    if (mAlertController != null) {
                        mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                    }
                    if (mTitle != null) {
                        mTitle.setAccessible(true);
                    }
                    TextView mTitleView = null;
                    if (mTitle != null) {
                        mTitleView = (TextView) mTitle.get(mAlertController);
                    }
                    if (mTitleView != null) {
                        mTitleView.setTextSize(textSize + 4);
                    }
                    Field mMessage = null;
                    if (mAlertController != null) {
                        mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                    }
                    if (mMessage != null) {
                        mMessage.setAccessible(true);
                    }
                    TextView mMessageView = null;
                    if (mMessage != null) {
                        mMessageView = (TextView) mMessage.get(mAlertController);
                    }
                    if (mMessageView != null) {
                        mMessageView.setTextSize(textSize);
                    }
                } catch (Exception e) {
                    Log.e("MyAdapter2", "AlertDialog 配置错误", e);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class tuple {
        String first;
        String second_1;
        boolean second_2;
        String third;
        int CompatFlag;
        boolean hasInformation;
        String information;

        tuple(String _1, String _2, String _3) {
            this.first = _1;
            this.second_1 = _2;
            this.second_2 = false;
            this.third = _3;
            this.CompatFlag = 1;
            this.hasInformation = false;
            this.information = "";
        }

        tuple(String _1, boolean _2, String _3) {
            this.first = _1;
            this.second_1 = "";
            this.second_2 = _2;
            this.third = _3;
            this.CompatFlag = 2;
            this.hasInformation = false;
            this.information = "";
        }

        tuple(String _1, String _2, String _3, String _i) {
            this.first = _1;
            this.second_1 = _2;
            this.second_2 = false;
            this.third = _3;
            this.CompatFlag = 1;
            this.hasInformation = true;
            this.information = _i;
        }

        tuple(String _1, boolean _2, String _3, String _i) {
            this.first = _1;
            this.second_1 = "";
            this.second_2 = _2;
            this.third = _3;
            this.CompatFlag = 2;
            this.hasInformation = true;
            this.information = _i;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        EditText editText;
        SwitchCompat switchCompat;
        ImageButton imageButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView2);
            editText = itemView.findViewById(R.id.editText2);
            switchCompat = itemView.findViewById(R.id.switchCompat2);
            imageButton = itemView.findViewById(R.id.imageButton2);
        }
    }
}
