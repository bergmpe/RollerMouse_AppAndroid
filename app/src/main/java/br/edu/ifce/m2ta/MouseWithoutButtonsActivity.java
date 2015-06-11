package br.edu.ifce.m2ta;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.com.blogspot.bergmpe.rollermouse_appandroid.R;
//import br.edu.ifce.m2ta.R;

public class MouseWithoutButtonsActivity extends Activity implements OnTouchListener, OnClickListener{
	//UI variables
	private TextView device;
	private Button btnScan;
		
	//bluetooth variables
	private List<BluetoothDevice> bluetoothDevices;
	private int currentDeviceIndex;
	
	//drag variables
	private float dragPositionStartX;
	private static final int dragMinWidth = 30;
	
	//string to get extra on intent
	public static final String EXTRA_DEVICES_LIST = "devicesList";
	
	// get the time when back button was pressed
	private long exitTimeBackPressed = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mouse_without_buttons);
		
		device = (TextView) findViewById(R.id.deviceName);
		device.setOnTouchListener(this);

		btnScan = (Button) findViewById(R.id.scanDevices);
		btnScan.setOnClickListener(this);
		
		currentDeviceIndex = 0;
		dragPositionStartX = 0;
		
		bluetoothDevices = (List<BluetoothDevice>)getIntent().getSerializableExtra(EXTRA_DEVICES_LIST);
		
		if(bluetoothDevices != null && bluetoothDevices.size() > 0){
			BluetoothDevice btDevice = bluetoothDevices.get(currentDeviceIndex);
			int devCount = bluetoothDevices.size();
			boolean hasSuffix = (devCount > 1);
			
			device.setText(
					(currentDeviceIndex+1) + " / " + devCount + 
					(hasSuffix ? " >>" : "") + 
					"\n\n"+ 
					btDevice.getName() +
					(btDevice.getBondState() == BluetoothDevice.BOND_BONDED ? " (pareado)" : " (n�o pareado)") +
					"\n Toque para conectar");
		}
		else{
			device.setText("Nenhum dispositivo encontrado.");
		}	
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			dragPositionStartX = event.getX();
		}
		else if (event.getAction() == MotionEvent.ACTION_UP){			
			if(event.getX() == dragPositionStartX || Math.abs(event.getX() - dragPositionStartX) < dragMinWidth){// touch
				handleClick(v);
				return true;
			}			
			//check drag to right
			if(event.getX() > dragPositionStartX && (event.getX() - dragPositionStartX) > dragMinWidth){
				currentDeviceIndex--;
			}//check drag to left
			else if(dragPositionStartX - event.getX() > dragMinWidth){
				currentDeviceIndex++;
			}
			
			if(currentDeviceIndex < 0 ){
				currentDeviceIndex = bluetoothDevices.size() - 1;
			}
			
			if(currentDeviceIndex == bluetoothDevices.size()){
				currentDeviceIndex = 0;
			}
			
			BluetoothDevice btDevice = bluetoothDevices.get(currentDeviceIndex);
			int devCount = bluetoothDevices.size();
			boolean hasPreffix = (devCount > 1) && currentDeviceIndex > 0;
			boolean hasSuffix = (devCount > 1) && (currentDeviceIndex < (devCount - 1));
			 
			device.setText(
					(hasPreffix ? "<< " : "") +
					(currentDeviceIndex+1) + " / " + devCount +
					(hasSuffix ? " >>" : "") +
					"\n\n"+ 
					btDevice.getName() +
					(btDevice.getBondState() == BluetoothDevice.BOND_BONDED ? " (pareado)" : " (n�o pareado)") +
					"\n Toque para conectar");
			
		}		
		return true;
	}
	
	private void handleClick(View v){
		if(v == device){			
			ArrayList<BluetoothDevice> uniqueList = new ArrayList<BluetoothDevice>();
			uniqueList.add(bluetoothDevices.get(currentDeviceIndex));
			Intent mouse = new Intent(MouseWithoutButtonsActivity.this, MouseWithButtonsActivity.class);
			mouse.putExtra(MouseWithButtonsActivity.EXTRA_DEVICE, uniqueList);
			startActivityForResult(mouse, RequestCode.APP_FINISH.ordinal());
		}
	}

	@Override
	public void onClick(View v) {
		if(v == btnScan){
			Intent scan = new Intent(MouseWithoutButtonsActivity.this, BluetoothActivity.class);
	    	scan.putExtra(BluetoothActivity.EXTRA_SCAN_DEVICES, true);
	    	startActivity(scan);
	    	finish();
		}
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RequestCode.APP_FINISH.ordinal()){
			if(resultCode == RESULT_OK){
				finish();
			}
		} 
	}
	
	@Override
	public void onBackPressed() {		
		if ((System.currentTimeMillis() - exitTimeBackPressed) > 3000) {
			Toast.makeText(this, "Pressione novamente para sair", Toast.LENGTH_SHORT).show();
			exitTimeBackPressed = System.currentTimeMillis();
			return;
		}
		disableBluetooth();
		finish();
	}
	
	private void disableBluetooth(){
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter!=null && btAdapter.isEnabled()) {
		    btAdapter.disable(); 
		}
	}
}