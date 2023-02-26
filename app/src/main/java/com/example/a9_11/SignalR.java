package com.example.a9_11;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

public class SignalR extends AppCompatActivity {
    HubConnection hubConnection;
    EditText receiveTxt;
    Button button;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_r);
        button = findViewById(R.id.recievebtn);
        receiveTxt = findViewById(R.id.recieveTxt);
        try {
            hubConnection = HubConnectionBuilder.create("http://192.168.1.28:4040/lpr").build();
            hubConnection.start().blockingAwait();
            hubConnection.send("joinGroup","Effat");
            // ...
        } catch (Exception ex) {
            ex.printStackTrace();
        } // Start the hub connection

        // n
        hubConnection.start()
                .doOnError(throwable -> {
                    Log.e(TAG, "doInBackground > doOnError: ", throwable);
//start fail , try again
//note: the start function need try chach when we use this function
                })
                .doOnComplete(() -> {
                    Log.i(TAG, "doInBackground > doOnComplete.");
                    //start complated
                    hubConnection.on("ReceiveProcessData",  (data) -> {
                        System.out.println("Received message: " + data.getItemName());
                        receiveTxt.setText(data.getItemName());
                    },  ProcessHub.class);
                })
                .blockingAwait();//you must write this function ,else other function not worck
        //  String methodName = String.valueOf(hubConnection.getConnectionState());;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hubConnection.start()
                        .doOnError(throwable -> {
                            Log.e(TAG, "doInBackground > doOnError: ", throwable);
//start fail , try again
//note: the start function need try chach when we use this function
                        })
                        .doOnComplete(() -> {
                            Log.i(TAG, "doInBackground > doOnComplete.");
                            //start complated
                            hubConnection.on("ReceiveProcessData",  (data) -> {
                                System.out.println("Received message: " + data.getItemName());
                                receiveTxt.setText(data.getItemName());
                            },  ProcessHub.class);
                        })
                        .blockingAwait();
            }
        });
    }

}