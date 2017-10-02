package com.windworkshop.gaze.glassaysncwork;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

/**
 * Created by zongbao on 2017/10/2.
 */

public class HttpServerService extends Service {
    public static final String SERVER_MESSAGE = "com.windworkshop.gaze.glassaysncwork.servermessage";
    IHttpDataInterface httpDataInterface;
    SharedPreferences sp;
    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("syncdata", 0);
        httpDataInterface = new IHttpDataInterface() {
            @Override
            public String getSyncText() {
                return sp.getString("sync_text", "");
            }
        };
        //ServerRunner.run(HttpServer.class);
        Thread thread = new Thread(httpserverThread);
        thread.start();
    }
    Runnable httpserverThread = new Runnable() {
        @Override
        public void run() {
            HttpServer server = new HttpServer(getApplicationContext(), httpDataInterface);
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
