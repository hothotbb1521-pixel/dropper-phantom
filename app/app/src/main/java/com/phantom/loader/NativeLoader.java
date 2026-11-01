package com.phantom.loader;

import android.content.Context;

public class NativeLoader {
    static {
        System.loadLibrary("native-loader");
    }
    public static native void loadPayload(Context context);
}
