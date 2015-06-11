package br.com.blogspot.bergmpe.rollermouse_appandroid;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.graphics.Color;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Message;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.GestureDetector;
        import android.view.MotionEvent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.ViewSwitcher;
        import android.os.Handler;

        import com.mouseacessivel.library.MouseCommand;

        import java.io.IOException;
        import java.net.UnknownHostException;
        import java.util.logging.LogRecord;

public class MyActivity extends Activity implements View.OnClickListener, View.OnTouchListener,
        GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener{

    private TCPCliente mTcpClient;
    private Handler handler;
    private AlertDialog alerta;
    private ProgressDialog progressDialog;

    //UI variables
    private Button halfClick, doubleClick, click, rightClick;
    private ImageButton scrollUp, scrollDown, scrollRight, scrollLeft;
    private ViewSwitcher switcher;
    private TextView no_buttons;
    //detects gestures on no_iu screen
    private GestureDetector detector;
    private Thread doubleClickConfirm;

    private boolean isHalfClickEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse_with_buttons);

        // try to connect to the server
        new connectTask().execute("");//chama o metodo doInBackground.
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                processMsg( msg );
            }
        };

        detector = new GestureDetector( MyActivity.this, this );
        detector.setOnDoubleTapListener( this );
        setupButtonsUI();
        setupNoButtonsUI();
        switcher = (ViewSwitcher) findViewById( R.id.mouseSwitcher );

        adaptToScreenSize();

        //isBackButtonPressed = false;
        //isPausedByBTEnable = false;
        //ignoreDisableBluetoothThread = false;
        isHalfClickEnabled = false;
        progressDialog = new ProgressDialog( this );
    }

    /**
     * Muda o layout da tela caso a altura ou a largura da tela seja menor do que 600dp.*/
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
    public void onClick(View v) {
        if(v == halfClick) {
            MouseCommand toSend = MouseCommand.HALF_CLICK;
            //sends the message to the server
            if (mTcpClient != null) {
                mTcpClient.sendMessage(toSend.getID());
            }
            isHalfClickEnabled = ! isHalfClickEnabled;
            setHalfClickLayout();
        }
        else if (v == doubleClick){
            MouseCommand toSend = MouseCommand.DOUBLE_CLICK;
            //sends the message to the server
            if (mTcpClient != null) {
                mTcpClient.sendMessage(toSend.getID());
            }
        }
        else if(v == click ){
            MouseCommand toSend = MouseCommand.CLICK;
            //sends the message to the server
            if (mTcpClient != null) {
                mTcpClient.sendMessage(toSend.getID());

            }
            disableButtons();
        }
        else if (v == rightClick){
            MouseCommand toSend = MouseCommand.RIGHT_BUTTON_CLICK;
            //sends the message to the server
            if (mTcpClient != null) {
                mTcpClient.sendMessage(toSend.getID());
            }
        }
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
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollUp){
                MouseCommand toSend = MouseCommand.SCROLL_UP_START;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollLeft){
                MouseCommand toSend = MouseCommand.SCROLL_LEFT_START;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollRight){
                MouseCommand toSend = MouseCommand.SCROLL_RIGHT_START;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage( toSend.getID() );
                }
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            if(v == scrollDown){
                MouseCommand toSend = MouseCommand.SCROLL_DOWN_END;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollUp){
                MouseCommand toSend = MouseCommand.SCROLL_UP_END;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollLeft){
                MouseCommand toSend = MouseCommand.SCROLL_LEFT_END;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }
            else if(v == scrollRight){
                MouseCommand toSend = MouseCommand.SCROLL_RIGHT_END;
                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(toSend.getID());
                }
            }

        }

        return true;
    }

    private void setupNoButtonsUI(){
        no_buttons = (TextView) findViewById( R.id.touchMouse );
        no_buttons.setOnTouchListener( this );
    }

    private void setupButtonsUI() {
        halfClick = (Button) findViewById( R.id.halfClick );
        doubleClick = (Button) findViewById( R.id.doubleClick );
        click = (Button) findViewById( R.id.click );
        rightClick = (Button) findViewById( R.id.rightClick );
        scrollRight = (ImageButton) findViewById( R.id.scrollRight );
        scrollLeft = (ImageButton) findViewById( R.id.scrollLeft );
        scrollDown = (ImageButton) findViewById( R.id.scrollDown );
        scrollUp = (ImageButton) findViewById( R.id.scrollUp );

        setButtonsEvent();
    }

    private void setButtonsEvent() {
        halfClick.setOnClickListener( this );
        doubleClick.setOnClickListener( this );
        click.setOnClickListener( this );
        rightClick.setOnClickListener( this );

        scrollLeft.setOnTouchListener( this );
        scrollRight.setOnTouchListener( this );
        scrollDown.setOnTouchListener( this );
        scrollUp.setOnTouchListener( this );
    }

    /**
     * Deixa os botões coloridos.*/
    private void setButtonsBackgroundEnabled() {
        halfClick.setBackgroundColor( Color.YELLOW );
        doubleClick.setBackgroundColor( Color.RED );
        click.setBackgroundColor( Color.CYAN );
        rightClick.setBackgroundColor( Color.GREEN );

        scrollLeft.setBackgroundColor( Color.GRAY );
        scrollRight.setBackgroundColor( Color.GRAY );
        scrollDown.setBackgroundColor( Color.GRAY );
        scrollUp.setBackgroundColor( Color.GRAY );
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

    private void disableButtons(){
        halfClick.setEnabled( false );
        doubleClick.setEnabled( false );
        click.setEnabled( false );
        rightClick.setEnabled( false );
        scrollLeft.setEnabled( false );
        scrollRight.setEnabled( false );
        scrollDown.setEnabled( false );
        scrollUp.setEnabled( false );
        setButtonsBackgroundDisabled();
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
                    //sends the message to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(toSend.getID());
                    }
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
        //sends the message to the server
        if (mTcpClient != null) {
            mTcpClient.sendMessage(toSend.getID());
        }
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
        //sends the message to the server
        if (mTcpClient != null) {
            mTcpClient.sendMessage(toSend.getID());
        }
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
                    //sends the message to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(toSend.getID());
                    }
                }
                else {
                    MouseCommand toSend = MouseCommand.SCROLL_LEFT;
                    //sends the message to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(toSend.getID());
                    }
                }
            }
            else {
                //is vertical
                if(e2.getY() > e1.getY()){
                    MouseCommand toSend = MouseCommand.SCROLL_DOWN;
                    //sends the message to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(toSend.getID());
                    }
                }
                else {
                    MouseCommand toSend = MouseCommand.SCROLL_UP;
                    //sends the message to the server
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(toSend.getID());
                    }
                }
            }
        }
        catch (Exception e) {}
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
            //sends the message to the server
            if (mTcpClient != null) {
                mTcpClient.sendMessage(toSend.getID());
            }
        }
    }

    public void Error(){
        Toast.makeText(this, "error O CONE", Toast.LENGTH_LONG).show();
    }

    private void processMsg( Message msg ) {
        if(msg.what == 1){
            //Cria o gerador do AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //define o titulo
            builder.setTitle( "Error" );
            //define a mensagem
            builder.setMessage( msg.obj.toString() );
            //define um botão como ok.
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            });
            //cria o AlertDialog
            alerta = builder.create();
            alerta.setCanceledOnTouchOutside( false );//clickar fora do alertDialog, não fecha ele.
            //Exibe
            alerta.show();
        }
        else if ( msg.what == 2 ){
            progressDialog.setTitle( "Conectando. . ." );
            progressDialog.setMessage( "Por favor aguarde." );
            progressDialog.setCancelable( false );
            progressDialog.setIndeterminate( true );
            progressDialog.show();
        }
    }

    public class checkConnectionTask extends AsyncTask<String,String,Void>{

        @Override
        protected Void doInBackground(String... params) {
            boolean lostConnection = false;
            while( true ){
                lostConnection =  mTcpClient.checkConnection();
                publishProgress( lostConnection );
            }



        }
        protected void onProgressUpdate(Boolean ... progress) {
            //This method runs on the UI thread, it receives progress updates
            //from the background thread and publishes them to the status bar

        }

    }
    public class connectTask extends AsyncTask<String,String,TCPCliente> {

        @Override
        protected TCPCliente doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPCliente( handler, new TCPCliente.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            try {
                Message message1 = new Message();
                message1.what = 2;
                handler.sendMessage( message1 );
                mTcpClient.connect();
                progressDialog.dismiss();
                setButtonsBackgroundEnabled();
                mTcpClient.run();
            } catch ( UnknownHostException e0 ){
                if(progressDialog != null)
                    progressDialog.dismiss();
                Log.e("TCP Error", e0.getMessage());
                Message message1 = new Message();
                message1.what = 1;
                message1.obj = "Error na conexão endereço errado";
                handler.sendMessage( message1);
            }
            catch ( IOException e1 ) {
                if(progressDialog != null)
                    progressDialog.dismiss();
                Log.e("TCP Error", e1.getMessage());
                Message message1 = new Message();
                message1.what = 1;
                message1.obj = "Error na conexão.";
                handler.sendMessage( message1 );
            }

            return null;
        }
    }
}
