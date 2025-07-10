package com.example.todolist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    public Database(Context context) {
        super(context, "Database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    private void createDayTable(String date) {
        String createTable = "CREATE TABLE IF NOT EXISTS `"+date+"` ( `ID` INTEGER PRIMARY KEY AUTOINCREMENT," +
                " `Task` TEXT, `Description` TEXT, `Priority` TEXT, `DueDate` TEXT, `Status` TEXT);";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(createTable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertTask(String date, Task task) {
        createDayTable(date);
        String insert = "INSERT INTO `"+date+"` (`Task`, `Description`, `Priority`, `DueDate`, `Status`) " +
                "VALUES ( '"+task.getTitle()+"', '"+task.getDescription()+"', '"+task.isPriority()+"', '"+
                task.getDueDateToString()+"', '"+task.getStatus()+"' );";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(insert);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Task> getTasks(String date) {
        createDayTable(date);
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Task> tasks = new ArrayList<>();
        String select = "SELECT * FROM `"+date+"`;";
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()) {
            do {
                Task t = new Task();
                t.setID(cursor.getInt(0));
                t.setTitle(cursor.getString(1));
                t.setDescription(cursor.getString(2));
                t.setPriority(Boolean.parseBoolean(cursor.getString(3)));
                t.setDueDate(cursor.getString(4));
                t.setStatus(cursor.getString(5));
                tasks.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tasks;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Task readTask(String date, int ID) {
        createDayTable(date);
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM `"+date+"` WHERE `ID` = "+ID+" ;";
        Cursor cursor = db.rawQuery(select, null);
        Task t = new Task();
        if (cursor.moveToFirst()) {
            t.setID(cursor.getInt(0));
            t.setTitle(cursor.getString(1));
            t.setDescription(cursor.getString(2));
            t.setPriority(Boolean.parseBoolean(cursor.getString(3)));
            t.setDueDate(cursor.getString(4));
            t.setStatus(cursor.getString(5));
        }
        cursor.close();
        return t;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateTask(String date, Task t) {
        createDayTable(date);
        String update = "UPDATE `"+date+"` SET `Task` = '"+t.getTitle()+"', `Description` = '"+
                t.getDescription()+"', `Priority` = '"+t.isPriority()+"', `DueDate` = '"+t.getDueDateToString()
                +"', `Status` = '"+t.getStatus()+"' WHERE `ID` = "+t.getID()+" ;";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(update);
    }

    public void deleteTask(String date, int ID) {
        createDayTable(date);
        String delete = "DELETE FROM `"+date+"` WHERE `ID` = "+ID+" ;";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(delete);
    }

}
