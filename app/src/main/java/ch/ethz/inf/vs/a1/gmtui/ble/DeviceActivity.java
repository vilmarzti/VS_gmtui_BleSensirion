package ch.ethz.inf.vs.a1.gmtui.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceActivity extends AppCompatActivity {
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private TextView textview;
    private TextView device_name;
    private SensirionSHT31UUIDS uuids;
    private BluetoothGattService humidity;
    private BluetoothGattService temperatur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        textview = (TextView) findViewById(R.id.connection_status);
        textview.setText(R.string.distconnected);
        device_name = (TextView) findViewById(R.id.device_name);


        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
        bluetoothGatt = device.connectGatt(this, false, gatCallback);;
        bluetoothGatt.connect();
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
        public void onServicesDiscovered(BluetoothGatt gatt,int status){
            textview.setText("discovered something");
            if(status == BluetoothGatt.GATT_SUCCESS){
                humidity = gatt.getService(SensirionSHT31UUIDS.UUID_HUMIDITY_SERVICE);
                temperatur = gatt.getService(SensirionSHT31UUIDS.UUID_TEMPERATURE_SERVICE);

                textview.setText("success");
            }

        }
    };

}
