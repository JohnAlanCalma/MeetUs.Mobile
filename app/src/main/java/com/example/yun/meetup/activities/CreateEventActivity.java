package com.example.yun.meetup.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yun.meetup.R;
import com.example.yun.meetup.managers.NetworkManager;
import com.example.yun.meetup.models.APIResult;
import com.example.yun.meetup.models.Event;
import com.example.yun.meetup.models.UserInfo;
import com.example.yun.meetup.requests.CreateEventRequest;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayoutLoading;

    private EditText editTextCreateEventTitle;
    private EditText editTextCreateEventSubtitle;
    private EditText editTextCreateEventDate;
    private EditText editTextCreateEventAddress;
    private EditText editTextCreateEventDescription;

    private TextView textViewErrorCreateEventTitle;
    private TextView textViewErrorCreateEventDate;
    private TextView textViewErrorCreateEventAddress;
    private TextView textViewErrorCreateEventCategory;
    private TextView textViewErrorCreateEvent;
    private Spinner spinnerCategory;

    private FloatingActionButton fabPhoto;

    private Button buttonCreate;

    private static final int REQUEST_PERMISSIONS = 2;
    private static final String[] PERMISSIONS_TO_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PICKIMAGE_REQUESTCODE = 1;

    private File mFile;
    private Event mEvent;
    private String mCategory = "";
    private List<String> mCategoryList;


    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        fabPhoto = findViewById(R.id.fab_create_photo);

        buttonCreate = (Button) findViewById(R.id.buttonCreateEvent);

        constraintLayoutLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutLoading);

        editTextCreateEventTitle = (EditText) findViewById(R.id.editTextCreateEventTitle);
        textViewErrorCreateEventTitle = (TextView) findViewById(R.id.textViewErrorCreateEventTitle);

        editTextCreateEventSubtitle = (EditText) findViewById(R.id.editTextCreateEventSubtitle);

        editTextCreateEventDate = (EditText) findViewById(R.id.editTextCreateEventDate);
        editTextCreateEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
        textViewErrorCreateEventDate = (TextView) findViewById(R.id.textViewErrorCreateEventDate);

        editTextCreateEventAddress = (EditText) findViewById(R.id.editTextCreateEventAddress);
        textViewErrorCreateEventAddress = (TextView) findViewById(R.id.textViewErrorCreateEventAddress);

        textViewErrorCreateEventCategory = (TextView) findViewById(R.id.textViewErrorCreateEventCategory);

        editTextCreateEventDescription = (EditText) findViewById(R.id.editTextCreateEventDescription);

        textViewErrorCreateEvent = (TextView) findViewById(R.id.textViewErrorCreateEvent);

        spinnerCategory = findViewById(R.id.spinner_category_create_event);

        String[] categoryArray = getResources().getStringArray(R.array.category_array);
        mCategoryList = Arrays.asList(categoryArray);

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, mCategoryList);
//        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCategory = mCategoryList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });




    }

    private void showDateTimePicker() {
        /*
        * Making the DateTimePicker
        * */
        new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.CANADA);

                        editTextCreateEventDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
    }

    public void handleOnClickCreateEvent(View view) {
        hideLoading();

        boolean error = false;

        if (editTextCreateEventTitle.getText().toString().isEmpty()) {
            textViewErrorCreateEventTitle.setText("Please provide a valid Title for your event!");
            textViewErrorCreateEventTitle.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextCreateEventDate.getText().toString().isEmpty()) {
            textViewErrorCreateEventDate.setText("Please provide a Date for your event!");
            textViewErrorCreateEventDate.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextCreateEventAddress.getText().toString().isEmpty()) {
            textViewErrorCreateEventAddress.setText("Please provide an Address for your event!");
            textViewErrorCreateEventAddress.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextCreateEventAddress.getText().toString().isEmpty()) {
            textViewErrorCreateEventAddress.setText("Please provide an Address for your event!");
            textViewErrorCreateEventAddress.setVisibility(View.VISIBLE);
            error = true;
        }
        if (mCategory == "" || mCategory.isEmpty()){
            textViewErrorCreateEventCategory.setText("Please select a category for your event!");
            textViewErrorCreateEventCategory.setVisibility(View.VISIBLE);
            error = true;
        }

        if (!error) {
            CreateEventRequest createEventRequest = new CreateEventRequest();

            SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            createEventRequest.setHost_id(sharedPreferences.getString("id", null));
            createEventRequest.setTitle(editTextCreateEventTitle.getText().toString());
            createEventRequest.setSubtitle(editTextCreateEventSubtitle.getText().toString());
            createEventRequest.setDate(editTextCreateEventDate.getText().toString());
            createEventRequest.setAddress(editTextCreateEventAddress.getText().toString());
            createEventRequest.setDescription(editTextCreateEventDescription.getText().toString());
            createEventRequest.setCategory(mCategory);

            showLoading();

            new ValidateAddressTask().execute(createEventRequest);
        }

    }

    public void hideLoading() {
        textViewErrorCreateEventAddress.setVisibility(View.GONE);
        textViewErrorCreateEventDate.setVisibility(View.GONE);
        textViewErrorCreateEventTitle.setVisibility(View.GONE);
        constraintLayoutLoading.setVisibility(View.GONE);
        textViewErrorCreateEvent.setVisibility(View.GONE);
        textViewErrorCreateEventCategory.setVisibility(View.GONE);
        editTextCreateEventTitle.setClickable(true);
        editTextCreateEventSubtitle.setClickable(true);
        editTextCreateEventDescription.setClickable(true);
        editTextCreateEventAddress.setClickable(true);
        editTextCreateEventDate.setClickable(true);
        spinnerCategory.setClickable(true);
        fabPhoto.setClickable(true);
        buttonCreate.setClickable(true);
    }

    public void showLoading() {
        textViewErrorCreateEventAddress.setVisibility(View.GONE);
        textViewErrorCreateEventDate.setVisibility(View.GONE);
        textViewErrorCreateEventTitle.setVisibility(View.GONE);
        constraintLayoutLoading.setVisibility(View.GONE);
        textViewErrorCreateEvent.setVisibility(View.GONE);
        textViewErrorCreateEventCategory.setVisibility(View.GONE);
        editTextCreateEventTitle.setClickable(false);
        editTextCreateEventSubtitle.setClickable(false);
        editTextCreateEventDescription.setClickable(false);
        editTextCreateEventAddress.setClickable(false);
        editTextCreateEventDate.setClickable(false);
        spinnerCategory.setClickable(false);
        fabPhoto.setClickable(false);
        buttonCreate.setClickable(false);

    }

    public void handleOnClickEventPhoto(View view) {
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

                mFile = new File(getRealPathFromURI(tempUri));
            }
        }
        else if (requestCode == REQUEST_PERMISSIONS){
            if (resultCode == RESULT_OK){
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent, PICKIMAGE_REQUESTCODE);
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private class ValidateAddressTask extends AsyncTask<CreateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(CreateEventRequest... createEventRequests) {

            NetworkManager networkManager = new NetworkManager();
            return networkManager.validateEventAddress(createEventRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult result) {
            if (result.isResultSuccess()) {
                new CreateEventTask().execute((CreateEventRequest)result.getResultEntity());
            }
            else {
                hideLoading();
                textViewErrorCreateEventAddress.setText(result != null ? result.getResultMessage() : "Please contact admin staff!");
                textViewErrorCreateEventAddress.setVisibility(View.VISIBLE);
            }
        }
    }

    private class CreateEventTask extends AsyncTask<CreateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(CreateEventRequest... createEventRequests) {

            NetworkManager networkManager = new NetworkManager();
            return networkManager.createEvent(createEventRequests[0]);
        }

        @Override
        protected void onPostExecute(APIResult result) {

            hideLoading();

            if (result.isResultSuccess()) {
                Toast.makeText(CreateEventActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                mEvent = (Event) result.getResultEntity();
                new UploadPhotoTask().execute(mFile);

            } else {
                textViewErrorCreateEvent.setText(result != null ? result.getResultMessage() : "Please contact admin staff!");
                textViewErrorCreateEvent.setVisibility(View.VISIBLE);
            }
        }
    }

    private class UploadPhotoTask extends AsyncTask<File, Void, APIResult>{

        @Override
        protected APIResult doInBackground(File...files) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.uploadEventPhoto(files[0], mEvent.get_id());
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (!apiResult.isResultSuccess()){
                Toast.makeText(CreateEventActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();

                new DeleteEventTask().execute(mEvent.get_id());
            }
            else{
                hideLoading();
                CreateEventActivity.this.finish();
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
                Toast.makeText(CreateEventActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
                new DeleteEventTask().execute(mEvent.get_id());
            }
            else{
                hideLoading();
            }
        }
    }
}
