package br.com.blogspot.bergmpe.rollermouse_appandroid;

        import android.content.Intent;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;


public class MainMenuActivity extends ActionBarActivity {

    Button btnInternet;
    Button btnBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnBluetooth = (Button) findViewById(R.id.btnBlueTooth);
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Class aClass = Class.forName("br.edu.ifce.m2ta.MainActivity");
                    Intent intent = new Intent(getApplicationContext(), aClass);
                    startActivity( intent );
                } catch ( ClassNotFoundException e ){
                    e.printStackTrace();
                }
            }
        });

        btnInternet = (Button) findViewById(R.id.btnInternet);
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Class aClass = Class.forName("br.com.blogspot.bergmpe.rollermouse_appandroid.LoginInternet");
                    Intent intent = new Intent(getApplicationContext(), aClass);
                    startActivity( intent );
                } catch ( ClassNotFoundException e ){
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
