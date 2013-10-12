package com.probojnik.terminal.network;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.probojnik.terminal.R;
import com.probojnik.terminal.data.sqlite.DataHelper;
import com.probojnik.terminal.data.sqlite.mediators.Request;
import com.probojnik.terminal.view.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Stanislav Shamji
 */
public class SendTask extends AsyncTask<Void, String, List<Map<String, String>>> {

    private Request request;
    private MainActivity mainActivity;

    ProgressBar progressBar1;

    public SendTask(MainActivity mainActivity, Request request) {
        super();
        this.mainActivity = mainActivity;
        this.request = request;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar1 = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);
        progressBar1.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... arg0) {
        List<Map<String, String>> responseArrayList;

        if (request != null) {
            responseArrayList = DataHelper.getInstance(mainActivity).processRequest(request);
        } else {
            System.err.println("request == null");
            responseArrayList = new ArrayList<Map<String, String>>();
        }

        System.out.println("RequestTask | doInBackground | responseArrayList = " + responseArrayList);

        return responseArrayList;
    }

    protected void onPostExecute(List<Map<String, String>> responseArrayList) {

        Toast.makeText(mainActivity, "buttons quantity = " + mainActivity.fillLayout(mainActivity, responseArrayList), Toast.LENGTH_SHORT).show();

        progressBar1.setVisibility(View.INVISIBLE);
    }

}