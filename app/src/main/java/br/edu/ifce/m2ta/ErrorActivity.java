package br.edu.ifce.m2ta;

//import br.edu.ifce.m2ta.R;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.blogspot.bergmpe.rollermouse_appandroid.R;

public class ErrorActivity extends Activity implements OnClickListener{
	//UI variables
	private TextView errorMsg;
	private ImageView image;
	
	//string to get extra on intent
	public static final String EXTRA_ERROR_MESSAGE = "errorMessage";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_error);
		
		image = (ImageView) findViewById(R.id.image_sad);
		image.setOnClickListener(this);
		
		errorMsg = (TextView) findViewById(R.id.error_msg);
		String msg = getIntent().getStringExtra(EXTRA_ERROR_MESSAGE);
		if(msg != null && msg.length() > 0){
			errorMsg.setText(msg + "\n\n Toque para fechar aplicação");
		}
		errorMsg.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			finish();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
		
	}

	@Override
	public void onClick(View v) {
		finish();
		//disable bluetooth
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter!=null && btAdapter.isEnabled()) {
		    btAdapter.disable(); 
		}
	}

}
