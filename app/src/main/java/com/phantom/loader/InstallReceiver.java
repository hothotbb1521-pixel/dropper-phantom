package com.phantom.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Log.d("Install", "Payload instalado silenciosamente!");
        } else {
            Log.e("Install", "Falha: " + status);
        }
    }
}
