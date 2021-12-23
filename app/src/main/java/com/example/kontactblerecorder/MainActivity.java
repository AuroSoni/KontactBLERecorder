package com.example.kontactblerecorder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kontactblerecorder.databinding.ActivityMainBinding;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'kontactblerecorder' library on application startup.
    static {
        System.loadLibrary("kontactblerecorder");
    }

    private static final String API_KEY = "zWsIXUklUXaQZfxVnQsraHqWllLvPETs";//grp0 API key
    private ProximityManager proximityManager;
    TextView beacons_in_range_tv;
    TextView update_cycles_tv;
    Button button;
    boolean scan_state;

    File file;
    FileWriter writer;

    int beacons_in_range=0;
    long update_cycles=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.kontactblerecorder.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KontaktSDK.initialize(API_KEY);

        beacons_in_range_tv = findViewById(R.id.beacons_in_range_tv);
        update_cycles_tv = findViewById(R.id.update_cycles_tv);
        button = findViewById(R.id.button);
        scan_state=false;

        button.setOnClickListener(view -> {
            if(scan_state){
                stopScanning();
                button.setText(R.string.start);
            }
            else{
                startScanning();

            }
        });

        File root = new File("/storage/self/primary/Download","BLEData");
        if(!root.exists()) root.mkdir();

        file = new File(root,"DistanceData.txt");

        try {
            writer = new FileWriter(file);
            writer.append("CREATED\n");
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.RANGING)
                .deviceUpdateCallbackInterval(300);
        proximityManager.setIBeaconListener(createIBeaconListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        initCount();

        writeln("");
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private void startScanning() {
        initCount();
        proximityManager.connect(() -> proximityManager.startScanning());
        writeln("START");
        scan_state = !scan_state;
        button.setText(R.string.stop);
    }

    private void stopScanning(){
        proximityManager.stopScanning();
        if(scan_state)writeln("STOP");
        scan_state = !scan_state;
        button.setText(R.string.start);
    }

    private void initCount(){
        beacons_in_range = 0;
        update_cycles = 0;
        updateCount();
    }

    private void updateCount(){
        beacons_in_range_tv.setText(String.valueOf(beacons_in_range));
        update_cycles_tv.setText(String.valueOf(update_cycles));
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Listener", "IBeacon discovered: " + ibeacon.toString());
                beacons_in_range++;
                updateCount();
                //add_beacon(ibeacon.getName(),ibeacon.getUniqueId(),ibeacon.getRssi());
                writeln("ADD_"+ibeacon.getName()+"_"+ibeacon.getProximityUUID()+"_"+ibeacon.getUniqueId()+"_"+ibeacon.getRssi()+"_"+ibeacon
                .getDistance());
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
                //Log.i("Listener", "IBeacon updated: " + ibeacons.toString());
                update_cycles++;
                updateCount();
                writeln("UPDATE_CYCLE_BEGIN");
                for(IBeaconDevice ibeacon:ibeacons){
                    writeln("UPDATE_"+ibeacon.getName()+"_"+ibeacon.getProximityUUID()+"_"+ibeacon.getUniqueId()+"_"+ibeacon.getRssi()+"_"+
                            ibeacon.getDistance());
                }
                writeln("UPDATE_CYCLE_END");
            }

            @Override
            public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Listener", "IBeacon lost: " + ibeacon.toString());
                beacons_in_range--;
                updateCount();
                //remove_beacon(ibeacon.getName(),ibeacon.getUniqueId());
                writeln("REMOVE_"+ibeacon.getName()+"_"+ibeacon.getProximityUUID()+"_"+ibeacon.getUniqueId());
            }
        };
    }


    private void write(String text){
        try {
            writer.append(text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeln(String text){
        write(text+"\n");
    }

    public native void initialize();
    public native void start_scan();
    public native void stop_scan();
    public native void add_beacon(String id, int rssi);
    public native void remove_beacon(String id);
    public native void update_beacons(IBeaconDevice[] ibeacons);
}