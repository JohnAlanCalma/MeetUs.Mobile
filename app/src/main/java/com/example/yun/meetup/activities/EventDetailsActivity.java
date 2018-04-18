package com.example.yun.meetup.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yun.meetup.R;
import com.example.yun.meetup.adapters.CommentListViewAdapter;
import com.example.yun.meetup.adapters.MemberListViewAdapter;
import com.example.yun.meetup.interfaces.RemoveMemberCallback;
import com.example.yun.meetup.managers.NetworkManager;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Comment;
import com.example.yun.meetup.models.Event;
import com.example.yun.meetup.models.UserInfo;
import com.example.yun.meetup.requests.AddCommentRequest;
import com.example.yun.meetup.requests.ParticipateToEventRequest;
import com.example.yun.meetup.requests.UnsubscribeRequest;

import org.joda.time.DateTime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class EventDetailsActivity extends AppCompatActivity {

    static final int UPDATE_EVENT_REQUEST = 1;  // The request code
    MemberListViewAdapter listviewMembersAdapter;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton fabParticipate;
    private FloatingActionButton fabEdit;
    private FloatingActionButton fabDelete;
    private ConstraintLayout constraintLayoutDetailsLoading;
    private LinearLayout linearLayoutComments;
    private TextView textViewDetailAddress;
    private TextView textViewDetailDate;
    private TextView textViewDetailHostName;
    private TextView textViewDetailSubtitle;
    private TextView textViewDetailDescription;
    private TextView textViewDetailCategory;
    private TextView textViewAddComment;
    private EditText editTextComment;
    private ListView listViewSubscribedUsers;
    private ListView listViewComemnts;
    private Button buttonAddComment;
    private String userId;
    private String eventId;
    private Event event;
    private FloatingActionButton fabUnsubscribe;
    private ImageView imgHeader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        constraintLayoutDetailsLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutDetailsLoading);
        linearLayoutComments = (LinearLayout) findViewById(R.id.linear_layout_comments);

        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        fabParticipate = findViewById(R.id.fab_event_detail_participate);
        fabUnsubscribe = (FloatingActionButton) findViewById(R.id.fab_event_detail_unsubscribe);
        fabEdit = (FloatingActionButton) findViewById(R.id.fab_edit_event_details);
        fabDelete = (FloatingActionButton) findViewById(R.id.fab_delete_event_details);

        textViewDetailAddress = (TextView) findViewById(R.id.txt_detail_event_address);
        textViewDetailDate = (TextView) findViewById(R.id.txt_detail_event_date);
        textViewDetailHostName = (TextView) findViewById(R.id.txt_detail_event_host);
        textViewDetailSubtitle = (TextView) findViewById(R.id.txt_subtitle);
        textViewDetailDescription = (TextView) findViewById(R.id.txt_description);
        textViewDetailCategory = (TextView) findViewById(R.id.txt_detail_event_category);
        editTextComment = (EditText) findViewById(R.id.edt_comment);
        listViewSubscribedUsers = (ListView) findViewById(R.id.lv_detail_subscribed_users);
        buttonAddComment = (Button) findViewById(R.id.btn_add_comment);


        imgHeader = (ImageView) findViewById(R.id.header_image);

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("id", null);


        eventId = getIntent().getExtras().getString("eventId");

