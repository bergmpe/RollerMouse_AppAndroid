package br.com.blogspot.bergmpe.rollermouse_appandroid;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

/**
 * Classe responsave por fazer o login no servidor, ela tenta se conectar ao pc,
 * usando o ip e  porta passados com parametro nessa activity.
 * Created by Williamberg on 30/03/15.
 */
public class LoginInternet extends Activity {

    Button btnConectar;
    EditText edTxtIp;
    EditText edTxtPorta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_internet_layout);

        edTxtIp = (EditText) findViewById( R.id.editxtIp );
        edTxtPorta = (EditText) findViewById( R.id.editxtPorta );

        btnConectar = (Button) findViewById( R.id.btnConectar );
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySingleton.ip = edTxtIp.getText().toString();
                MySingleton.porta = Integer.valueOf( edTxtPorta.getText().toString() );
                try{
                    Class aClass = Class.forName("br.com.blogspot.bergmpe.rollermouse_appandroid.MyActivity");
                    Intent intent = new Intent( getApplicationContext(), aClass);
                    startActivity( intent );
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
