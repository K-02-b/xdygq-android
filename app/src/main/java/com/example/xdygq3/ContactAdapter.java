// ContactAdapter.java
package com.example.xdygq3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.linearLayout.setOnClickListener(item->{
            if(!Objects.equals(contact.getTag(), "")) {
                Functions.PutFile(contact.getContext(), "currentTag.txt", contact.getTag());
            }
            Context context = contact.getContext();
            Class<? extends Activity> page = contact.getPage();
            if(context != null && page != null) {
                Intent intent = new Intent(context, page);
                context.startActivity(intent);
            } else if(context != null) {
                Toast.makeText(context, "敬请期待", Toast.LENGTH_SHORT).show();
            }
        });
        if(contact.getAvatar() != null) {
            holder.avatar.setImageResource(contact.getAvatar());
        }
        holder.name.setText(contact.getName());
        holder.name.setTextSize(shareData.getConfig().textSize + 4);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView avatar;
        TextView name;

        ContactViewHolder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout_contact);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }
    }
}
