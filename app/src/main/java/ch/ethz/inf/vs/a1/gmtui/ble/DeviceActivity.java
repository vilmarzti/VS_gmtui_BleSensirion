package ch.ethz.inf.vs.a1.gmtui.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DeviceActivity extends AppCompatActivity {
    private GraphContainer graphContainer;
    private int x=0;
    private BluetoothDevice device;
    private BluetoothGatt humibluetoothGatt;
    private BluetoothGatt tempbluetoothGatt;
    private TextView textview;
    private BluetoothGattService humidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        textview = (TextView) findViewById(R.id.connection_status);
        textview.setText(R.string.distconnected);

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");
        tempbluetoothGatt = device.connectGatt(this, false, humidCallback);
        tempbluetoothGatt.connect();

        GraphView graphView = new GraphView(this);
        graphView.setTag("myGraphView");

        ViewGroup layout = (ViewGroup)  findViewById(R.id.activity_device);
        graphContainer = new GraphContainerImpl(graphView, 2, 40);

        layout.addView(graphView);
    }

    private float convertRawValue(byte[] raw) {
        ByteBuffer wrapper = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        return wrapper.getFloat();
    }

    private final BluetoothGattCallback humidCallback = new BluetoothGattCallback() {

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
            float  a = convertRawValue(characteristic.getValue());
            graphContainer.addValues(x, new float[]{a});
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status){

            if(status == BluetoothGatt.GATT_SUCCESS){
                humidity = gatt.getService(SensirionSHT31UUIDS.UUID_HUMIDITY_SERVICE);

                BluetoothGattCharacteristic humidityCharacteristic = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC).getDescriptors().get(1);
                descriptor.setValue(new byte[]{1});

                gatt.writeDescriptor(descriptor);

                BluetoothGattCharacteristic test = new BluetoothGattCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC,
                        humidityCharacteristic.getProperties(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                humidity.addCharacteristic(test);
                gatt.setCharacteristicNotification(test, true);
            }
        }
    };

    private final BluetoothGattCallback temptCallback = new BluetoothGattCallback() {

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
            float  a = convertRawValue(characteristic.getValue());

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status){

            if(status == BluetoothGatt.GATT_SUCCESS){
                humidity = gatt.getService(SensirionSHT31UUIDS.UUID_TEMPERATURE_SERVICE);

                BluetoothGattCharacteristic humidityCharacteristic = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC);
                BluetoothGattDescriptor descriptor = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC).getDescriptors().get(1);
                descriptor.setValue(new byte[]{1});

                gatt.writeDescriptor(descriptor);

                BluetoothGattCharacteristic test = new BluetoothGattCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC,
                        humidityCharacteristic.getProperties(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                humidity.addCharacteristic(test);
                gatt.setCharacteristicNotification(test, true);
            }
        }
    };

}
