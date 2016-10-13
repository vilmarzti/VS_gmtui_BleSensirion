package ch.ethz.inf.vs.a1.gmtui.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, ListView.OnItemClickListener{
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Button btn;
    private ListView listView;
    private List<BluetoothDevice> devices;
    private ArrayAdapter<BluetoothDevice> adapter;
    private Handler handler = new Handler();

    private static final long SCAN_PERIOD  = 10000;
    private static final int ACCESS_FINE   = 1;
    private static final int ACCESS_COARSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Turn on Bluetooth
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }else{
            btn.setEnabled(true);
        }

        // Ask for required Permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE);

        // Get everthing Bluetooth
        //bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);;
        //bluetoothAdapter = bluetoothManager.getAdapter();
        //bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Get Button
        btn = (Button) findViewById(R.id.scan_devices);
        btn.setOnClickListener(this);
        btn.setText(R.string.bnt_enabled);
        btn.setEnabled(false);

        // Listview and devices
        devices = new ArrayList<BluetoothDevice>();
        adapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, devices);

        listView = (ListView) findViewById(R.id.device_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                             int[] grantResults) {
        // Check if i got the permissions I wanted

        switch (requestCode) {
            case ACCESS_FINE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     // Get everthing Bluetooth
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);;
                    bluetoothAdapter = bluetoothManager.getAdapter();
                    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                    btn.setEnabled(true);
                }
                return;


            case ACCESS_COARSE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // enable scan if bluetooth is turned on
        if(requestCode == 1){
            if (resultCode == RESULT_OK){
                btn.setEnabled(true);
            }
        }
    }


    @Override
    public void onClick(View view){
        // disable scan
        btn.setEnabled(false);
        btn.setText(R.string.btn_scanning);
        adapter.clear();

        // stop scan after SCAN_PERIOD
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.setEnabled(true);
                btn.setText(R.string.bnt_enabled);
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }, SCAN_PERIOD);

        bluetoothLeScanner.startScan(scanCallback);
   }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            BluetoothDevice device;

            switch(callbackType){
                case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                    device = result.getDevice();
                    if (device != null && adapter.getPosition(device) == -1 &&
                            result.getScanRecord().getDeviceName() != null &&
                            result.getScanRecord().getDeviceName().equals("Smart Humigadget")){

                        adapter.add(device);
                    }
                    return;

                case ScanSettings.CALLBACK_TYPE_MATCH_LOST :
                    device = result.getDevice();
                    if (device != null) {
                        adapter.remove(device);
                    }
                    return;
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothLeScanner.stopScan(scanCallback);
        btn.setEnabled(true);
        btn.setText(R.string.bnt_enabled);

        BluetoothDevice device = devices.get(position);
        Intent intent = new Intent(this, DeviceActivity.class);
        intent.putExtra("device",(Parcelable) device);
        startActivity(intent);
    }
}
