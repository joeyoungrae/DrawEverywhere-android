package com.draw.free.ar.util;


import androidx.annotation.Keep;

public class JniInterface2 {
    static {
        System.loadLibrary("library_collection");
    }

    @Keep
    public static native String googleClientID();

    @Keep
    public static native String kakaoApi();

    @Keep
    public static native String naverApi();

    @Keep
    public static native String serverEccPublicKey();

    @Keep
    public static native String baseUrl();

    @Keep
    public static native String awsAccessKey();

    @Keep
    public static native String awsSecretKey();


    @Keep
    public static native String awsBucketName();
    

}
