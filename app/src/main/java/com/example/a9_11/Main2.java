package com.example.a9_11;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main2 extends AppCompatActivity {

    Button validation ,enroll,Face,Rec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        validation= findViewById(R.id.Validation);
        enroll= findViewById(R.id.enrollment);
        Face = findViewById(R.id.AllCards);
        Rec =findViewById(R.id.rec);

        Rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2.this,face_capture.class );
                startActivity(intent);
            }
        });

        Face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2.this,fingerRecognation.class );
                startActivity(intent);
            }
        });
        validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main2.this,face_recognation.class );
                startActivity(intent);
            }
        });
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main2.this,fingerscan.class );
                startActivity(intent);
            }
        });
    }
}