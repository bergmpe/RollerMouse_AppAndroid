package br.com.blogspot.bergmpe.rollermouse_appandroid;

/**
 * Created by marcio on 30/03/15.
 */
public final class MySingleton {

    private static final MySingleton mySingleton = new MySingleton();
    public static  String ip = "";
    public static int porta = 0;

    private MySingleton() {
    }

    public static MySingleton getIntance(){
        return mySingleton;
    }
}
