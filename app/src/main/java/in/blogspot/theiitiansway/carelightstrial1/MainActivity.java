package in.blogspot.theiitiansway.carelightstrial1;


import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    ImageButton btnOn, btnOff;
    LinearLayout viewwhenon;
    Switch switch1,switch2;
    public RelativeLayout viewwhenconnecting,viewwhenerror;
    String mode="NORMAL";
    boolean load=true,finishedSending;
    final int handlerState = 0;
    //used to identify handler message
    public BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket = null;
    private int currentbrightness=100;
    private int lastbrightness;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;



    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finishedSending=true;

        setContentView(R.layout.activity_main);
        //Link the buttons and textViews to respective views
        viewwhenerror=(RelativeLayout)findViewById(R.id.connectionfailure);
        viewwhenconnecting=(RelativeLayout)findViewById(R.id.connectingview);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        viewwhenon=(LinearLayout)findViewById(R.id.screenwhenon);
        viewwhenon=(LinearLayout)findViewById(R.id.screenwhenon);
    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBTState();
        super.onResume();


        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();
        String er="ERROR";
        if (intent.hasExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)){
            address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        }
        else{
            address=s.getString("BA",er);

            if (address==er){
                //Launch Device Picker
                Intent k=new Intent(MainActivity.this,DeviceListActivity.class);
                startActivity(k);
                finish();
                load=false;
            }
        }



        //Get the MAC address from the DeviceListActivty via EXTRA
        switch1=(Switch) findViewById(R.id.switch1);
        switch1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch1.isChecked()){
                    Toast.makeText(MainActivity.this, "Button ON", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("A");
                } else {mConnectedThread.write("B");
                    Toast.makeText(MainActivity.this, "Button OFF", Toast.LENGTH_SHORT).show();}
            }
        });
        switch2=(Switch) findViewById(R.id.switch2);
        switch2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch2.isChecked()){
                    Toast.makeText(MainActivity.this, "Button ON", Toast.LENGTH_SHORT).show();
                    mConnectedThread.write("C");
                } else {mConnectedThread.write("D");
                    Toast.makeText(MainActivity.this, "Button OFF", Toast.LENGTH_SHORT).show();}
            }
        });
        //create device and set the MAC address
        if (load) {
            connectBluetooth b = new connectBluetooth();
            b.execute(1);
        }

    }


    public void showonnnviewfromview(View view){
        showonview();
    }

    public void tryagain(View view){
        connectBluetooth c=new connectBluetooth();
        c.execute(1);
    }
    public void bulblist(View v){
        Intent k=new Intent(MainActivity.this,DeviceListActivity.class);
        startActivity(k);

    }
    public void showerrorview(){
        mode="ERROR";
        viewwhenon.setVisibility(View.INVISIBLE);
        viewwhenconnecting.setVisibility(View.INVISIBLE);
        viewwhenerror.setVisibility(View.VISIBLE);}


    public void showonview(){
        mode="NORMAL";

        viewwhenconnecting.setVisibility(View.INVISIBLE);
        viewwhenerror.setVisibility(View.INVISIBLE);
        viewwhenon.setVisibility(View.VISIBLE);
}
    public void showconnectingview(){
        mode="CONNECTING";

        viewwhenon.setVisibility(View.INVISIBLE);
        viewwhenerror.setVisibility(View.INVISIBLE);
        viewwhenconnecting.setVisibility(View.VISIBLE);}

    class connectBluetooth extends AsyncTask<Number,Number,Number>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showconnectingview();
        }

        @Override
        protected Number doInBackground(Number... view) {
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
            }
            // Establish the Bluetooth socket connection.
            try {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                }
            }
            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();
            //I send a character when resuming.beginning transmission to check device is connected
            //If it is not an exception will be thrown in the write method and finish() will be called

            return view[0];

        }
        @Override
        protected void onPostExecute(Number s) {
            super.onPostExecute(s);
            if(mConnectedThread.write("x")){
                switch (s.intValue()){
                    case 1: showonview();
                        break;
                    case 3: showerrorview();break;
                    default:showonview();}}
            else {showerrorview();}

        }
    }
    @Override
    public void onPause()
    {
        super.onPause();


        if (load){
            try
            {
                //Don't leave Bluetooth sockets open when leaving activity
                btSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();//insert code to deal with this
            }}
    }



    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    public void checkBTState() {

        {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    public class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //write method
        public boolean write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);
                return true;//write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application

                return false;


            }
        }
    }
}