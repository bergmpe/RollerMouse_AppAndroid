package br.edu.ifce.m2ta;

import br.com.blogspot.bergmpe.rollermouse_appandroid.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import br.edu.ifce.m2ta.BluetoothActivity;

public class MainActivity extends Activity implements OnClickListener{

    private TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // shows the screen: iniciando a aplica��o: toque se a busca de dispositivos n�o foi iniciada automaticamente
        txtView = (TextView) findViewById(R.id.txtView1);
        txtView.setOnClickListener(this);

        // inicia a busca de dispositivos (BluetoothActivity)
        onClick(txtView);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * onBackPressed
     *     exit lancher when user touches on screen two times in a 2s interval
     * @see Activity#onBackPressed()
     * @author nta.ifce
     * @version 1.0
     */
    @Override
    public void onBackPressed() {
        // do nothing
    }
}
