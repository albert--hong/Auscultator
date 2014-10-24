package com.auscultator.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.auscultator.data.SAX_AudioTagService;

import java.io.InputStream;


public class Menu extends Activity {
    private final String TAG = "MENU";
    /**
     * The states for the view
     */
    private final short ST_MENU = 0;          // The Menu.
    private final short ST_AUSCULT = 1;
    private final short ST_BREATH = 2;
    private final short ST_HEART = 3;
    private final short ST_RECORD = 4;

    private short status = ST_MENU;

    private ImageView auscultation;
    private ImageView breathSounds;
    private ImageView heartSounds;
    private ImageView mediaRecords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // get the instances of the components
        auscultation = (ImageView) findViewById(R.id.btn_auscultation);
        breathSounds = (ImageView) findViewById(R.id.btn_breath_sounds);
        heartSounds = (ImageView) findViewById(R.id.btn_heart_sounds);
        mediaRecords = (ImageView) findViewById(R.id.btn_medical_records);

        initializeApplication();

        // add listeners to components
        auscultation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status != ST_MENU) {
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(Menu.this, Auscultation.class);
                Menu.this.startActivity(intent);
            }
        });
        breathSounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status != ST_MENU) {
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(Menu.this, BreathSounds.class);
                Menu.this.startActivity(intent);
            }
        });
        heartSounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status != ST_MENU) {
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(Menu.this, HeartSounds.class);
                Menu.this.startActivity(intent);
            }
        });
        mediaRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status != ST_MENU) {
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(Menu.this, MedicalRecords.class);
                Menu.this.startActivity(intent);
            }
        });
    }

    private void initializeApplication() {
        // Get the heart sounds' list from library
        int resId = getResources().getIdentifier("audiotags", "raw", getPackageName());
        if (resId == 0) {
            Toast.makeText(getApplicationContext(), "软件被损坏", Toast.LENGTH_SHORT).show();
        }
        InputStream ins = getResources().openRawResource(resId);
        SAX_AudioTagService.initialize(ins);
    }
}
