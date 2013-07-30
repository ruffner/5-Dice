package com.bluntllama.fivekind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HighScoreDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "scores.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE " + ScoreTable.TABLE_NAME + " ("
            + ScoreTable._ID + " INTEGER PRIMARY KEY,"
            + ScoreTable.COLUMN_NAME_GAME_DATE + " INTEGER,"
            + ScoreTable.COLUMN_NAME_PLAYER_NAME + " TEXT,"
            + ScoreTable.COLUMN_NAME_TALLY_GAME + " INTEGER"
            + ");";

    public HighScoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(HighScoreDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + ScoreTable.TABLE_NAME);
        onCreate(db);
    }

} 