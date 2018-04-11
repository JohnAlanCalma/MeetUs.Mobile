package com.example.yun.meetup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yun.meetup.R;
import com.example.yun.meetup.models.Comment;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListViewAdapter extends BaseAdapter {

    private final List<Comment> mComments;
    private Context mContext;

    public class CommentListViewHolder {

        final CircleImageView circleImageViewProfileComment;
        final TextView txtMemberName;
        final TextView txtDate;
        final TextView txtComment;

        public CommentListViewHolder(View view) {

            circleImageViewProfileComment = view.findViewById(R.id.comment_list_profile_image);
            txtMemberName = view.findViewById(R.id.item_comment_username);
            txtDate = view.findViewById(R.id.item_comment_date);
            txtComment = view.findViewById(R.id.lv_content_comment);
        }
    }

    public CommentListViewAdapter(List<Comment> comments, Context context){
        this.mComments = comments;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        CommentListViewHolder holder;

        if(convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.lv_comments_item, parent, false);
            holder = new CommentListViewHolder(view);
            view.setTag(holder);
        }else{
            view = convertView;
            holder = (CommentListViewHolder) view.getTag();
        }


//        holder.circleImageViewProfileComment.setImageBitmap(mComments.get(position).getUserInfo().);


        holder.txtMemberName.setText(mComments.get(position).getUserInfo().getName());
        holder.txtDate.setText(mComments.get(position).getDate());
        holder.txtComment.setText(mComments.get(position).getContent());

        return view;
    }
}
