package com.example.yun.meetup.providers;

import android.graphics.Bitmap;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.InputStreamEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

/**
 * Created by alessio on 03-Dec-17.
 */

public class ApiProvider {

    private static final String BASE_URL = "https://meet-us-server1.herokuapp.com/api";

    public String sendRequest(String absoluteURL, String method, String body) throws  IOException {

        // If provided URL is partial, append internal API BASE_URL prefix
        String urlPath = absoluteURL.startsWith("http") ? absoluteURL : getAbsoluteUrl(absoluteURL);

        StringBuilder result = new StringBuilder();

        BufferedWriter bufferedWriter = null;
        BufferedReader bufferedReader = null;

        // Initialize and config request, then connect to server
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.connect();

        //Write data into server
        if(body != null) {
            OutputStream outputStream = urlConnection.getOutputStream();
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(body);
            bufferedWriter.flush();
        }

        // read response from server
        InputStream inputStream = urlConnection.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }

        if(bufferedReader != null){
            bufferedReader.close();
        }
        if(bufferedWriter != null){
            bufferedWriter.close();
        }

        return result.toString();
    }

//    public String sendMultipartFormRequest(String absoluteURL, String method, File file) throws IOException {
//
//        String urlPath = absoluteURL.startsWith("http") ? absoluteURL : getAbsoluteUrl(absoluteURL);
//
//        HttpClient httpclient = new DefaultHttpClient();
//
//        HttpPost httppost = new HttpPost(urlPath);
//
//        InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
//        reqEntity.setContentType("binary/octet-stream");
//        reqEntity.setChunked(true); // Send in multiple parts if needed
//        httppost.setEntity(reqEntity);
//        HttpResponse response = httpclient.execute(httppost);
//
//        return response.toString();
//    }

    public String sendMultipartFormRequest(String absoluteURL, File file) throws Exception {

        String urlPath = absoluteURL.startsWith("http") ? absoluteURL : getAbsoluteUrl(absoluteURL);

        String type = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        String filename = file.getName();

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;


        FileInputStream fileInputStream = new FileInputStream(file);

        URL url = new URL(urlPath);
        connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + filename + "\"" + lineEnd);
        outputStream.writeBytes("Content-Type: " + type + lineEnd);
        outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

        outputStream.writeBytes(lineEnd);

        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            outputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        outputStream.writeBytes(lineEnd);

        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


        if (200 != connection.getResponseCode()) {
            throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        inputStream = connection.getInputStream();

        result = convertStreamToString(inputStream);

        fileInputStream.close();
        inputStream.close();
        outputStream.flush();
        outputStream.close();

        return result;


    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
