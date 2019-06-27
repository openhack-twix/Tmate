package com.example.tmate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomAdapter extends BaseAdapter {
    List<ChatRoom> rooms = new ArrayList<ChatRoom>();
    Context context;

    public ChatRoomAdapter(Context context) {
        this.context = context;
    }

    public void add(ChatRoom room) {
        this.rooms.add(room);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int i) {
        return rooms.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ChatRoomViewHolder holder = new ChatRoomViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ChatRoom room = rooms.get(i);

        convertView = messageInflater.inflate(R.layout.chat_room, null);
        holder.room_icon = (ImageView) convertView.findViewById(R.id.imageView_chatroom);
        holder.room_name = (TextView) convertView.findViewById(R.id.tv_roomname);
        holder.last_message = (TextView) convertView.findViewById(R.id.tv_last_message);
        convertView.setTag(holder);

        holder.room_name.setText(room.getRoomName());
        holder.last_message.setText(room.getLastMessage());
        GradientDrawable drawable = (GradientDrawable) holder.room_icon.getBackground();
        drawable.setColor(Color.parseColor(room.getColorString()));

        return convertView;
    }

}

class ChatRoomViewHolder {
    public ImageView room_icon;
    public TextView room_name;
    public TextView last_message;
}
