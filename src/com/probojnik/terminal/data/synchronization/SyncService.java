package com.probojnik.terminal.data.synchronization;

import android.app.IntentService;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import com.probojnik.terminal.data.sqlite.DataHelper;
import com.probojnik.terminal.util.Const;
import com.probojnik.terminal.util.UserPreferences;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author Stanislav Shamji
 */
public class SyncService extends IntentService {
    public static final String TAG = "SyncService";
    private static UserPreferences preferences;

    public SyncService() {
        super("SyncService");
        preferences = new UserPreferences(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Log.d(TAG, "Synchronization started...");
        Long start = System.currentTimeMillis();
        try {
            String id = preferences.getStringPreference(Const.PREF_TERMINAL_ID);
            String date = "DateSynhr=\"" + preferences.getStringPreference(Const.PREF_DATE_SYNC) + "\"";
            Log.d(TAG, "Terminal ID: " + id);
            Log.d(TAG, "Date: " + date);

            String request = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<request_type>" +
                    "<sinxronize terminalid=\"" + id + "\" DecSymbol=\".\" " + date + "/>" +
                    "</request_type>";

            InputStream is = getCompressedResponse(request, true);
            SyncEntry entry = new SyncParser().parseToDB(is, DataHelper.getInstance(this));
            if (entry.getStatusCode().equals("0")) {
                //Log.v(TAG, "Writing table content to file");
                //DataHelper.getInstance(this).listAllTables(new File(getFilesDir().getAbsolutePath() + "/tables"));
                preferences.setStringPreferences(Const.PREF_DATE_SYNC, entry.getTime());
            } else {
                Log.e(TAG, "Something went wrong: " + entry.getStatusDetail());
            }
            if (receiver != null) {
                sendResult(receiver, Integer.valueOf(entry.getStatusCode()), entry.getStatusDetail());
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't get entity from response!");
            sendResult(receiver, 1, e.getMessage());
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Malformed XML response");
            sendResult(receiver, 1, e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            Log.e(SyncService.TAG, "Can't execute received SQL: " + e.getMessage());
            sendResult(receiver, 1, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException occurred (reason unknown)");
            sendResult(receiver, 1, e.getMessage());
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Synchronization finished. " + (System.currentTimeMillis() - start) + "ms elapsed.");
        }
    }

    private void sendResult(ResultReceiver receiver, int code, String details) {
        if (receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putString("statusDetail", details);
            receiver.send(code, bundle);
        }
    }

    private InputStream getCompressedResponse(String request, boolean isCompressed) throws IllegalStateException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        //HttpPost httppost = new HttpPost("http://212.66.44.2:3309");
        HttpPost httppost = new HttpPost("http://192.168.1.132:3333");
        HttpEntity entity;
        if (isCompressed) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream gzip = new DeflaterOutputStream(out);
            gzip.write(request.getBytes());
            gzip.close();
            httppost.setHeader("Accept-Encoding", "gzip");
            entity = new ByteArrayEntity(out.toByteArray());
        } else {
            entity = new StringEntity(request);
        }
        httppost.setEntity(entity);
        HttpResponse response = httpclient.execute(httppost);
        return isCompressed ? new InflaterInputStream(response.getEntity().getContent()) : response.getEntity().getContent();
    }
}
