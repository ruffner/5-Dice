package com.bluntllama.fivekind;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HighScoreDataSource {
    private static final String TAG = "5 Dice";
    private static String DATE_FORMAT = "hh:mm MM/dd/yy";

    // Database fields
    private SQLiteDatabase database;
    private HighScoreDatabaseHelper dbHelper;

    public static final String[] PROJECTION = {
            ScoreTable._ID,
            ScoreTable.COLUMN_NAME_GAME_DATE,
            ScoreTable.COLUMN_NAME_PLAYER_NAME,
            ScoreTable.COLUMN_NAME_TALLY_GAME,
    };

    public static final String[] DATE_PROJECTION = {
        ScoreTable.COLUMN_NAME_GAME_DATE,
    };

    public HighScoreDataSource(Context context) {
        dbHelper = new HighScoreDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertScore(ContentValues values) {
        // Gets the current system time in milliseconds
        Long now = Long.valueOf(System.currentTimeMillis());

        // If the values map doesn't contain the creation date, sets the value to the current time.
        if (values.containsKey(ScoreTable.COLUMN_NAME_GAME_DATE) == false) {
            values.put(ScoreTable.COLUMN_NAME_GAME_DATE, now);
        }

        // If the values map doesn't contain note text, sets the value to an empty string.
        if (values.containsKey(ScoreTable.COLUMN_NAME_TALLY_GAME) == false) {
            throw new IllegalArgumentException("Must have a game score to insert");
        }

        if (values.containsKey(ScoreTable.COLUMN_NAME_PLAYER_NAME) == false) {
            throw new IllegalArgumentException("Must have a player name to insert");
        }

        // Performs the insert and returns the ID of the new note.
        long rowId = database.insert(
                ScoreTable.TABLE_NAME,        // The table to insert into.
                null,  // A hack, SQLite sets this column value to null if values is empty.
                values                           // A map of column names, and the values to insert
                // into the columns.
        );

        return rowId;
    }

    public void deleteScore(String playerName, int playerScore) {
        database.delete(
                ScoreTable.TABLE_NAME,
                ScoreTable.COLUMN_NAME_TALLY_GAME + " = " + playerScore + "; " + ScoreTable.COLUMN_NAME_PLAYER_NAME + " = " + playerName,
                null
        );
    }

    public Cursor getHighScores() {
        Cursor cursor = database.query(
                ScoreTable.TABLE_NAME,
                PROJECTION,
                null,
                null,
                null,
                null,
                ScoreTable.DEFAULT_SORT_ORDER
        );

        return cursor;
    }

    public String getDateOfRecord(String playerName, int playerScore) {
        Cursor cursor = database.query(
                ScoreTable.TABLE_NAME,
                DATE_PROJECTION,
                ScoreTable.COLUMN_NAME_TALLY_GAME + " = " + playerScore + "; " + ScoreTable.COLUMN_NAME_PLAYER_NAME + " = " + playerName,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        String ret = cursor.getString(0);
        cursor.close();

        return longToDate(Long.parseLong(ret));
    }

    public String getLongDateOfRecord(String playerName, int playerScore) {
        Cursor cursor = database.query(
                ScoreTable.TABLE_NAME,
                DATE_PROJECTION,
                ScoreTable.COLUMN_NAME_TALLY_GAME + " = " + playerScore + "; " + ScoreTable.COLUMN_NAME_PLAYER_NAME + " = " + playerName,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        String ret = cursor.getString(0);
        cursor.close();

        return ret;
    }

    public long getDateOfRecord(long id) {
        Cursor cursor = database.query(ScoreTable.TABLE_NAME,
                DATE_PROJECTION,
                ScoreTable._ID + " = " + id,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        long date = cursor.getLong(0);
        cursor.close();
        return date;
    }

    public static String longToDate(long millis) {
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return formatter.format(calendar.getTime());
    }
} 