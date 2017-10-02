package com.windworkshop.gaze.glassaysncwork;

/**
 * Created by zongbao on 2017/10/2.
 */

public class HttpParam {
    String key;
    String value;
    private HttpParam(String key, String value) {
        this.key = key;
        this.value = value;
    }
    public static HttpParam create(String key, String value) {
        return new HttpParam(key, value);
    }
}
