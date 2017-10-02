package com.windworkshop.gaze.glassaysncwork;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by zongbao on 2017/10/2.
 */

public class HttpServer extends NanoHTTPD {
    Context context;
    IHttpDataInterface httpDataInterface;
    public HttpServer(Context context, IHttpDataInterface httpDataInterface) {
        super(8080);
        this.context = context;
        this.httpDataInterface = httpDataInterface;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
        return super.serve(uri, method, headers, parms, files);
    }

    @Override
    public Response serve(IHTTPSession session) {
        System.out.println("===========Start=============");
        boolean isShowDetail = true;
        if(isShowDetail == true) {
            System.out.println("URI:" + session.getUri());
            System.out.println("Method:" + session.getMethod());

            System.out.println("Header");
            for (Map.Entry<String, String> entry : session.getHeaders().entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }

            System.out.println("Parms");
            for (Map.Entry<String, String> entry : session.getParms().entrySet()) {
                System.out.println(entry.getKey() + "=" + entry.getValue());
            }
        }
        System.out.println("===========Finish=============");
        Log.i("myTest", "uri:" + session.getUri());
        //Log.i("myTest", "uri:" + session.getUri());
        if(session.getUri().equals("/")) {
            try {
                InputStream is = context.getResources().getAssets().open("main.html");
                int lenght = is.available();
                byte[]  buffer = new byte[lenght];
                is.read(buffer);
                String result = new String(buffer, "utf8");
                return newFixedLengthResponse(result);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                String uri = session.getUri();
                if(uri.lastIndexOf(".") != -1) {
                    Log.i("myTest", "uri:" + session.getUri());
                    if(uri.lastIndexOf(".") > uri.lastIndexOf("/")) {
                        //
                        // file
                        String type = uri.substring(uri.lastIndexOf(".") + 1, uri.length());
                        Log.i("myTest", type);
                        InputStream is = context.getResources().getAssets().open(session.getUri().substring(1));
                        int lenght = is.available();
                        byte[]  buffer = new byte[lenght];
                        is.read(buffer);
                        String result = new String(buffer, "utf8");
                        //result = compress(result);
                        Response response = newFixedLengthResponse(result);
                        if(type.equals("css")) {
                            response.addHeader("Content-Type", "text/css");
                        } else if(type.equals("js")) {
                            response.addHeader("Content-Type", "application/x-javascript");
                        }
                        return response;
                    }
                } else {
                    // page
                    if(session.getUri().equals("/sync")) {
                        if(session.getParms().get("type").equals("text")) {
                            String result = getStringWithStream(session.getInputStream());
                            Intent intent = new Intent(HttpServerService.SERVER_MESSAGE);
                            intent.putExtra("type", "text");
                            intent.putExtra("text", result);
                            context.sendBroadcast(intent);
                            Log.i("myTest", "get input stream:" + result);
                            return newFixedLengthResponse("OK");
                        } else if(session.getParms().get("type").equals("get_text")) {
                            return newFixedLengthResponse(httpDataInterface.getSyncText());
                        }
                    }
                    Log.i("myTest", "request:" + session.getUri());


                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newFixedLengthResponse("Hello");
    }

    private String getStringWithStream(InputStream is) throws IOException {
        int lenght = is.available();
        byte[]  buffer = new byte[lenght];
        is.read(buffer);
        String result = new String(buffer, "utf8");
        return result;
    }
    // 压缩
    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }
}
