package com.smarttechx.rfduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;



public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    // State machine
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;
    //private BluetoothGatt mBluetoothGatt;
    SeekBar customSeekbar;
    private TextView progress;
    private int state;
    private boolean scanStarted;
    private boolean scanning;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private RFduinoService rfduinoService;
    private TextView enableBluetooth;
    private TextView scanStatusText;
    private Button scanButton;
    private TextView deviceInfoText;
    private TextView connectionStatusText;
    private Button connectButton;
    private Button disconnectButton;
    private Button sendFourButton, closeApp;
    private Button sendTwelveButton;
    private Button sendTwentyButton;
    private TextView valOne;
    int count=0;
    String valget, val1,val2;



    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }
        }
    };

    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanning = (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            scanStarted &= scanning;
            updateUi();
        }
    };

    private ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(bluetoothDevice.getAddress())) {

                    upgradeState(STATE_CONNECTING);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }
    };

    public BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("REEEECCCCEIVING");
            final String action = intent.getAction();

            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
                bluetoothAdapter.stopLeScan(MainActivity.this);
              // addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
                /*String hex = HexAsciiHelper.bytesToHex(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
                System.out.println("MY DATA");
                System.out.println(hex);*/

                // MY NEW DATA
                byte[] bytes = intent.getByteArrayExtra(RFduinoService.EXTRA_DATA);

                int asInt = (bytes[0] & 0xFF)
                        | ((bytes[1] & 0xFF) << 8)
                        | ((bytes[2] & 0xFF) << 16)
                        | ((bytes[3] & 0xFF) << 24);

                float asFloat = Float.intBitsToFloat(asInt);
                //System.out.println("MY DATA");
                //System.out.println(asFloat);

                float copyFloat = asFloat;

                BigDecimal bd = new BigDecimal(copyFloat);
                bd = bd.round(new MathContext(3));
                double rounded = bd.doubleValue();

                String floatStr = Double.toString(rounded);
                String status = "2";
                String urlServer = "";

                if (rounded > 1.5){
                    status = "0";
                    urlServer = "http://openseat.mirrim-game.com/insertStatus.php?id=123456789&pressureValue=" + floatStr + "&status=" + status;
                }
                else{
                    urlServer = "http://openseat.mirrim-game.com/insertStatus.php?id=123456789&pressureValue=" + floatStr + "&status=" + status;
                }
                //System.out.println(urlServer);
                new RequestTask().execute(urlServer);

                //System.out.println(floatStr);
                addDatastr(floatStr);

                System.out.println(floatStr);

                rfduinoService = null;
                unregisterReceiver(this);
                System.out.println("KKKKKK");
                System.out.println(RFduinoService.UUID_SERVICE);
                System.out.println(bluetoothDevice.getName());

                if ( RFduinoService.UUID_SERVICE.equals(BluetoothHelper.sixteenBitUuid(0x3000))) {
                    System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{");
                    RFduinoService.UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x2220);
                    RFduinoService.UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x2221);
                    RFduinoService.UUID_SEND = BluetoothHelper.sixteenBitUuid(0x2222);
                    RFduinoService.UUID_DISCONNECT = BluetoothHelper.sixteenBitUuid(0x2223);
                    RFduinoService.UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);
                }
                else if ( RFduinoService.UUID_SERVICE.equals(BluetoothHelper.sixteenBitUuid(0x2220))) {
                    System.out.println("|||||||||||||||||||||||||||||||||||||||||");
                    RFduinoService.UUID_SERVICE = BluetoothHelper.sixteenBitUuid(0x3000);
                    RFduinoService.UUID_RECEIVE = BluetoothHelper.sixteenBitUuid(0x3001);
                    RFduinoService.UUID_SEND = BluetoothHelper.sixteenBitUuid(0x3002);
                    RFduinoService.UUID_DISCONNECT = BluetoothHelper.sixteenBitUuid(0x3003);
                    RFduinoService.UUID_CLIENT_CONFIGURATION = BluetoothHelper.sixteenBitUuid(0x2902);
                }

                System.out.println(RFduinoService.UUID_SERVICE);

                System.out.println("STOPPING SCAN");
                //nregisterReceiver(scanModeReceiver);
                //unregisterReceiver(bluetoothStateReceiver);
                //unregisterReceiver(rfduinoReceiver);
                //rfduinoService.disconnect();
                //bluetoothAdapter.disable();

                scanStarted = false;
                disconnectButton.setEnabled(false);
                //rfduinoService.disconnect();
                //bluetoothAdapter.disable();
                registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

                bluetoothAdapter.startLeScan(
                        new UUID[]{RFduinoService.UUID_SERVICE},
                        MainActivity.this);

                System.out.println("NEXT");

                //downgradeState(STATE_DISCONNECTED);
                /*if (count<2 && hex!="41"){
                    count++;
                    String ascii=hexToAscii(hex);
                    valget=valget+ascii;
                    //addDatastr(valget);
                }
                else{
                    addDatastr(valget);
                    count=0;
                    valget="";
                }*/

            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Bluetooth
        // Find Device
        scanStatusText = (TextView) findViewById(R.id.scanStatus);
        deviceInfoText = (TextView) findViewById(R.id.deviceInfo);
        customSeekbar = (SeekBar) findViewById(R.id.seekBar1);
        enableBluetooth = (TextView) findViewById(R.id.enableBluetoothButton);

        // GAUTHIER CODE
        bluetoothAdapter.enable();
        scanStarted = true;
        System.out.println("Starting Scan Device 1.");
        //System.out.println(BluetoothHelper.sixteenBitUuid(0x4000));
        System.out.println(RFduinoService.UUID_SERVICE);

        bluetoothAdapter.startLeScan(
                new UUID[]{RFduinoService.UUID_SERVICE},
                MainActivity.this);

        System.out.println(":(");


        System.out.println(":)");
        //////////////////

        // Connect Device
        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);
        scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.enable();
                scanStarted = true;
                bluetoothAdapter.startLeScan(
                        new UUID[]{RFduinoService.UUID_SERVICE},
                        MainActivity.this);

            }
        });


        // Device Info

        disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanStarted = false;
                disconnectButton.setEnabled(false);
                if(rfduinoService==null){
                    bluetoothAdapter.disable();

                }
                else{
                   // rfduinoService.close();
                    rfduinoService.disconnect();
                    bluetoothAdapter.disable();
                }

            }
        });

        connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);
                scanStarted = false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        System.out.println("IIIIII");
        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

        updateState(bluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
    }

    @Override
    protected void onStop() {
        System.out.println("STOPPING SCAN");
        super.onStop();
        bluetoothAdapter.stopLeScan(this);
        unregisterReceiver(scanModeReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        unregisterReceiver(rfduinoReceiver);
    }


    private void upgradeState(int newState) {
        if (newState > state) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < state) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        state = newState;
        updateUi();
    }

    private void updateUi() {
        // Enable Bluetooth
        boolean on = state > STATE_BLUETOOTH_OFF;
        scanButton.setEnabled(on);
        if (bluetoothAdapter.isEnabled() == true) {
            enableBluetooth.setText("Bluetooth ON");
            disconnectButton.setEnabled(true);
            scanButton.setEnabled(false);
        } else {
            enableBluetooth.setText("Bluetooth OFF");
        }
        // Scan
        if (scanStarted && scanning) {
            scanStatusText.setText("Scanning...");
            scanButton.setText("Stop Scan");
            scanButton.setEnabled(true);

        } else if (scanStarted) {
            scanStatusText.setText("Scan started...");
            scanButton.setEnabled(false);
        } else {
            scanStatusText.setText("");
            scanButton.setText("Scan");
            scanButton.setEnabled(true);
        }


        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            //
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);

        // Send


    }

    private void addData(byte[] data) {
       // String ascii = HexAsciiHelper.bytesToAsciiMaybe(data);
        valOne = (TextView) findViewById(R.id.valueone);
        String hex = HexAsciiHelper.bytesToHex(data);
        String ascii=hexToAscii(hex);
        valOne.setText(ascii+"."+"00mA");
    }
    private void addDatastr(String data) {
        // String ascii = HexAsciiHelper.bytesToAsciiMaybe(data);
        valOne = (TextView) findViewById(R.id.valueone);
        valOne.setText(data + "V");
        /*if(data.length()==4){
            val1=data.substring(0,2);
            val2=data.substring(2,4);
            valOne.setText(val1+"."+val2+"mA");
        }
        else
        {
            val1=data.substring(0,1);
            val2=data.substring(1);
            valOne.setText(val1+"."+val2+"mA");
        }*/

    }


    private  static   String hexToAscii(String hexString){
        /*System.out.println("Converting String");
        long hello = Long.parseLong(hexString, 16);
        System.out.println(hello);

        int val = Integer.parseInt(hexString,16);
        System.out.println(val);
        return String.valueOf(val);*/
        return "HELLO";
    }

    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        System.out.println("Stopping scan");
        bluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfoText.setText(
                        BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord));
                updateUi();
            }
        });



        System.out.println("===========");
        System.out.println(bluetoothDevice.getName());
        connectionStatusText.setText("Connecting...");
        System.out.println("Connecting...");
        Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
        System.out.println("-----------");
        bindService(rfduinoIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                rfduinoService = ((RFduinoService.LocalBinder) service).getService();
                if (rfduinoService.initialize()) {
                    if (rfduinoService.connect(bluetoothDevice.getAddress())) {

                        upgradeState(STATE_CONNECTING);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                rfduinoService = null;
                downgradeState(STATE_DISCONNECTED);
            }
        }, BIND_AUTO_CREATE);
    }
}

