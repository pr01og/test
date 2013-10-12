package com.probojnik.terminal.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import com.probojnik.terminal.data.sqlite.mediators.Request;
import com.probojnik.terminal.data.sqlite.mediators.RequestType;
import com.probojnik.terminal.network.ParseXml;
import com.probojnik.terminal.network.RequestTask;
import com.probojnik.terminal.util.ButtonAdapter;
import com.probojnik.terminal.util.ButtonEntry;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @author Stanislav Shamji
 */
public abstract class FillLayoutActivity extends Activity implements CallbackListener {
    MainActivity mainActivity;

    LinearLayout linearLayout;
    ListView listView;
    Request request;
    private RequestTask requestTask;

    ArrayList<ButtonEntry> mainArrayListButtonEntry;
    ButtonAdapter mainButtonAdapter;
    private JSONObject jsonObject;
    //private HashMap<RequestParam, String> requestHashMap;

    public SharedPreferences sharedPreferences;
    public final String PREFS_NAME = "TerminalLogin";
    public final String LOGIN = "login";
    public final String SESSION = "session";

    public final String rightLogin = "log";
    public final String rightPassword = "pass";
    public final int timeOut = 1000*5; // 5 seconds

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag().equals("send")) {
                Toast.makeText(mainActivity, "button Click = " + v.getTag(), Toast.LENGTH_SHORT).show();
            } else
                System.out.println("FillLayoutActivity | onClickListener | v.getTag() = " + v.getTag());
        }
    };

    void showLinearLayout(boolean show){
        if(show){
            linearLayout.removeAllViews();
            linearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }else{
            listView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);

        }
    }

    public short fillLayout(MainActivity mainActivity, List<Map<String, String>> responseArrayList) {
        short i = 0;

        System.out.println("request.getRequestType() = " + request.getRequestType());
        if (request.getRequestType().equals(RequestType.GetParams)) {
            showLinearLayout(true);
            for (Map<String, String> responseHashMap : responseArrayList) { // iterate over responseArrayList
                System.out.println("responseHashMap.toString() = " + responseHashMap.toString());
                // responseHashMap.get("paramtype").equals("s") || responseHashMap.get("paramtype").equals("i") || responseHashMap.get("paramtype").equals("f")
                if(responseHashMap.get("paramtype").equals("l")){  // l - список из param_list

                }else if(responseHashMap.get("paramtype").equals("s")){ // s - строка

                    linearLayout.addView( addTextView( responseHashMap.get("paramtext") ) );
                    linearLayout.addView(addEditText( responseHashMap.get("paramname"), InputType.TYPE_CLASS_TEXT ));

                }else if(responseHashMap.get("paramtype").equals("i")){   // i - целое число
                    linearLayout.addView( addTextView( responseHashMap.get("paramtext") ) );
                    linearLayout.addView(addEditText( responseHashMap.get("paramname"), InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED ));

                }else if(responseHashMap.get("paramtype").equals("f")){  // f - дробь
                    linearLayout.addView( addTextView( responseHashMap.get("paramtext") ) );
                    linearLayout.addView(addEditText( responseHashMap.get("paramname"), InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL ));

                }else if(responseHashMap.get("paramtype").equals("null")){

                }else
                    System.err.println("FillLayoutActivity | fillLayout | responseHashMap.get('paramtype') = " + responseHashMap.get("paramtype"));
            }

            linearLayout.addView(addButton("Отправить", "send"));
        }else{
            showLinearLayout(false);
            i = addButtons(mainActivity, responseArrayList);
        }

        return i;
    }

    private View addEditText(String tag, int inputType) {
        EditText editText;
        editText = new EditText(mainActivity);
        editText.setTag(tag);
        editText.setInputType(inputType);
        editText.setRawInputType(inputType);


        return editText;
    }

    private View addTextView(String text) {
        TextView textView;
        textView = new TextView(mainActivity);
        textView.setText(text);

        return textView;
    }

    private View addButton(String text, String tag) {
        Button button;
        button = new Button(mainActivity);
        button.setText(text);
        button.setOnClickListener(onClickListener);
        button.setTag(tag);

        return button;
    }


    public short addButtons(MainActivity mainActivity, List<Map<String, String>> responseArrayList) {
        short i = 0; // -32768 до 32767

        mainArrayListButtonEntry.clear();
        if (!ParseXml.customXMLparser(request.getRequestType().toString(), 0).equals("null")) {
            for (Map<String, String> responseHashMap : responseArrayList) {
                String name = (String) responseHashMap.get(ParseXml.customXMLparser(request.getRequestType().toString(), 2) + "name");
                String tag = hashMap2JSONObject(responseHashMap);
//                System.out.println("addButtons | name = " + name + ", tag = " + tag);
                mainArrayListButtonEntry.add(new ButtonEntry(name, tag));
                i++;
            }
        }
        mainButtonAdapter.notifyDataSetChanged();


        return i;
    }



    private String hashMap2JSONObject(Map<String, String> requestHashMap) {
        JSONObject jsonObject = new JSONObject(requestHashMap);
        return jsonObject.toString();
    }

    /*                      | i=0           | i=1           | i=2
     * s = terminalservices | <service      | serviceid     | servicename   |
     * s = groupslist       | <row1         | groupid       | groupname     |
     * s = servicelist      | <row          | operstateid   | opername      |
     * s = getparams        | <row          |               |               |
     * */
    public static String customXMLparser(String s, int i) { // i=0
        String result = "null";

        //System.out.println("customXMLparser s = " + s + ", i = " + i);

        String JSONstring = "{'" + RequestType.TerminalServices + "':['service','service','service']," +
                "'" + RequestType.GroupsList + "':['row1','group','group']," +
                "'" + RequestType.ServiceList + "':['row','operstate','oper']," +
                "'" + RequestType.GetParams + "':['row','','']}";
        try {
            JSONObject jsonObject = new JSONObject(JSONstring);

            result = jsonObject.getJSONArray(s).getString(i);    // String
        } catch (JSONException e) {
            System.err.println("JSONException e = " + e.getCause());
        }
        return result;
    }

    public void task(Context context, String req) {
        if (requestTask == null || requestTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            returnRequest(req);
            requestTask = new RequestTask(mainActivity, this.request); // 1
            requestTask.execute();
        }
    }

    String department="", groupid="";
    public Request returnRequest(String req) {
        request = null;
        System.out.println("req = " + req);
        if (req.equals("terminalservices")) {                                               // 0
            request = Request.createTerminalServicesRequest();
        } else {    // 1, 2, 3, 4
            try {
                jsonObject = new JSONObject(req);
                if (req.contains("serviceid")) {                                            // 1
                    department = jsonObject.getString("serviceid");
                    String parent = "";
                    request =  Request.createGroupsListRequest(department, parent);
                }
                if (req.contains("groupid") && jsonObject.toString().contains("isNext")) {  // 2&3
                    groupid = jsonObject.getString("groupid");
                    String isnext = jsonObject.getString("isNext");
                    if (isnext.equals("1")) {                                               // 2
                        request =  Request.createGroupsListRequest(department,groupid);
                    } else {                                                                // 3
                        String groupid = jsonObject.getString("groupid");
                        request =  Request.createServiceListRequest(department, groupid);
                    }
                }
                if (req.contains("operstateid")) {                                           // 4  inputs$select
                    String ovirServiceID = jsonObject.getString("operstateid");
                    request =  Request.createGetParamsRequest(ovirServiceID);
                }
                if (req.contains("getparams")) {                                             // 5   toServer
                    String getparams = jsonObject.getString("getparams");
                    System.out.println("getparams = " + getparams);
                }
            } catch (JSONException e) {
                System.err.println("MainActivity | returnRequest | e = " + e);
            }
        }
        System.out.println(", request = " + request);
        return request;
    }

    Boolean getSession(){ //
        Boolean sessionOut = true;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Long secondTime = calendar.getTimeInMillis();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Long firstTime = sharedPreferences.getLong(SESSION, 0);

        if(secondTime - firstTime - timeOut > 0)  // secondTime != 0 &&  // предположим что onPause() срабатывает всегда
            finish();
        else
            sessionOut = false;

        System.out.println("getSession | st-ft = " + (secondTime - firstTime) + ", st-ft-to = " + (secondTime - firstTime - timeOut) + ", sessionOut = " + sessionOut);

        return sessionOut;
    }
    void setSession(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        System.out.println("setSession | calendar.getTimeInMillis() = " + calendar.getTimeInMillis());
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SESSION, calendar.getTimeInMillis());
        editor.commit();
    }



}
