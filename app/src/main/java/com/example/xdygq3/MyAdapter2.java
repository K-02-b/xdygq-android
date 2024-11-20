package com.example.xdygq3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.List;

import okhttp3.internal.platform.OpenJSSEPlatform;

public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.MyViewHolder> {

    public static class tuple {
        String first;
        String second_1;
        boolean second_2 = false;
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

    private List<tuple> data;
    private Context context;

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
        if(tmp.CompatFlag == 1) {
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
                    Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                    mTitle.setAccessible(true);
                    TextView mTitleView = (TextView) mTitle.get(mAlertController);
                    mTitleView.setTextSize(textSize + 4);
                    Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                    mMessage.setAccessible(true);
                    TextView mMessageView = (TextView) mMessage.get(mAlertController);
                    mMessageView.setTextSize(textSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
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
