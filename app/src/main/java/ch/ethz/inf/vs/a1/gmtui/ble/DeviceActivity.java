package ch.ethz.inf.vs.a1.gmtui.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DeviceActivity extends AppCompatActivity {
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    private BluetoothDevice device;
    private BluetoothGatt humibluetoothGatt;
    private TextView textview;
    private Activity act;
    private BluetoothGattService humidity;
    private long starttime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        graphView = (GraphView) findViewById(R.id.graphview);
        textview = (TextView) findViewById(R.id.connection_status);
        textview.setText(R.string.distconnected);

        act = this;

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
        humibluetoothGatt = device.connectGatt(this, true, humidCallback);
        humibluetoothGatt.connect();
        starttime = System.currentTimeMillis();
    }

    private float convertRawValue(byte[] raw) {
        ByteBuffer wrapper = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getFloat();
    }

    private  BluetoothGattCallback humidCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newstate){
            if(newstate == BluetoothProfile.STATE_CONNECTED){
                gatt.discoverServices();
            }
            else{
                //textview.setText(R.string.distconnected);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            final float a = convertRawValue(characteristic.getValue());
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double currentsec = (System.currentTimeMillis()- starttime)/1000.0;
                    series.appendData(new DataPoint(currentsec, a), false, 100);
                    graphView.addSeries(series);
                }
            });
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status){
            BluetoothGattCharacteristic humidityCharacteristic;

            if(status == BluetoothGatt.GATT_SUCCESS){
                humidity = gatt.getService(SensirionSHT31UUIDS.UUID_HUMIDITY_SERVICE);

                humidityCharacteristic = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC);

                BluetoothGattDescriptor descriptor = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC).getDescriptors().get(1);
                descriptor.setValue(new byte[]{1});

                gatt.writeDescriptor(descriptor);

                BluetoothGattCharacteristic newChar = new BluetoothGattCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC,
                        humidityCharacteristic.getProperties(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                humidity.addCharacteristic(newChar);
                gatt.setCharacteristicNotification(newChar, true);
            }
        }

    };

}
