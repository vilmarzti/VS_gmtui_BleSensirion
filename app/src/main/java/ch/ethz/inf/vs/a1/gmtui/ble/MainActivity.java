package ch.ethz.inf.vs.a1.gmtui.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Button btn;
    private TextView textview;
    private ListView listView;
    private List<BluetoothDevice> devices;
    private Handler handler = new Handler();

    private static final SensirionSHT31UUIDS sensirion =  new SensirionSHT31UUIDS();
    private static final long SCAN_PERIOD = 10000;
    private static final int ACCESS_FINE   = 1;
    private static final int ACCESS_COARSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);;
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        btn = (Button) findViewById(R.id.scan_devices);
        btn.setOnClickListener(this);
        btn.setText(R.string.bnt_enabled);

        textview = (TextView) findViewById(R.id.textview);
        listView = (ListView) findViewById(R.id.listview);

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }else{
            btn.setEnabled(true);
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                             int[] grantResults) {
        textview.setText(permissions.toString());
        switch (requestCode) {
            case ACCESS_FINE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    textview.setText(textview.getText() + " " + "FINE");
                }
                else{
                    textview.setText(":(");
                }
                return;
            }
            case ACCESS_COARSE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    textview.setText(textview.getText() + " " + "COARSE");
                }
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            if (resultCode == RESULT_OK){
                btn.setEnabled(true);
            }
        }
    }


    @Override
    public void onClick(View view){
        btn.setEnabled(false);
        btn.setText(R.string.btn_scanning);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btn.setEnabled(true);
                btn.setText(R.string.bnt_enabled);
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }, SCAN_PERIOD);

        devices.clear();
        bluetoothLeScanner.startScan(scanCallback);
   }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            devices.add(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            textview.setText("found something");
        }
    };
}
