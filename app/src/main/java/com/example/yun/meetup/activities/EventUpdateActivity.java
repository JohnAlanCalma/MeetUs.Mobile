package com.example.yun.meetup.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
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
import com.example.yun.meetup.requests.UpdateEventRequest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventUpdateActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 2;
    private static final String[] PERMISSIONS_TO_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PICKIMAGE_REQUESTCODE = 1;

    private Calendar calendar = Calendar.getInstance();

    private EditText editTextUpdateEventDate;
    private EditText edt_update_event_title, edt_update_event_subtitle, edt_update_event_address, edt_update_event_description;

    private TextView txt_error_update_event_title, txt_error_update_event_date, txt_error_update_event_address, txt_error_update_event, txt_error_update_event_category;
    private Event event;

    private Spinner spinnerCategory;

    private File mFile;
    private String mCategory;
    private List<String> mCategoryList;

    private ConstraintLayout constraintLayoutLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_update);

        constraintLayoutLoading = findViewById(R.id.constraintLayoutLoading);

        event = (Event) getIntent().getExtras().get("event");

        editTextUpdateEventDate = findViewById(R.id.edt_update_event_date);
        editTextUpdateEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
        editTextUpdateEventDate.setText(event.getDate());

        edt_update_event_title = findViewById(R.id.edt_update_event_title);
        edt_update_event_title.setText(event.getTitle());

        edt_update_event_subtitle = findViewById(R.id.edt_update_event_subtitle);
        edt_update_event_subtitle.setText(event.getSubtitle());

        edt_update_event_address = findViewById(R.id.edt_update_event_address);
        edt_update_event_address.setText(event.getAddress());

        edt_update_event_description = findViewById(R.id.edt_update_event_description);
        edt_update_event_description.setText(event.getDescription());

        txt_error_update_event_title = findViewById(R.id.txt_error_update_event_title);
        txt_error_update_event_date = findViewById(R.id.txt_error_update_event_date);
        txt_error_update_event_address = findViewById(R.id.txt_error_update_event_address);
        txt_error_update_event_category = findViewById(R.id.txt_error_update_event_category);
        txt_error_update_event = findViewById(R.id.txt_error_update_event);

        spinnerCategory = findViewById(R.id.spinner_category_update_event);

        String[] categoryArray = getResources().getStringArray(R.array.category_array);
        mCategoryList = Arrays.asList(categoryArray);

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
        new DatePickerDialog(EventUpdateActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(EventUpdateActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.CANADA);

                        editTextUpdateEventDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
    }

    public void hideViews() {
        constraintLayoutLoading.setVisibility(View.GONE);
        txt_error_update_event_title.setVisibility(View.GONE);
        txt_error_update_event_date.setVisibility(View.GONE);
        txt_error_update_event_address.setVisibility(View.GONE);
        txt_error_update_event.setVisibility(View.GONE);
    }

    public void handleOnClickUpdate(View view) {


        boolean error = false;

        if (edt_update_event_title.getText().toString().isEmpty()) {
            txt_error_update_event_title.setText("Please provide a valid Title for your event!");
            txt_error_update_event_title.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextUpdateEventDate.getText().toString().isEmpty()) {
            txt_error_update_event_date.setText("Please provide a Date for your event!");
            txt_error_update_event_date.setVisibility(View.VISIBLE);
            error = true;
        }
        if (edt_update_event_address.getText().toString().isEmpty()) {
            txt_error_update_event_address.setText("Please provide an Address for your event!");
            txt_error_update_event_address.setVisibility(View.VISIBLE);
            error = true;
        }
        if (mCategory == "" || mCategory.isEmpty()) {
            txt_error_update_event_category.setText("Please select a category for your event!");
            txt_error_update_event_category.setVisibility(View.VISIBLE);
            error = true;
        }

        if (!error) {
            constraintLayoutLoading.setVisibility(View.VISIBLE);
            UpdateEventRequest updateEventRequest = new UpdateEventRequest();
            updateEventRequest.set_id(event.get_id());
            updateEventRequest.setTitle(edt_update_event_title.getText().toString());
            updateEventRequest.setSubtitle(edt_update_event_subtitle.getText().toString());
            updateEventRequest.setDescription(edt_update_event_description.getText().toString());
            updateEventRequest.setDate(editTextUpdateEventDate.getText().toString());
            updateEventRequest.setAddress(edt_update_event_address.getText().toString());
            new ValidateAddressTask().execute(updateEventRequest);
        }

    }

    public void handleOnClickUpdateEventPhoto(View view) {
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

    private class ValidateAddressTask extends AsyncTask<UpdateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(UpdateEventRequest... requests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.validateEventAddress(requests[0]);
        }

        @Override
        protected void onPostExecute(APIResult result) {
            if (result.isResultSuccess()) {
                new UpdateEventTask().execute((UpdateEventRequest) result.getResultEntity());
            } else {
                hideViews();
                txt_error_update_event_address.setText(result != null ? result.getResultMessage() : "Please contact admin staff!");
                txt_error_update_event_address.setVisibility(View.VISIBLE);
            }
        }
    }

    private class UpdateEventTask extends AsyncTask<UpdateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(UpdateEventRequest... requests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.updateEvent(requests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            if (apiResult.isResultSuccess()) {
                new UploadPhotoTask().execute(mFile);
            } else {
                txt_error_update_event.setVisibility(View.VISIBLE);
                txt_error_update_event.setText(apiResult != null ? apiResult.getResultMessage() : "Please contact admin staff!");
            }
        }
    }

    private class UploadPhotoTask extends AsyncTask<File, Void, APIResult>{

        @Override
        protected APIResult doInBackground(File...files) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.uploadEventPhoto(files[0], event.get_id());
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            hideViews();

            if (!apiResult.isResultSuccess()){
                Toast.makeText(EventUpdateActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                Intent returnIntent = getIntent();
                setResult(Activity.RESULT_OK, returnIntent);
                EventUpdateActivity.this.finish();
            }
        }
    }
}
