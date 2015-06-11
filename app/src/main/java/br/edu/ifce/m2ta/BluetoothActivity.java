package br.edu.ifce.m2ta;

        import java.util.ArrayList;
        import java.util.Set;

//import br.edu.ifce.m2ta.R;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothClass;
        import android.bluetooth.BluetoothDevice;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.res.Configuration;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Window;
        import android.widget.TextView;
        import android.widget.Toast;

        import br.com.blogspot.bergmpe.rollermouse_appandroid.R;

public class BluetoothActivity extends Activity{
    //UI variable
    TextView txtView;
    //string to get extra on intent
    public static final String EXTRA_SCAN_DEVICES = "scanDevices";
    //tag to android log
    private static final String TAG = "BluetoothActivity";

    //bluetooth variables
    private ArrayList<BluetoothDevice> btDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private Intent btEnableRequest;

    //if true, searches and shows devices in range
    //if false, does not search and shows only paired devides (even if paired device is not in range)
    private boolean isScanRequest;
    //if true, should not request bluetooth again
    private boolean isWaitingForScanRequest;

    // get the time when back button was pressed
    private long exitTimeBackPressed = 0;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                if (getBluetoothDeviceType(device.getBluetoothClass().getMajorDeviceClass()).equalsIgnoreCase("COMPUTER")){
                    btDevices.add(device);
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setProgressBarIndeterminateVisibility(false);
                showDevices();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"on Create");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth);

        txtView = (TextView) findViewById(R.id.txtView1);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        btDevices = new ArrayList<BluetoothDevice>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG,"Bluetooth not available");
            Intent error = new Intent(BluetoothActivity.this, ErrorActivity.class);
            error.putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, getString(R.string.bluetoothNotAvailable));
            startActivity(error);
        }

        isScanRequest = getIntent().getBooleanExtra(EXTRA_SCAN_DEVICES, false);
        isWaitingForScanRequest = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"on Start");
        if(!isWaitingForScanRequest) {
            isWaitingForScanRequest = true;
            if(checkBluetoothAvailable()){
                txtView.setText(getString(R.string.searchingDevices));
                if(isScanRequest){
                    startScan();
                }
                else {
                    getPairedDevices();
                    showDevices();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"on Pause");
        if(isWaitingForScanRequest){
            isWaitingForScanRequest = false;
            setResult(100,btEnableRequest);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"on Destroy");
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "Configuration changed: "+newConfig.toString());
    }

    private void showBluetoothErrorScreen(){
        Intent error = new Intent(BluetoothActivity.this, ErrorActivity.class);
        error.putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE,getString(R.string.bluetoothNotEnabled));
        startActivity(error);
        finish();
    }

    // Starting the device discovery
    private void startScan(){
        setProgressBarIndeterminateVisibility(true);
        if(!mBluetoothAdapter.startDiscovery()){
            txtView.setText("Erro ao iniciar a busca.");
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private boolean checkBluetoothAvailable(){
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            Toast.makeText(getApplicationContext(), "Bluetooth habilitado", Toast.LENGTH_SHORT).show();
            isWaitingForScanRequest = false;
            txtView.setText(getString(R.string.searchingDevices));
            // Starting the device discovery
            if(isScanRequest){
                startScan();
            }
            else{
                getPairedDevices();
                showDevices();
            }
        }
        return true;
    }

    private void getPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                btDevices.add(device);
            }
        }
    }

    private void showDevices(){
        // inicia a tela do mouse em bot�es
        Intent intent = new Intent(BluetoothActivity.this, MouseWithoutButtonsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MouseWithoutButtonsActivity.EXTRA_DEVICES_LIST, btDevices);
        startActivity(intent);
        finish();
    }

    private String getBluetoothDeviceType(int major){
        switch(major){
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";
            default: return "unknown!";
        }
    }

    /**
     * onBackPressed
     *     exit lancher when user touches on screen two times in a 2s interval
     * @see android.app.Activity#onBackPressed()
     * @author nta.ifce
     * @version 1.0
     */
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTimeBackPressed) > 3000) {
            Toast.makeText(this, "Pressione novamente para cancelar a busca", Toast.LENGTH_SHORT).show();
            exitTimeBackPressed = System.currentTimeMillis();
            return;
        }
        finish();
    }
}