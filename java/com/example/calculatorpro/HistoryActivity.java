package com.example.calculatorpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();

        if (intent.hasExtra("historyData")) {
            // Obtain the transmitted historical data
            ArrayList<String> historyList = getIntent().getStringArrayListExtra("historyData");

            // Find a view in the layout to display historical data, such as TextView or RecyclerView
            TextView historyTextView = findViewById(R.id.HAView);

            // Set historical data to TextView
            if (historyList != null) {
                StringBuilder historyText = new StringBuilder();
                for (String historyItem : historyList) {
                    historyText.append(historyItem).append("\n");
                }
                historyTextView.setText(historyText.toString());

            }
        }


    }


}
