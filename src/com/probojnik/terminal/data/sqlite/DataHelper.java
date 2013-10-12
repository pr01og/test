package com.probojnik.terminal.data.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import com.probojnik.terminal.data.sqlite.mediators.Request;
import com.probojnik.terminal.util.Const;
import com.probojnik.terminal.util.UserPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * @author Stanislav Shamji
 */
public class DataHelper {
    private static DataHelper instance;
    private final SQLiteOpenHelper helper;
    private final String terminalID;

    private DataHelper(Context context) {
        helper = new OpenHelper(context);
        UserPreferences preferences = new UserPreferences(context);
        terminalID = preferences.getStringPreference(Const.PREF_TERMINAL_ID);
    }

    public static DataHelper getInstance(Context context) {
        return instance == null ? instance = new DataHelper(context) : instance;
    }

    public SQLiteDatabase getDB(boolean isWritable) {
        if (isWritable) {
            return helper.getWritableDatabase();
        } else {
            return helper.getReadableDatabase();
        }
    }

    public List<Map<String, String>> processRequest(Request request) {
        List<Map<String, String>> response = new ArrayList<Map<String, String>>();
        switch (request.getRequestType()) {
            case TerminalServices:
                response.addAll(processTerminalServicesRequest());
                break;
            case GroupsList:
                response.addAll(processGroupsListRequest(request));
                break;
            case ServiceList:
                response.addAll(processServiceListRequest(request));
                break;
            case GetParams:
                response.addAll(processGetParamsRequest(request));
                break;
        }
        return response;
    }

    private List<Map<String, String>> processTerminalServicesRequest() {
        List<Map<String, String>> response = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("terminalservices INNER JOIN services ON terminalservices.serviceid = services.serviceid");
        Cursor cursor = null;
        try {
            cursor = builder.query(
                    db,
                    new String[]{"terminalid", "name", "services.serviceid"},
                    "terminalid = " + getTerminalSID(db), null, null, null, null);
            if (cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("servicename", cursor.getString(1));
                    map.put("serviceid", cursor.getString(2));
                    response.add(map);
                }
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        } finally {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return response;
    }

    private List<Map<String, String>> processGroupsListRequest(Request request) {
        List<Map<String, String>> response = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (!request.getParent().equals("")) {
            builder.setTables("terminalservices INNER JOIN services ON terminalservices.serviceid = services.serviceid " +
                    "INNER JOIN groups ON services.serviceid = groups.departmentid");
        } else {
            builder.setTables("groupsterminal INNER JOIN groups ON groups.groupid = groupsterminal.groupid");
        }
        Cursor cursor = null;
        try {
            if (!request.getParent().equals("")) {
                cursor = builder.query(
                        db,
                        new String[]{"terminalid", "parent", "isNext", "groupname", "groupid"},
                        "terminalid=" + terminalID + " AND departmentid=" + request.getDepartment() +
                                " AND parent=" + request.getParent(),
                        null, null, null, null);
            } else {
                cursor = builder.query(
                        db,
                        new String[]{"terminalname", "parent", "isNext", "groupname", "groups.groupid"},
                        "terminalname='" + terminalID + "'",
                        null, null, null, null);
            }
            if (cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("parent", cursor.getString(1));
                    map.put("isNext", cursor.getString(2));
                    map.put("groupname", cursor.getString(3));
                    map.put("groupid", cursor.getString(4));
                    response.add(map);
                }
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        } finally {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return response;
    }

    private List<Map<String, String>> processServiceListRequest(Request request) {
        List<Map<String, String>> response = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("terminalservices INNER JOIN services ON terminalservices.serviceid = services.serviceid " +
                "INNER JOIN groups ON services.serviceid = groups.departmentid " +
                "INNER JOIN operstate ON groups.groupid = operstate.groupid");
        Cursor cursor = null;
        try {
            cursor = builder.query(
                    db,
                    new String[]{"terminalid", "opername", "guids", "operstateid"},
                    "terminalid=" + getTerminalSID(db) + " AND groups.departmentid=" + request.getDepartment() + " AND operstate.groupid=" + request.getGroupID(),
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("opername", cursor.getString(1));
                    map.put("guids", cursor.getString(2));
                    map.put("operstateid", cursor.getString(3));
                    response.add(map);
                }
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        } finally {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
        }
        return response;
    }


    private List<Map<String, String>> processGetParamsRequest(Request request) {
        List<Map<String, String>> response = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("operstate INNER JOIN operlinks ON operstate.operstateid = operlinks.operstateid " +
                "INNER JOIN docstate ON docstate.docstateid = operlinks.docstateid");
        Cursor cursor = null;
        Cursor listCursor = null;
        try {
            cursor = builder.query(
                    db,
                    new String[]{"docstate.docstateid", "docname"},
                    "operstate.operstateid=" + request.getOvirServiceID() + " AND docstate.deleted=0 AND operlinks.deleted=0",
                    null, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String docStateID = cursor.getString(0);
                String docName = cursor.getString(1);
                String[] tmp = docName.split("<");
                String where = "";
                List<String> params = new LinkedList<String>();
                for (String param : tmp) {
                    param = param.split(">")[0];
                    if (param.length() > 0) {
                        params.add(param);
                    }
                }
                for (int i = 0; i < params.size(); i++) {
                    where += "paramname='" + params.get(i) + "'";
                    if (i < params.size() - 1) {
                        where += " OR ";
                    }
                }
                cursor.close();
                builder.setTables("plat_params");
                cursor = builder.query(
                        db,
                        new String[]{"id", "paramname", "paramtype", "paramtext"},
                        where,
                        null, null, null, null);
                if (cursor.getCount() > 0) {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("id", cursor.getString(0));
                        map.put("paramname", cursor.getString(1));
                        map.put("paramtype", cursor.getString(2));
                        map.put("paramtext", cursor.getString(3));
                        if (cursor.getString(2).equals("l")) {
                            JSONArray array = new JSONArray();
                            builder.setTables("param_list");
                            listCursor = builder.query(
                                    db,
                                    new String[]{"id", "paramid", "name", "paramtext"},
                                    "paramid=" + cursor.getString(0),
                                    null, null, null, null);
                            if (listCursor.getCount() > 0) {
                                for (listCursor.moveToFirst(); !listCursor.isAfterLast(); listCursor.moveToNext()) {
                                    JSONObject obj = new JSONObject();
                                    obj.put("id", listCursor.getString(0));
                                    obj.put("paramid", listCursor.getString(1));
                                    obj.put("name", listCursor.getString(2));
                                    obj.put("paramtext", listCursor.getString(3));
                                    array.put(map);
                                }
                            }
                            map.put("values", array.toString());
                        }
                        response.add(map);
                    }
                }
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            db.close();
            if (cursor != null) {
                cursor.close();
            }
            if (listCursor != null) {
                listCursor.close();
            }
        }
        return response;
    }


    public String getTerminalSID() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String res = getTerminalSID(db);
        db.close();
        return res;
    }

    private String getTerminalSID(SQLiteDatabase db) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables("terminals");
        Cursor cursor = builder.query(db, new String[]{"terminalsid"}, "terminal_id=?", new String[]{terminalID}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        return "-1";
    }
}
