package com.yura.buttleship;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RecordsActivity extends AppCompatActivity {
    TableLayout tableLayout;
    DBHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        dbHelper = new DBHelper(this);
        db = dbHelper.getReadableDatabase();
        tableLayout = (TableLayout) findViewById(R.id.table);
        fillTable();
    }

    private void fillTable() {
        TableRow tr_head = new TableRow(this);
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        addRow(tr_head, "Имя", "Рекорд");
        Cursor c = db.query("records", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                TableRow tr = new TableRow(this);
                tr.setBackgroundColor(Color.LTGRAY);
                addRow(tr,
                        c.getString(c.getColumnIndex("username")),
                        c.getDouble(c.getColumnIndex("time")) + ""
                );
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        dbHelper.close();

    }

    private void addRow(TableRow tr, String fcCol, String secCol) {
        TextView cell1 = new TextView(this);
        cell1.setText(fcCol);
        cell1.setTextColor(Color.WHITE);
        cell1.setPadding(5, 5, 5, 5);
        tr.addView(cell1);

        TextView cell2 = new TextView(this);
        cell2.setText(secCol);
        cell2.setTextColor(Color.WHITE);
        cell2.setPadding(5, 5, 5, 5);
        tr.addView(cell2);
        tableLayout.addView(tr, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }
}
