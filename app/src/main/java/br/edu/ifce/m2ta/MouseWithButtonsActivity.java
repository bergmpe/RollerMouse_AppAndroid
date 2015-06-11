package br.edu.ifce.m2ta;

import java.util.List;

import com.mouseacessivel.library.MouseCommand;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import br.com.blogspot.bergmpe.rollermouse_appandroid.R;
//import br.edu.ifce.m2ta.R;

public class MouseWithButtonsActivity extends Activity implements SensorEventListener, OnClickListener, OnTouchListener,
														GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
	//UI variables
	private Button halfClick, doubleClick, click, rightClick;
	private ImageButton scrollUp, scrollDown, scrollRight, scrollLeft;
	private ViewSwitcher switcher;
	private TextView no_buttons;
	
	//string to get extra on intent
	public static final String EXTRA_DEVICE = "device_to_connect";
	
	//tag to android log
	private static final String TAG = "MouseActivity";

	// Intent request options
	//private static final int REQUEST_ENABLE_BT = 17;
	
	//shake variables
	private SensorManager sensorMgr;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 800;
	 
	//detects gestures on no_iu screen
	private GestureDetector detector;
	private Thread doubleClickConfirm;
	 
    //bluetooth variables
	private BluetoothDevice btDevice;
    private BluetoothService commandService = null;
	private BluetoothAdapter btAdapter;
	private Thread disableBluetoothThread;
	
	private boolean isBackButtonPressed;
	private boolean isPausedByBTEnable;
	//private boolean ignoreDisableBluetoothThread;
	private boolean isHalfClickEnabled;
	private boolean mustConnectInsecure = false;
	 
	 //handles responses from commandService
	 private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case BluetoothService.MESSAGE_STATE_CHANGE:
	                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                switch (msg.arg1) {
	                case BluetoothService.STATE_CONNECTED:
	                    enableButtons();
	                    no_buttons.setEnabled(true);
	                    no_buttons.setTextColor(Color.BLACK);
	                    break;
	                    
	                case BluetoothService.STATE_CONNECTING:
	                    disableButtons();
	                    no_buttons.setEnabled(false);
	                    no_buttons.setTextColor(Color.GRAY);
	                    break;
	                    
	                case BluetoothService.STATE_LISTEN:
	                    disableButtons();
	                    no_buttons.setEnabled(false);
	                    no_buttons.setTextColor(Color.GRAY);
	                    break;
	                    
	                case BluetoothService.STATE_NONE:
	                    disableButtons();
	                    no_buttons.setEnabled(false);
	                    no_buttons.setTextColor(Color.GRAY);
	                    if(isBackButtonPressed){
	                    	setProgressBarIndeterminateVisibility(false);
	                    	setResult(RESULT_CANCELED);
	                    	finish();
	                    }
	                    break;
	                }
	                break;
	            /*case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String(writeBuf);
	                mConversationArrayAdapter.add("Me:  " + writeMessage);
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
	                break;*/
	            case BluetoothService.MESSAGE_DEVICE_NAME:
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + msg.getData().getString(BluetoothService.DEVICE_NAME), Toast.LENGTH_SHORT).show();
	                mustConnectInsecure = false;
	                break;
	            case BluetoothService.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            case BluetoothService.MESSAGE_CONNECTION_FAILED:
	            	if(mustConnectInsecure){
	            		mustConnectInsecure = false;
	            		connectCommandServiceInsecure();
	            	} else {
		                Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST),
		                               Toast.LENGTH_SHORT).show();
	            	}
	                break;
	            
	            }
	        }
	    };	    
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_mouse_with_buttons);
		
		// start motion detection
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		boolean accelSupported = sensorMgr.registerListener(this,
				sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
			    SensorManager.SENSOR_DELAY_UI);
	 
		if (!accelSupported) {
		    // no accelerometer on this device
		    sensorMgr.unregisterListener(this);
		}
		
	    List<BluetoothDevice> list = (List<BluetoothDevice>) getIntent().getSerializableExtra(EXTRA_DEVICE);
	    if(list == null || list.size() == 0){
	    	Intent error = new Intent(MouseWithButtonsActivity.this, ErrorActivity.class);
	    	error.putExtra(ErrorActivity.EXTRA_ERROR_MESSAGE, getString(R.string.noDevices));
	    	startActivity(error);
	//    	ignoreDisableBluetoothThread = true;
	    	setResult(RESULT_OK);
	    	finish();
	    }
    
	    btDevice = list.get(0);
	    btAdapter = BluetoothAdapter.getDefaultAdapter();
	    
	    detector = new GestureDetector(MouseWithButtonsActivity.this, this);
	    detector.setOnDoubleTapListener(this);
	    
	    setupButtonsUI();
	    setupNoButtonsUI();
	    
	    switcher = (ViewSwitcher) findViewById(R.id.mouseSwitcher);
	    
	    adaptToScreenSize();
	    
	    isBackButtonPressed = false;
	    isPausedByBTEnable = false;
	//    ignoreDisableBluetoothThread = false;
	    isHalfClickEnabled = false;
	}
	
    private void adaptToScreenSize() {
    	DisplayMetrics metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    
	    float scaleFactor = metrics.density;
	    float widthDp = metrics.widthPixels / scaleFactor;
		float heightDp = metrics.heightPixels / scaleFactor;
		
		float smallestWidth = Math.min(widthDp, heightDp);
		if (smallestWidth < 600) {
		    //device has small screen, change to no_buttons UI
			switcher.showNext();
		}		
	}

	@Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "++ ON START ++");
    }
    
    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.i(TAG, "+ ON RESUME +");
        
        stopBluetoothThread();
    	
    	if(!isPausedByBTEnable){
        // If BT is not on, request that it be enabled.
	        if (!btAdapter.isEnabled()) {
	        	isPausedByBTEnable = true;
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, RequestCode.ENABLE_BLUETOOTH.ordinal());
	        } 
	        else{
	        	connectCommandService();
	        }
    	}
    }
    
    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.i(TAG, "- ON PAUSE -");
        
    	if(!isPausedByBTEnable && !isBackButtonPressed){// && !ignoreDisableBluetoothThread){
            stopBluetoothThread();
	    	//start thread to disable bluetooth after 1 minute
	    	disableBluetoothThread = new Thread(new Runnable() {				
				@Override
				public void run() {
					try {
						//Log.e(TAG, "start bluetooth thread");
						Thread.sleep(60000);
						disableBluetooth();						
					} 
					catch (InterruptedException e) {
						// do nothing. 
					//	e.printStackTrace();
					}
				}
			});
	    	disableBluetoothThread.start();
    	}
    	
    //	if(!isPausedByBTEnable){
	        disconnectCommandService();
    //	}
    	
    }

    @Override
    public void onStop() {
        Log.i(TAG, "-- ON STOP --");
        disconnectCommandService();
        super.onStop();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(sensorMgr != null){
			sensorMgr.unregisterListener(this);
		}
		Log.e(TAG, "stop thread on Destroy");
		stopBluetoothThread();
		disconnectCommandService();
		
		if(!isBackButtonPressed){
			disableBluetooth();
		}
	}
	
	private void disableBluetooth(){
    	if (btAdapter!=null && btAdapter.isEnabled()) {
    		Log.e(TAG, "disable bluetooth");
		    btAdapter.disable(); 
		}
    }
	
	private void stopBluetoothThread(){
		//stop thread that would disable bluetooth
    	if(disableBluetoothThread != null && disableBluetoothThread.isAlive()){
    		Log.e(TAG, "stop bluetooth thread");
    		disableBluetoothThread.interrupt();
    	}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mouse, menu);
		return true;
	}	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
		//	ignoreDisableBluetoothThread = true;
			setResult(RESULT_OK);
			finish();
			return true;
		case R.id.action_change_ui:
			switcher.showNext();
			setHalfClickLayout();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}		
	}

	@Override
	public void onClick(View v) {		
		if(v == halfClick) {
			MouseCommand toSend = MouseCommand.HALF_CLICK;
			sendCommandToDevice(toSend);		    
			isHalfClickEnabled = ! isHalfClickEnabled;
			setHalfClickLayout();
		} 
		else if (v == doubleClick){
			MouseCommand toSend = MouseCommand.DOUBLE_CLICK;
			sendCommandToDevice(toSend);
		} 
		else if(v == click ){
			MouseCommand toSend = MouseCommand.CLICK;
			sendCommandToDevice(toSend);
		}
		else if (v == rightClick){
			MouseCommand toSend = MouseCommand.RIGHT_BUTTON_CLICK;
			sendCommandToDevice(toSend);
		} 
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//handle no_buttons events
		if(v == no_buttons){
			return detector.onTouchEvent(event);
		}
		
		//handle buttons events 
		if(event.getAction() == MotionEvent.ACTION_DOWN){			
			if(v == scrollDown){
				MouseCommand toSend = MouseCommand.SCROLL_DOWN_START;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollUp){
				MouseCommand toSend = MouseCommand.SCROLL_UP_START;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollLeft){
				MouseCommand toSend = MouseCommand.SCROLL_LEFT_START;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollRight){
				MouseCommand toSend = MouseCommand.SCROLL_RIGHT_START;
				sendCommandToDevice(toSend);
			}
		}
		else if (event.getAction() == MotionEvent.ACTION_UP){
			if(v == scrollDown){
				MouseCommand toSend = MouseCommand.SCROLL_DOWN_END;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollUp){
				MouseCommand toSend = MouseCommand.SCROLL_UP_END;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollLeft){
				MouseCommand toSend = MouseCommand.SCROLL_LEFT_END;
				sendCommandToDevice(toSend);
			} 
			else if(v == scrollRight){
				MouseCommand toSend = MouseCommand.SCROLL_RIGHT_END;
				sendCommandToDevice(toSend);
			}
			
		}			
		
		return true;
	}
	

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);        
       if(requestCode == RequestCode.ENABLE_BLUETOOTH.ordinal()){
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show();
                /*try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}*/
                isPausedByBTEnable = false;
            } 
            else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Bluetooth n�o est� habilitado", Toast.LENGTH_SHORT).show();
      //          ignoreDisableBluetoothThread = true;
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onBackPressed() {
		if(commandService.getState() == BluetoothService.STATE_CONNECTED){
			AlertDialog.Builder dialog = new AlertDialog.Builder(MouseWithButtonsActivity.this);
	
			dialog.setMessage("A conex�o atual ser� encerrada. Deseja continuar?")
			       .setTitle("Voltar");
			
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   isBackButtonPressed = true;
		        	   disconnectCommandService();
		        	   setProgressBarIndeterminateVisibility(true);
		           }
		       });
			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog, do nothing
		           }
		       });
			dialog.show();
		}
		else{
			isBackButtonPressed = true;
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	//===========================
	// UI setup methods
	//===========================
	
	private void setupNoButtonsUI(){
		no_buttons = (TextView) findViewById(R.id.touchMouse);
	    no_buttons.setOnTouchListener(this);
	}
    
    private void setupButtonsUI() {
        halfClick = (Button) findViewById(R.id.halfClick);
		doubleClick = (Button) findViewById(R.id.doubleClick);
		click = (Button) findViewById(R.id.click);
		rightClick = (Button) findViewById(R.id.rightClick);
		scrollRight = (ImageButton) findViewById(R.id.scrollRight);
		scrollLeft = (ImageButton) findViewById(R.id.scrollLeft);
		scrollDown = (ImageButton) findViewById(R.id.scrollDown);
		scrollUp = (ImageButton) findViewById(R.id.scrollUp);
		
		setButtonsEvent();
    }
    
    private void setButtonsEvent() {
		halfClick.setOnClickListener(this);
		doubleClick.setOnClickListener(this);
		click.setOnClickListener(this);
		rightClick.setOnClickListener(this);
		
		scrollLeft.setOnTouchListener(this);
		scrollRight.setOnTouchListener(this);
		scrollDown.setOnTouchListener(this);
		scrollUp.setOnTouchListener(this);
	}
    
    private void disableButtons(){
    	halfClick.setEnabled(false);
 		doubleClick.setEnabled(false);
 		click.setEnabled(false);
 		rightClick.setEnabled(false);
 		scrollLeft.setEnabled(false);
 		scrollRight.setEnabled(false);
 		scrollUp.setEnabled(false);
 		scrollDown.setEnabled(false);

		setButtonsBackgroundDisabled();
    }
    
    private void enableButtons(){
   	 	halfClick.setEnabled(true);
		doubleClick.setEnabled(true);
		click.setEnabled(true);
		rightClick.setEnabled(true);
 		scrollLeft.setEnabled(true);
 		scrollRight.setEnabled(true);
 		scrollUp.setEnabled(true);
 		scrollDown.setEnabled(true);
		
		setButtonsBackgroundEnabled();
   }
    
	private void setButtonsBackgroundEnabled() {
		halfClick.setBackgroundColor(Color.YELLOW);
		doubleClick.setBackgroundColor(Color.RED);
		click.setBackgroundColor(Color.CYAN);
		rightClick.setBackgroundColor(Color.GREEN);
		
		scrollLeft.setBackgroundColor(Color.GRAY);
		scrollRight.setBackgroundColor(Color.GRAY);
		scrollDown.setBackgroundColor(Color.GRAY);
		scrollUp.setBackgroundColor(Color.GRAY);
	}
	
	private void setButtonsBackgroundDisabled() {
		halfClick.setBackgroundColor(Color.LTGRAY);
		doubleClick.setBackgroundColor(Color.LTGRAY);
		click.setBackgroundColor(Color.LTGRAY);
		rightClick.setBackgroundColor(Color.LTGRAY);
		
		scrollLeft.setBackgroundColor(Color.LTGRAY);
		scrollRight.setBackgroundColor(Color.LTGRAY);
		scrollDown.setBackgroundColor(Color.LTGRAY);
		scrollUp.setBackgroundColor(Color.LTGRAY);
	}

	private void setHalfClickLayout() {		
		if(isHalfClickEnabled){
			no_buttons.setBackgroundColor(Color.YELLOW);
			halfClick.setText(getString(R.string.halfClickEnabled));
		}
		else {
			no_buttons.setBackgroundColor(Color.WHITE);
			halfClick.setText(getString(R.string.halfClickDisabled));
		}		
	}
	
	//===========================
	// Bluetooth Service methods
	//===========================
    private void connectCommandService() {
    	Toast.makeText(getApplicationContext(), "Conectando ...", Toast.LENGTH_SHORT).show();
        if (commandService == null) {
        	commandService = new BluetoothService(this, mHandler); 
        }
        
        // Only if the state is STATE_NONE, do we know that we haven't started already
        if (commandService.getState() == BluetoothService.STATE_NONE) {
          // Start the Bluetooth service
          commandService.start();
        }
        
        if(btAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON){
        	Log.e(TAG, "bt state turning on");
        } 
        else if (btAdapter.getState() == BluetoothAdapter.STATE_OFF){
        	Log.e(TAG, "bt state off");
        } 
        else if (btAdapter.getState() == BluetoothAdapter.STATE_ON){
        	Log.e(TAG, "bt state on");
        }
        
        commandService.connect(btDevice, true);
        mustConnectInsecure = true;
	}
    
    private void connectCommandServiceInsecure(){
    //	Toast.makeText(getApplicationContext(), "Conectando (modo inseguro)...", Toast.LENGTH_SHORT).show();
    	commandService.connect(btDevice, false);
    }
    
	protected void sendCommandToDevice(MouseCommand toSend) {
		commandService.write(toSend.getID());
	}
    
    private void disconnectCommandService(){
    	if (commandService != null){
			commandService.stop();
    	}
    }
    
    //===========================
  	// Accelerometer methods
  	//===========================

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		    long curTime = System.currentTimeMillis();
		    // only allow one update every 100ms.
		    if ((curTime - lastUpdate) > 100) {
		      long diffTime = (curTime - lastUpdate);
		      lastUpdate = curTime;
		      float[] values = event.values;
		      
		      x = values[0];
		      y = values[1];
		      z = values[2];

		      float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

		      if (speed > SHAKE_THRESHOLD) {
		    	  //show other ui (if buttons -> no buttons, if no buttons -> buttons)
		    	  switcher.showNext();
		    	  setHalfClickLayout();
		      }
		      last_x = x;
		      last_y = y;
		      last_z = z;
		    }
		  }		
	}	

	//===========================
	// Gesture detector methods
	//===========================

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		sendPendingDoubleClick();		
		doubleClickConfirm = new Thread(new Runnable() {			
			@Override
			public void run() {
				try{
					Thread.sleep(1000);
					//if no interruption, double click is confirmed
					MouseCommand toSend = MouseCommand.DOUBLE_CLICK;
					sendCommandToDevice(toSend);
				} catch(InterruptedException e){
					//triple click detected (HALF_CLICK)
					//do nothing.
				}
			}
		});		
		doubleClickConfirm.start();		
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		MouseCommand toSend;		
		//if double click is waiting to be confirmed, then it is a triple click
		if(doubleClickConfirm != null && doubleClickConfirm.isAlive()){			
			doubleClickConfirm.interrupt();
			toSend = MouseCommand.HALF_CLICK;
			isHalfClickEnabled = !isHalfClickEnabled;			
		}
		else {
			toSend = MouseCommand.CLICK;
		}
		sendCommandToDevice(toSend);
		setHalfClickLayout();
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		//must be true, otherwise other events wont work
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		sendPendingDoubleClick();
		
		MouseCommand toSend = MouseCommand.RIGHT_BUTTON_CLICK;
		sendCommandToDevice(toSend);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		sendPendingDoubleClick();		
		if (e1 == null || e2 == null) return false;
		try {
			float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
			//calculate orientation
			if(Math.abs(diffX) > Math.abs(diffY)){
				//is horizontal
				if(e2.getX() > e1.getX()){
            		MouseCommand toSend = MouseCommand.SCROLL_RIGHT;
            		sendCommandToDevice(toSend);
            	}
				else {
            		MouseCommand toSend = MouseCommand.SCROLL_LEFT;
            		sendCommandToDevice(toSend);
            	}
			} 
			else {
				//is vertical
				if(e2.getY() > e1.getY()){
            		MouseCommand toSend = MouseCommand.SCROLL_DOWN;
            		sendCommandToDevice(toSend);
            	} 
				else {
            		MouseCommand toSend = MouseCommand.SCROLL_UP;
            		sendCommandToDevice(toSend);
            	}
			}
		} 
		catch (Exception e) { e.printStackTrace();}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	private void sendPendingDoubleClick(){
		if(doubleClickConfirm != null && doubleClickConfirm.isAlive()){			
			doubleClickConfirm.interrupt();
			MouseCommand toSend = MouseCommand.DOUBLE_CLICK;
			sendCommandToDevice(toSend);
		}
	}	
}