package com.example.bt_sendreceive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;


    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Button btn1,btn2,listenBtn;
    TextView incomindData;
    Thread btListenerThread;
    boolean thread_wait=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.send1Btn);
        btn2 = findViewById(R.id.send2Btn);
        listenBtn = findViewById(R.id.listenBtn);
        incomindData = findViewById(R.id.textView);
        Intent intent = getIntent();
        address = intent.getStringExtra(ConnectActivity.EXTRA_ADDRESS);
        new MainActivity.ConnectBT().execute();
        btListenerThread = new Thread(new MyThread());
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread_wait = true;
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread_wait = false;
            }
        });
    }

    public class MyThread implements Runnable{
        public MyThread(){

        }
        @Override
        public void run() {
            while (true){
                try {
                    if(thread_wait == true) {
                        mmInputStream = btSocket.getInputStream();
                        mmInputStream.skip(mmInputStream.available());

                        char b = (char) mmInputStream.read();
                        Log.d("DATA : ", Character.toString(b));
                        SystemClock.sleep(100);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Baglanıyor...","Bekleyin");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if(btSocket==null || isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if(!ConnectSuccess){
                finish();
            }else {
                isBtConnected = true;
                btListenerThread.start();//if connection is success then start listener thread
            }
            progress.dismiss();
        }
    }
}