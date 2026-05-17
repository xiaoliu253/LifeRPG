package com.example.liferpg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.content.Context;

public class AvatarAdapter extends BaseAdapter {

    private Context context;
    private int[] avatarResources;
    private int selectedPosition = -1;

    public AvatarAdapter(Context context, int[] avatarResources) {
        this.context = context;
        this.avatarResources = avatarResources;
    }

    @Override
    public int getCount() {
        return avatarResources.length;
    }

    @Override
    public Object getItem(int position) {
        return avatarResources[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
            holder.ivCheck = convertView.findViewById(R.id.iv_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ivAvatar.setImageResource(avatarResources[position]);

        // 显示选中状态
        if (position == selectedPosition) {
            holder.ivCheck.setVisibility(View.VISIBLE);
            convertView.setBackgroundResource(R.drawable.avatar_selected_border);
        } else {
            holder.ivCheck.setVisibility(View.GONE);
            convertView.setBackgroundResource(0);
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class ViewHolder {
        ImageView ivAvatar;
        ImageView ivCheck;
    }
}