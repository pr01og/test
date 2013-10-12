package com.probojnik.terminal.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.probojnik.terminal.util.Const;

/**
 * @author Stanislav Shamji
 */
class OpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "probojnik.terminal.db";

    public OpenHelper(Context context) {
        super(context, DB_NAME, null, Const.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Tables table : Tables.values()) {
            db.execSQL(table.getValue().generateCreateScript());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Tables table : Tables.values()) {
            db.execSQL(table.getValue().generateDropScript());
        }
        onCreate(db);
    }
}
