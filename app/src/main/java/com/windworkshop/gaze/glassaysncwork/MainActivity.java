package com.windworkshop.gaze.glassaysncwork;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.List;

import madgaze.x5_gesture.activity.MADGestureActivity;
import madgaze.x5_gesture.detector.MADGestureTouchDetector;
import madgaze.x5_gesture.listener.MADTouchGestureListener;

public class MainActivity extends MADGestureActivity {
    TextView ipTextView, syncText;
    ScrollView textScroll;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("syncdata", 0);
        ipTextView = (TextView) findViewById(R.id.ip_textview);
        ipTextView.setText("请在浏览器中输入以打开管理器:" + getIPAddress(getApplicationContext()) + ":8080");
        syncText = (TextView) findViewById(R.id.sync_text);
        //syncText.setText(sp.getString("sync_text", ""));
        try {
            syncText.setText(URLDecoder.decode(sp.getString("sync_text", ""), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        textScroll = (ScrollView) findViewById(R.id.text_scroll);
        System.out.println("serve");
        Log.i("myTest", "testaaaaaaaaaaaaa");
        Intent startServiceIntent = new Intent(MainActivity.this, HttpServerService.class);
        startService(startServiceIntent);

        showSystemParameter();
        if(SystemUtil.getSystemModel().indexOf("MAD Gaze") != -1) {
            MADTouchGestureListener gestureListener = getMadGestureView().getGestureListener();
            gestureListener.setOnScrollListener(new MADGestureTouchDetector.OnScrollListener() {
                @Override
                public void onStart(MotionEvent event) {
                }

                @Override
                public void onMove(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, boolean isDrag) {
                    //Log.e("myTest", "onMove:" + distanceX);
                    textScroll.scrollTo(0, (int) (textScroll.getScrollY()+distanceY));
                }

                @Override
                public void onMoveEnd(MotionEvent event) {
                }

                @Override
                public void onFling(MotionEvent event, MotionEvent motionEvent1, float v, float v1) {
                }
            });
        }
        Log.i("myTest",  "ip=" + getHostIP());
    }
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(reciveServerMessage);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(reciveServerMessage, new IntentFilter(HttpServerService.SERVER_MESSAGE));
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = "";
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("myTest", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    BroadcastReceiver reciveServerMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(HttpServerService.SERVER_MESSAGE)) {
                String type = intent.getStringExtra("type");
                if(type.equals("text")) {
                    String result = intent.getStringExtra("text");
                    String[] requestContext = result.split("=");
                    // TODO 完成解析键值对
                    /*for(int i = 0;i < requestContext.length;) {

                        i += 2;
                    }*/
                    //String string = ;
                    try {
                        syncText.setText(URLDecoder.decode(requestContext[1], "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "同步完成", Toast.LENGTH_SHORT).show();
                    sp.edit().putString("sync_text", requestContext[1]).commit();
                }
            }
        }
    };
    private void showSystemParameter() {
        String TAG = "系统参数：";
        Log.e(TAG, "手机厂商：" + SystemUtil.getDeviceBrand());
        Log.e(TAG, "手机型号：" + SystemUtil.getSystemModel());
        Log.e(TAG, "手机当前系统语言：" + SystemUtil.getSystemLanguage());
        Log.e(TAG, "Android系统版本号：" + SystemUtil.getSystemVersion());
        Log.e(TAG, "手机IMEI：" + SystemUtil.getIMEI(getApplicationContext()));
    }
}
