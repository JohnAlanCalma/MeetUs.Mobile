package com.example.yun.meetup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yun.meetup.R;
import com.example.yun.meetup.interfaces.RemoveMemberCallback;
import com.example.yun.meetup.models.UserInfo;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by danie on 12/22/2017.
 */

public class MemberListViewAdapter extends BaseAdapter {

    private final List<UserInfo> mUsers;
    private Context mContext;
    private Boolean mIsHost;
    private RemoveMemberCallback mRemoveMemberCallback;

    public class MembersListViewHolder {

        final TextView txtMemberName;
        final ImageButton btnRemoveMember;

        public MembersListViewHolder(View view) {
            txtMemberName = view.findViewById(R.id.txt_item_member_name);
            btnRemoveMember = view.findViewById(R.id.btn_remove_member_list);

        }
    }

    public MemberListViewAdapter(Boolean isHost, List<UserInfo> users, Context context, RemoveMemberCallback removeMemberCallback) {
        this.mUsers = users;
        this.mContext = context;
        this.mIsHost = isHost;
        this.mRemoveMemberCallback = removeMemberCallback;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return new BigInteger(mUsers.get(position).getName(), 16).longValue();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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


        holder.txtMemberName.setText(mUsers.get(position).getName());

        if (!mIsHost){
            holder.btnRemoveMember.setVisibility(View.GONE);
        }
        else{
            holder.btnRemoveMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRemoveMemberCallback.onRemoveMemberClicked(mUsers.get(position));
                }
            });
        }

        return view;
    }
}
