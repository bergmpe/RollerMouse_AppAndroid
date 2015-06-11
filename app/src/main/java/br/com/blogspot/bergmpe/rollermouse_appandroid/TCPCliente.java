package br.com.blogspot.bergmpe.rollermouse_appandroid;

/**
 * Created by Williamberg on 28/03/2015.
 */


        import android.app.AlertDialog;
        import android.content.Context;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.widget.Toast;

        import java.io.*;
        import java.net.InetAddress;
        import java.net.Socket;
        import java.net.UnknownHostException;

public class TCPCliente {

    private String serverMessage;
    public  String serverIp;    //o ip do computador que você quer se conectar.
    public int serverPort;      //a porta na qual você quer se conectar.
    private Socket socket;      //socket para fazer a conexão.
    private Socket listenSocket;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private Handler handler;

    private BufferedWriter out; //writter, com isso mando mensagens para o servidor.
    private BufferedReader in;  //reader, com isso leio mensagens do servidor.

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPCliente( Handler handler, OnMessageReceived listener ) {

        this.handler = handler;
        mMessageListener = listener;
        serverIp = MySingleton.ip;
        serverPort = MySingleton.porta;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage( String message ){
        if ( out != null ) {
            try {
                out.write( Integer.valueOf( message ) );
                out.flush();
            } catch ( IOException e ) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage( msg );
            }
        }
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage( int message ){
        if ( out != null ) {
            try {
                out.write( message );
                out.flush();
            } catch ( IOException e ) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage( msg );
            }
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void connect() throws UnknownHostException, IOException{
        serverIp = MySingleton.ip;
        serverPort = MySingleton.porta;
        //here you must put your computer's IP address.
        InetAddress serverAddr = InetAddress.getByName(serverIp);

        Log.e("TCP Client", "C: Connecting...");
        Log.e("TCP Client", "meu ip: " + MySingleton.ip + " porta " + MySingleton.porta);

        //create a socket to make the connection with the server
        socket = new Socket(serverAddr, serverPort);
        //create a socket to check the connection.
        listenSocket = new Socket( serverAddr, 12346 );
        //send the message to the server
        out = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ));
        sendMessage( 0 );
        Log.e("TCP Client", "C: Sent.");
        //receive the message which the server sends back
        in = new BufferedReader(new InputStreamReader( socket.getInputStream()) );
    }

    public void run() {

        mRun = true;

        try {

            //in this while the client listens for the messages sent by the server
            while (mRun) {
                serverMessage = in.readLine();

                if (serverMessage != null && mMessageListener != null) {
                    //call the method messageReceived from MyActivity class
                    mMessageListener.messageReceived(serverMessage);
                }
                serverMessage = null;
            }

            Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

        } catch (Exception e) {

            Log.e("TCP", "S: Error", e);

        } finally {
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void checkConnection(){
        InputStream in;
        OutputStream out;
        boolean isConnected = false;
        int result = 0;
        int fails = 0;
        try {
            in = listenSocket.getInputStream();
            out = listenSocket.getOutputStream();
            while( isConnected ){
                result = 0;
                try {
                    Thread.sleep( 1000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result = in.read();
                if( result == -1){
                    //error na leitura
                    fails++;
                    if( fails == 2 ){
                        isConnected = false;
                    }
                }
                else{
                    out.write( 0 );
                    out.flush();
                    fails = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
