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
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminUpdateEventActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 2;
    private static final String[] PERMISSIONS_TO_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PICKIMAGE_REQUESTCODE = 1;

    private EditText editTextAdminUpdateTitle;
    private EditText editTextAdminUpdateDate;
    private EditText editTextAdminUpdateAddress;
    private EditText editTextAdminUpdateDescription;
    private EditText editTextAdminUpdateSubtitle;

    private TextView textViewErrorAdminTitle;
    private TextView textViewErrorAdminDate;
    private TextView textViewErrorAdminAddress;
    private TextView textViewErrorAdminUpdate;
    private TextView textViewErrorAdminCategory;

    private Spinner spinnerCategory;

    private ConstraintLayout constraintLayoutAdminUpdateLoading;

    private String event_id;

    private Calendar calendar = Calendar.getInstance();

    private Event mEvent;
    private File mFile;
    private String mCategory = "";
    private List<String> mCategoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_update_event);

        editTextAdminUpdateTitle = (EditText) findViewById(R.id.edt_admin_update_event_title);
        editTextAdminUpdateDate = (EditText) findViewById(R.id.edt_admin_update_event_date);
        editTextAdminUpdateAddress = (EditText) findViewById(R.id.edt_admin_update_event_address);
        editTextAdminUpdateDescription = (EditText) findViewById(R.id.edt_admin_update_event_description);
        editTextAdminUpdateSubtitle = (EditText) findViewById(R.id.edt_admin_update_event_subtitle);

        textViewErrorAdminTitle = (TextView) findViewById(R.id.txt_admin_error_update_event_title);
        textViewErrorAdminDate = (TextView) findViewById(R.id.txt_admin_error_update_event_date);
        textViewErrorAdminAddress = (TextView) findViewById(R.id.txt_admin_error_update_event_address);
        textViewErrorAdminCategory = (TextView) findViewById(R.id.txt_admin_error_update_event_category);
        textViewErrorAdminUpdate = (TextView) findViewById(R.id.txt_admin_error_update_event);

        editTextAdminUpdateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        constraintLayoutAdminUpdateLoading = (ConstraintLayout) findViewById(R.id.constraintLayoutAdminUpdateLoading);

        event_id = getIntent().getExtras().getString("event_id");

        spinnerCategory = findViewById(R.id.spinner_category_admin_update_event);

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

        new AdminEventDetailsTask().execute(event_id);


    }

    private void showDateTimePicker() {
         /*
        * Making the DateTimePicker
        * */
        new DatePickerDialog(AdminUpdateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(AdminUpdateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm", Locale.CANADA);

                        editTextAdminUpdateDate.setText(sdf.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
    }

    public void hideViews(){
        constraintLayoutAdminUpdateLoading.setVisibility(View.GONE);
        textViewErrorAdminTitle.setVisibility(View.GONE);
        textViewErrorAdminDate.setVisibility(View.GONE);
        textViewErrorAdminAddress.setVisibility(View.GONE);
        textViewErrorAdminCategory.setVisibility(View.GONE);
        textViewErrorAdminUpdate.setVisibility(View.GONE);
    }

    public void handleOnClickAdminUpdate(View view) {

        boolean error = false;

        if (editTextAdminUpdateTitle.getText().toString().isEmpty()) {
            textViewErrorAdminTitle.setText("Please provide a valid Title for your event!");
            textViewErrorAdminTitle.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextAdminUpdateDate.getText().toString().isEmpty()) {
            textViewErrorAdminDate.setText("Please provide a Date for your event!");
            textViewErrorAdminDate.setVisibility(View.VISIBLE);
            error = true;
        }
        if (editTextAdminUpdateAddress.getText().toString().isEmpty()) {
            textViewErrorAdminAddress.setText("Please provide an Address for your event!");
            textViewErrorAdminAddress.setVisibility(View.VISIBLE);
            error = true;
        }
        if (mCategory == "" || mCategory.isEmpty()) {
            textViewErrorAdminCategory.setText("Please select a category for your event!");
            textViewErrorAdminCategory.setVisibility(View.VISIBLE);
            error = true;
        }

        if (!error) {
            constraintLayoutAdminUpdateLoading.setVisibility(View.VISIBLE);
            UpdateEventRequest updateEventRequest = new UpdateEventRequest();
            updateEventRequest.set_id(event_id);
            updateEventRequest.setTitle(editTextAdminUpdateTitle.getText().toString());
            updateEventRequest.setSubtitle(editTextAdminUpdateSubtitle.getText().toString());
            updateEventRequest.setDescription(editTextAdminUpdateDescription.getText().toString());
            updateEventRequest.setDate(editTextAdminUpdateDate.getText().toString());
            updateEventRequest.setAddress(editTextAdminUpdateAddress.getText().toString());
            new AdminUpdateEventActivity.AdminValidateAddressTask().execute(updateEventRequest);
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

    private class AdminEventDetailsTask extends AsyncTask<String, Void, APIResult>{

        @Override
        protected APIResult doInBackground(String... strings) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.getEventById(event_id);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            hideViews();

            if(!apiResult.isResultSuccess()){
                Toast.makeText(AdminUpdateEventActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG).show();
            }
            else{
                mEvent = (Event) apiResult.getResultEntity();

                editTextAdminUpdateTitle.setText(mEvent.getTitle());
                editTextAdminUpdateAddress.setText(mEvent.getAddress());
                editTextAdminUpdateDate.setText(mEvent.getDate());
                editTextAdminUpdateDescription.setText(mEvent.getDescription());
                editTextAdminUpdateSubtitle.setText(mEvent.getSubtitle());
                String category = mEvent.getCategory();

                int position = mCategoryList.indexOf(category);

                spinnerCategory.setSelection(position);

            }
        }
    }

    private class AdminValidateAddressTask extends AsyncTask<UpdateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(UpdateEventRequest... requests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.validateEventAddress(requests[0]);
        }

        @Override
        protected void onPostExecute(APIResult result) {
            if (result.isResultSuccess()) {
                new AdminUpdateEventActivity.AdminUpdateEventTask().execute((UpdateEventRequest) result.getResultEntity());
            }
            else {
                hideViews();
                Toast.makeText(AdminUpdateEventActivity.this, result.getResultMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AdminUpdateEventTask extends AsyncTask<UpdateEventRequest, Void, APIResult> {

        @Override
        protected APIResult doInBackground(UpdateEventRequest... requests) {
            NetworkManager networkManager = new NetworkManager();
            return networkManager.updateEvent(requests[0]);
        }

        @Override
        protected void onPostExecute(APIResult apiResult) {

            hideViews();

            if (apiResult.isResultSuccess()) {
                new UploadPhotoTask().execute(mFile);
            }
            else {
                Toast.makeText(AdminUpdateEventActivity.this, apiResult.getResultMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(AdminUpdateEventActivity.this, apiResult.getResultMessage(), Toast.LENGTH_SHORT).show();
            }
            else{
                Intent returnIntent = getIntent();
                setResult(Activity.RESULT_OK, returnIntent);
                AdminUpdateEventActivity.this.finish();
            }
        }
    }
}
