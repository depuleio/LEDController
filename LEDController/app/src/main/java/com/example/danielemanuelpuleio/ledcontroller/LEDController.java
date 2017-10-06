package com.example.danielemanuelpuleio.ledcontroller;


import android.app.PendingIntent;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.util.Log;
import android.view.*;
import com.felhr.usbserial.*;
import java.io.*;

import java.util.HashMap;
import java.util.Map;

public class LEDController extends AppCompatActivity {

    private HashMap<String, UsbDevice> _deviceHashMap;
    private Button _findButton;
    private TextView _status;
    private UsbDeviceConnection _connection;
    private UsbDevice _device;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbSerialDevice serialPort;
    private Button[] _colorButtons;
    private TextView _output;

    //onCreate initializes our app, inherited from AppCompactActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledcontroller);
        _deviceHashMap = new HashMap<String, UsbDevice>();
        _findButton = (Button) findViewById(R.id.findButton);
        _status = (TextView) findViewById(R.id.status);
        _output = (TextView) findViewById(R.id.output);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        BroadCastHandler broadCastHandler = new BroadCastHandler(this);
        Thread thread = new Thread(broadCastHandler);
        thread.start();
        initializeButtons();

    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                tvAppend(_output, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver _usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    _connection = usbManager.openDevice(_device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(_device, _connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true); //Enable Buttons in UI
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback); //
                            tvAppend(_output, "Serial Connection Opened!\n");
                            String text = "Device Connected";
                            _status.setText(text);
                            _status.setTextColor(Color.GREEN);

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(_findButton);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStart(_findButton);
            }
        }
    };

    public void setUiEnabled(Boolean set)
    {
        for (Button button: _colorButtons) {
            button.setEnabled(set);
        }
    }

    public void initializeButtons()
    {
        Button red = (Button) findViewById(R.id.red);
        Button orange = (Button) findViewById(R.id.orange);
        Button yellow = (Button) findViewById(R.id.yellow);
        Button yellow_green = (Button) findViewById(R.id.yellow_green);
        Button green = (Button) findViewById(R.id.green);
        Button blue_green = (Button) findViewById(R.id.blue_green);
        Button light_blue = (Button) findViewById(R.id.light_blue);
        Button blue = (Button) findViewById(R.id.blue);
        Button indigo = (Button) findViewById(R.id.indigo);
        Button purple = (Button) findViewById(R.id.purple);
        Button pink = (Button) findViewById(R.id.pink);
        Button white = (Button) findViewById(R.id.white);
        _colorButtons = new Button[]{red,orange,yellow,yellow_green,green,blue_green,
                light_blue,blue,indigo,purple,pink,white};
        for(final Button button: _colorButtons){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorDrawable colorDrawable = (ColorDrawable) button.getBackground();
                    int intColor = colorDrawable.getColor();
                    Color color = new Color();
                    byte red = (byte) color.red(intColor);
                    byte green = (byte) color.green(intColor);
                    byte blue = (byte) color.blue(intColor);
                    byte[] byte_array = {red, green, blue};
                    serialPort.write(byte_array);
                }
            });
        }
    }

    public void onClickStart(View v){
        _deviceHashMap = usbManager.getDeviceList();
        String text;
        if(_deviceHashMap.isEmpty())
        {
            System.out.println("NO DEVICES FOUND, YOU ARE FUCKED");
            text = "Device Not Found!";
            _status.setText(text);
            _status.setTextColor(Color.RED);
            setUiEnabled(false);
        }
        else
        {
            for (Map.Entry entry : _deviceHashMap.entrySet()) {
                UsbDevice device = (UsbDevice) entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                            new Intent(ACTION_USB_PERMISSION), 0);
                    _device = device;
                    usbManager.requestPermission(device, pi);
                    break;
                }
                else {
                    _connection = null;
                    device = null;
                }
            }
        }
    }

    public class BroadCastHandler implements Runnable
    {
        private LEDController _ledController;
        public BroadCastHandler(LEDController ledController){
            _ledController = ledController;
        }

        @Override
        public void run() {
            _findButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickStart(v);
                }
            });
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(_ledController, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(_usbReceiver, filter);

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //check if the device is already closed
        if (_connection != null) {
            try {
                _connection.close();
            } catch (Exception e) {
                //we couldn't close the device, but there's nothing we can do about it!
            }
            //remove the reference to the device
            _connection = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

        @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
