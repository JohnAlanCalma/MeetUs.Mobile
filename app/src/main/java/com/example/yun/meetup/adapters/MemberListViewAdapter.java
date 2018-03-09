package com.example.yun.meetup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yun.meetup.R;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by danie on 12/22/2017.
 */

public class MemberListViewAdapter extends BaseAdapter {

    private final List<String> mUserNames;
    private Context mContext;

    public class MembersListViewHolder {

        final TextView txtMemberName;
        final ImageButton btnSendMessage;
        final ImageButton btnRemoveMember;

        public MembersListViewHolder(View view) {
            txtMemberName = view.findViewById(R.id.txt_item_member_name);
            btnSendMessage = view.findViewById(R.id.btn_send_message_member_list);
            btnRemoveMember = view.findViewById(R.id.btn_remove_member_list);

        }
    }

    public MemberListViewAdapter(List<String> users, Context context) {
        this.mUserNames = users;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mUserNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return new BigInteger(mUserNames.get(position), 16).longValue();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        MembersListViewHolder holder;

        if(convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.lv_members_item, parent, false);
            holder = new MembersListViewHolder(view);
            view.setTag(holder);
        }else {
            view = convertView;
            holder = (MembersListViewHolder) view.getTag();
        }


        holder.txtMemberName.setText(mUserNames.get(position));

        //TODO: Buttons event listeners.

        return view;
    }
}
