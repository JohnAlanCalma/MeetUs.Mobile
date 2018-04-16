package com.example.yun.meetup.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yun.meetup.R;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Comment;

import org.joda.time.DateTime;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListViewAdapter extends BaseAdapter {

    private final List<Comment> mComments;
    private Context mContext;
    private CommentListViewHolder holder;

    public class CommentListViewHolder {

//        final CircleImageView circleImageViewProfileComment;
        final TextView txtMemberName;
        final TextView txtDate;
        final TextView txtComment;

        public CommentListViewHolder(View view) {

//            circleImageViewProfileComment = view.findViewById(R.id.comment_list_profile_image);
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

        if(convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.lv_comments_item, parent, false);
            holder = new CommentListViewHolder(view);
            view.setTag(holder);
        }else{
            view = convertView;
            holder = (CommentListViewHolder) view.getTag();
        }

        holder.txtMemberName.setText(mComments.get(position).getUserInfo().getName());

        String dateText = "more than 1 year ago";



        long msDifference = Math.abs(DateTime.now().getMillis() - mComments.get(position).getCreationDate().getMillis());

        if (msDifference < 60000) {
            dateText = "less than a minute ago";
        }
        else if (msDifference < 3600000){
            if ((msDifference / 60000) >= 1 && (msDifference / 60000) < 2) {
                dateText = "1 minute ago";
            }
            else{
                dateText = (int) Math.floor((msDifference / 60000)) + " minutes ago";
            }
        }
        else if (msDifference < 86400000){
            if ((msDifference / 3600000) >= 1 && (msDifference / 3600000) < 2){
                dateText = "1 hour ago";
            }
            else{
                dateText = (int) Math.floor((msDifference / 3600000)) + " hours ago";
            }
        }
        else if (msDifference < 2628000000L){
            if ((msDifference / 86400000) >= 1 && (msDifference / 86400000) < 2){
                dateText = "1 day ago";
            }
            else{
                dateText = (int) Math.floor((msDifference / 86400000)) + " days ago";
            }
        }
        else if (msDifference < 31540000000L){
            if ((msDifference / 2628000000L) >= 1 && (msDifference / 2628000000L) < 2){
                dateText = "1 month ago";
            }
            else{
                dateText = (int) Math.floor((msDifference / 2628000000L)) + " months ago";
            }
        }

        holder.txtDate.setText(dateText);
        holder.txtComment.setText(mComments.get(position).getComment());

        new DownloadImageTask().execute("https://meet-us-server1.herokuapp.com/api/user/photo/?user_id=" + mComments.get(position).getUser_id());

        return view;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urldisplay = strings[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null){
//                holder.circleImageViewProfileComment.setImageBitmap(bitmap);
            }
        }
    }
}
