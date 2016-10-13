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
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DeviceActivity extends AppCompatActivity {
    private double minX = 0;
    private double maxX = 50;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> temperaturseries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> humidityseries = new LineGraphSeries<>();
    private BluetoothDevice device;
    private BluetoothGatt humibluetoothGatt;
    private TextView textview;
    private Activity act;
    private BluetoothGattService humidity;
    private BluetoothGattService temperatur;
    private long starttime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        graphView = (GraphView) findViewById(R.id.graphview);
        textview = (TextView) findViewById(R.id.connection_status);
        textview.setText(R.string.distconnected);

        humidityseries.setColor(Color.RED);
        temperaturseries.setColor(Color.BLUE);

        Viewport viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMaxX(maxX);
        viewport.setMinX(minX);
        viewport.setMaxY(100);
        viewport.setMinY(-10);

        act = this;

        Intent intent = getIntent();
        device = intent.getParcelableExtra("device");

        starttime = 0;
        humibluetoothGatt = device.connectGatt(this, true, humidCallback);
        humibluetoothGatt.connect();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        humibluetoothGatt.disconnect();
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

                if(starttime == 0){
                    starttime = System.currentTimeMillis();
                }

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(R.string.connected);
                    }
                });
            }
            else{
                 act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(R.string.distconnected);
                    }
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic){

            final float a = convertRawValue(characteristic.getValue());
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double currentsec = (System.currentTimeMillis()- starttime)/1000.0;
                    if(characteristic.getUuid().equals(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC)){
                        humidityseries.appendData(new DataPoint(currentsec, a), false, 1000);
                        graphView.addSeries(humidityseries);

                        if(humidityseries.getHighestValueX() > maxX){
                            maxX = humidityseries.getHighestValueX();
                            minX = maxX - 50;
                            graphView.getViewport().setMaxX(maxX);
                            graphView.getViewport().setMinX(minX);
                        }
                    }
                    else{
                        temperaturseries.appendData(new DataPoint(currentsec, a), false, 1000);
                        graphView.addSeries(temperaturseries);

                       if(temperaturseries.getHighestValueX() > maxX){
                            maxX = temperaturseries.getHighestValueX();
                            minX = maxX - 50;
                            graphView.getViewport().setMaxX(maxX);
                            graphView.getViewport().setMinX(minX);
                        }

                    }
                }
            });
        }

        @Override
        public void onDescriptorWrite (BluetoothGatt gatt, final BluetoothGattDescriptor descriptor,
                                    int status){
            if(descriptor.getUuid().equals(SensirionSHT31UUIDS.NOTIFICATION_DESCRIPTOR_UUID) && temperatur == null) {

                temperatur = gatt.getService(SensirionSHT31UUIDS.UUID_TEMPERATURE_SERVICE);

                BluetoothGattCharacteristic characteristic = temperatur.getCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC);

                BluetoothGattDescriptor writedescriptor = temperatur.getCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC).getDescriptors().get(1);
                writedescriptor.setValue(new byte[]{1});
                gatt.writeDescriptor(writedescriptor);

                BluetoothGattCharacteristic newChar = new BluetoothGattCharacteristic(SensirionSHT31UUIDS.UUID_TEMPERATURE_CHARACTERISTIC,
                        characteristic.getProperties(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                temperatur.addCharacteristic(newChar);

                gatt.setCharacteristicNotification(newChar, true);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status){
            BluetoothGattCharacteristic characteristic;

            if(status == BluetoothGatt.GATT_SUCCESS && humidity == null){
                humidity = gatt.getService(SensirionSHT31UUIDS.UUID_HUMIDITY_SERVICE);

                characteristic = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC);

                BluetoothGattDescriptor descriptor = humidity.getCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC).getDescriptors().get(1);
                descriptor.setValue(new byte[]{1});
                gatt.writeDescriptor(descriptor);

                BluetoothGattCharacteristic newChar = new BluetoothGattCharacteristic(SensirionSHT31UUIDS.UUID_HUMIDITY_CHARACTERISTIC,
                        characteristic.getProperties(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

                humidity.addCharacteristic(newChar);

                gatt.setCharacteristicNotification(newChar, true);
            }
        }

    };


}
