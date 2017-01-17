package com.talkingdata.myna.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class EntryActivity extends AppCompatActivity {

    private Button bt_developer;
    private Button bt_datascientist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        bt_developer = (Button) findViewById(R.id.bt_developer);
        bt_datascientist = (Button) findViewById(R.id.bt_datascientist);
        bindButton();
    }

    private void bindButton(){
        bt_developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeveloperActivity.class);
                startActivity(intent);
            }
        });
        bt_datascientist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DataScientistActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
