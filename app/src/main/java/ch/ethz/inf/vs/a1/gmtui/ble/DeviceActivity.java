package ch.ethz.inf.vs.a1.gmtui.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceActivity extends AppCompatActivity {
    BluetoothDevice device;
    BluetoothGatt bluetoothGatt;
    TextView textview;
    private List<BluetoothDevice> services;
    private ArrayAdapter<BluetoothDevice> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        textview = (TextView) findViewById(R.id.connection_status);

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
        bluetoothGatt = device.connectGatt(this, false, gatCallback);;
    }

    private final BluetoothGattCallback gatCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newstate){
            if(newstate == BluetoothProfile.STATE_CONNECTED){
                textview.setText(R.string.connected);
                gatt.discoverServices();
            }
            else{
                textview.setText(R.string.distconnected);
            }
        }

        @Override
        public void onServicessDiscovered(BluetoothGatt gatt,int status){
            if(status == BluetoothGatt.GATT_SUCCESS){

            }

        }
    };

}