//        This code should run after get the EVENT NAME or from Intent or Database
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        new GetEventTask().execute(eventId);
    }

    @Override
    protected void onResume() {
        showLoading();
        new GetEventTask().execute(eventId);
        super.onResume();
    }

    public void hideLoading() {
        constraintLayoutDetailsLoading.setVisibility(View.GONE);
        fabDelete.setClickable(true);
        fabEdit.setClickable(true);
        fabParticipate.setClickable(true);
        fabUnsubscribe.setClickable(true);
        editTextComment.setClickable(true);
        buttonAddComment.setClickable(true);
    }

    public void showLoading() {
        constraintLayoutDetailsLoading.setVisibility(View.VISIBLE);
        fabDelete.setClickable(false);
        fabEdit.setClickable(false);
        fabParticipate.setClickable(false);
        fabUnsubscribe.setClickable(false);
        editTextComment.setClickable(false);
        buttonAddComment.setClickable(false);
    }
    // TODO: Implement the unsubscribe.
    public void handleOnClickParticipate(View view) {

        showLoading();

        ParticipateToEventRequest participateToEventRequest = new ParticipateToEventRequest();
        participateToEventRequest.setEvent_id(eventId);
        participateToEventRequest.setUser_id(userId);
        new ParticipateToEventTask().execute(participateToEventRequest);


    }

    public void handleOnClickUpdate(View view) {
        Intent intent = new Intent(this, EventUpdateActivity.class);
        intent.putExtra("event", event);
        startActivityForResult(intent, UPDATE_EVENT_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_EVENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                showLoading();
                new GetEventTask().execute(eventId);
            }
        }
    }

    public void handleOnClickDelete(View view) {
        new DeleteEventTask().execute(eventId);
    }

    public void handleOnClickUnsubscribe(View view) {
        showLoading();

        UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
        unsubscribeRequest.setEvent_id(eventId);
        unsubscribeRequest.setUser_id(userId);
        new UnsubscribeTask().execute(unsubscribeRequest);
    }

    public void handleOnClickAddComment(View view) {

        if (!editTextComment.getText().toString().isEmpty() && editTextComment.getText().toString() != ""){

            showLoading();

            AddCommentRequest addCommentRequest = new AddCommentRequest();
            addCommentRequest.setEvent_id(eventId);
            addCommentRequest.setUser_id(userId);
            addCommentRequest.setText(editTextComment.getText().toString());

            editTextComment.setText("");

            new AddCommentTask().execute(addCommentRequest);
        }


    }

    private class GetEventTask extends AsyncTask<String, Void, APIResult> {

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.getEventById(strings[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (apiResult.getResultEntity() == null) {
                Toast.makeText(EventDetailsActivity.this, "Error retrieving details of event: please try again", Toast.LENGTH_LONG).show();
            } else {
                event = (Event) apiResult.getResultEntity();

                collapsingToolbarLayout.setTitle(event.getTitle().toUpperCase());

                textViewDetailAddress.setText(event.getAddress());
                textViewDetailDate.setText(event.getDate());
                textViewDetailHostName.setText(event.getUserInfo().getName());
                textViewDetailSubtitle.setText(event.getSubtitle());
                textViewDetailDescription.setText(event.getDescription());
                textViewDetailCategory.setText(event.getCategory());

                List<UserInfo> listSubscribedUsers = new ArrayList<>();

                boolean isMember = false;

                for (UserInfo member : event.getMembers()) {

                    listSubscribedUsers.add(member);

                    if (userId.equals(member.get_id())) {
                        isMember = true;
                        break;
                    }
                }

                if (isMember){
                    fabParticipate.setVisibility(View.GONE);
                    fabUnsubscribe.setVisibility(View.VISIBLE);
                }
                else{
                    fabParticipate.setVisibility(View.VISIBLE);
                    fabUnsubscribe.setVisibility(View.GONE);
                }

                // Members list

                Boolean isHost = userId.equals(event.getHost_id());

                if (isHost) {
                    fabParticipate.setVisibility(View.GONE);
                } else {
                    fabEdit.setVisibility(View.GONE);
                    fabDelete.setVisibility(View.GONE);
                }

                if (!isHost && !isMember){
                    editTextComment.setVisibility(View.GONE);
                    buttonAddComment.setVisibility(View.GONE);
                }

                listviewMembersAdapter = new MemberListViewAdapter(isHost, listSubscribedUsers, getApplicationContext(), new MyRemoveMemberCallback());
                listViewSubscribedUsers.setAdapter(listviewMembersAdapter);

                new GetCommentsTask().execute(eventId);

            }
        }
    }

    private class ParticipateToEventTask extends AsyncTask<ParticipateToEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(ParticipateToEventRequest... participateToEventRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.participateToEvent(participateToEventRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (!apiResult.isResultSuccess()) {
                hideLoading();
                Toast.makeText(EventDetailsActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG).show();
            } else {
                new GetEventTask().execute(eventId);
            }
        }
    }

    private class DeleteEventTask extends AsyncTask<String, Void, APIResult>{

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.deleteEvent(strings[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {
            if (!apiResult.isResultSuccess()){
                Toast.makeText(EventDetailsActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG).show();
            }
            else{
                Event event = (Event) apiResult.getResultEntity();

                Toast.makeText(EventDetailsActivity.this, "Event deleted successfully!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class UnsubscribeTask extends AsyncTask<UnsubscribeRequest, Void, APIResult>{

        @Override
        protected APIResult doInBackground(UnsubscribeRequest... unsubscribeRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.unsubscribeFromEvent(unsubscribeRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {
            if (!apiResult.isResultSuccess()) {
                hideLoading();
                Toast.makeText(EventDetailsActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG).show();
            } else {
                new GetEventTask().execute(eventId);
            }
        }
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
            hideLoading();

            if (bitmap != null){
                imgHeader.setImageBitmap(bitmap);
            }
        }
    }

    private class MyRemoveMemberCallback implements RemoveMemberCallback{

        @Override
        public void onRemoveMemberClicked(UserInfo userInfo) {
            UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
            unsubscribeRequest.setEvent_id(eventId);
            unsubscribeRequest.setUser_id(userInfo.get_id());
            new UnsubscribeTask().execute(unsubscribeRequest);
        }
    }

    private class GetCommentsTask extends AsyncTask<String, Void, APIResult>{

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.getEventComments(strings[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            linearLayoutComments.removeAllViews();

            if (apiResult.isResultSuccess()){

                List<Comment> comments = (List<Comment>) apiResult.getResultEntity();

                for (Comment comment : comments){
                    View viewComment = getLayoutInflater().inflate(R.layout.lv_comments_item, null);

//                    CircleImageView circleImageViewProfileComment = viewComment.findViewById(R.id.comment_list_profile_image);
                    TextView txtMemberName = viewComment.findViewById(R.id.item_comment_username);
                    TextView txtDate = viewComment.findViewById(R.id.item_comment_date);
                    TextView txtComment = viewComment.findViewById(R.id.lv_content_comment);

                    String dateText = "more than 1 year ago";

                    DateTime currentDate = DateTime.now();

                    long msDifference = Math.abs(currentDate.getMillis() - comment.getCreationDate().getMillis());

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

                    txtMemberName.setText(comment.getUserInfo().getName());
                    txtDate.setText(dateText);
                    txtComment.setText(comment.getComment());

                    linearLayoutComments.addView(viewComment);

//                    DownloadCommentUserImageTask task = new DownloadCommentUserImageTask();
//                    task.setCircleImageViewProfileComment(circleImageViewProfileComment);
//                    task.execute("https://meet-us-server1.herokuapp.com/api/user/photo/?user_id=" + comment.getUser_id());
                }



//                CommentListViewAdapter commentListViewAdapter = new CommentListViewAdapter(comments, EventDetailsActivity.this);
//                listViewComemnts.setAdapter(commentListViewAdapter);
            }

            new DownloadImageTask().execute("https://meet-us-server1.herokuapp.com/api/event/photo/?event_id=" + eventId);
        }
    }

    private class DownloadCommentUserImageTask extends AsyncTask<String, Void, Bitmap>{

        private CircleImageView circleImageViewProfileComment;

        public CircleImageView getCircleImageViewProfileComment() {
            return circleImageViewProfileComment;
        }

        public void setCircleImageViewProfileComment(CircleImageView circleImageViewProfileComment) {
            this.circleImageViewProfileComment = circleImageViewProfileComment;
        }

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
                circleImageViewProfileComment.setImageBitmap(bitmap);
            }
        }
    }

    private class AddCommentTask extends AsyncTask<AddCommentRequest, Void, APIResult>{

        @Override
        protected APIResult doInBackground(AddCommentRequest... addCommentRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.addComment(addCommentRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (!apiResult.isResultSuccess()){
                hideLoading();
                Toast.makeText(EventDetailsActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                new GetEventTask().execute(eventId);
            }

        }
    }
}
