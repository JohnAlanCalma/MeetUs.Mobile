package com.example.yun.meetup.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yun.meetup.R;
import com.example.yun.meetup.managers.NetworkManager;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.UserInfo;
import com.example.yun.meetup.requests.UpdateProfileRequest;
import com.google.android.gms.maps.model.Circle;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 2;
    private static final String[] PERMISSIONS_TO_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private TextView txtUserDescription;
    private TextView txtUserInterests;
    private TextView txtUserName;

    private String mDescription = "";
    private String mInterests = "";
    private String userId;

    private CircleImageView circleImageViewProfile;

    private ConstraintLayout layoutLoading;

    private static int PICKIMAGE_REQUESTCODE = 1;

    private Bitmap mPhotoSelected;

    private UserInfo mUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        layoutLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutProfileLoading);

        layoutLoading.setVisibility(View.GONE);

        txtUserDescription = findViewById(R.id.txt_user_description);
        txtUserInterests = findViewById(R.id.txt_user_interest);
        txtUserName = (TextView) findViewById(R.id.txt_user_name);

        circleImageViewProfile = (CircleImageView) findViewById(R.id.profile_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        userId = sharedPref.getString("id", "");

        if (!userId.isEmpty()){
            new GetUserProfileTask().execute(userId);
        }

    }

    public void insertDescription(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert a Description");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDescription = input.getText().toString();
                txtUserDescription.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void insertInterests(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert your interests");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mInterests = input.getText().toString();
                txtUserInterests.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void handleOnClickPhoto(View view) {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, PERMISSIONS_TO_REQUEST, REQUEST_PERMISSIONS);

        } else {
            Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImageIntent, PICKIMAGE_REQUESTCODE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKIMAGE_REQUESTCODE) {
            if (resultCode == RESULT_OK) {

                Uri tempUri = data.getData();

                File finalFile = new File(getRealPathFromURI(tempUri));

                new UploadPhotoTask().execute(finalFile);
            }
        }
        else if (requestCode == REQUEST_PERMISSIONS){
            if (resultCode == RESULT_OK){
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent, PICKIMAGE_REQUESTCODE);
            }
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public void handleOnClickUpdateProfile(View view) {
        layoutLoading.setVisibility(View.VISIBLE);

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setUser_id(userId);
        updateProfileRequest.setDescription(mDescription);
        updateProfileRequest.setInterests(mInterests);

        new UpdateProfileTask().execute(updateProfileRequest);
    }

    private class GetUserProfileTask extends AsyncTask<String, Void, APIResult>{

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.getUserById(strings[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (!apiResult.isResultSuccess()){
                Toast.makeText(UserProfileActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                mUserInfo = (UserInfo) apiResult.getResultEntity();

                txtUserName.setText(mUserInfo.getName());

                if (!mUserInfo.getDescription().equals("") && !mUserInfo.getDescription().isEmpty()){
                    txtUserDescription.setText(mUserInfo.getDescription());
                    mDescription = mUserInfo.getDescription();
                }

                if (!mUserInfo.getInterests().equals("") && !mUserInfo.getInterests().isEmpty()){
                    txtUserInterests.setText(mUserInfo.getInterests());
                    mInterests = mUserInfo.getInterests();
                }

                String photoURL = "https://meet-us-server1.herokuapp.com/api/user/photo/?user_id=" + userId;

                Picasso.get()
                        .load(photoURL)
                        .fit()
                        .centerInside()
                        .rotate(270)
                        .placeholder(R.drawable.main_background)
                        .error(R.drawable.main_background)
                        .into(circleImageViewProfile);

                layoutLoading.setVisibility(View.GONE);
            }
        }
    }

    private class UploadPhotoTask extends AsyncTask<File, Void, APIResult>{

        @Override
        protected APIResult doInBackground(File...files) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.uploadPhoto(files[0], userId);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            layoutLoading.setVisibility(View.GONE);

            if (!apiResult.isResultSuccess()){
                Toast.makeText(UserProfileActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{

                String photoURL = "https://meet-us-server1.herokuapp.com/api/user/photo/?user_id=" + userId;

                Picasso.get()
                        .load(photoURL)
                        .fit()
                        .centerInside()
                        .rotate(270)
                        .placeholder(R.drawable.main_background)
                        .error(R.drawable.main_background)
                        .into(circleImageViewProfile);
            }
        }
    }

    private class UpdateProfileTask extends AsyncTask<UpdateProfileRequest, Void, APIResult>{

        @Override
        protected APIResult doInBackground(UpdateProfileRequest... updateProfileRequests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.updateProfile(updateProfileRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            layoutLoading.setVisibility(View.GONE);

            if (!apiResult.isResultSuccess()){
                Toast.makeText(UserProfileActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                UserInfo userInfo = (UserInfo) apiResult.getResultEntity();

                txtUserDescription.setText(userInfo.getDescription());
                txtUserInterests.setText(userInfo.getInterests());
            }
        }
    }
}
