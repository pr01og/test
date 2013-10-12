package com.probojnik.terminal.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.probojnik.terminal.R;
import com.probojnik.terminal.data.synchronization.SyncService;

/**
 * @author Stanislav Shamji
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Intent intent = new Intent(this, SyncService.class);
        intent.putExtra("receiver", receiver);
        startService(intent);
    }

    private ResultReceiver receiver = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 0) {
                startActivity(new Intent(SplashActivity.this, Login.class));
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle(R.string.sync_failed_title);
                String statusDetail = resultData.getString("statusDetail");
                builder.setMessage(getString(R.string.sync_failed_msg) + statusDetail);
                builder.create().show();
            }
        }
    };
}
