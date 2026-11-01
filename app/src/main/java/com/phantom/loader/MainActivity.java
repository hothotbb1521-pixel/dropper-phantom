package com.phantom.loader;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-loader"); // Carrega o C++
    }

    public native byte[] decryptDex(byte[] encryptedDex);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 100, 50, 50);
        
        TextView tv = new TextView(this);
        tv.setText("Sistema Pronto.");
        layout.addView(tv);
        
        Button btn = new Button(this);
        btn.setText("Iniciar");
        btn.setOnClickListener(v -> loadPayload());
        layout.addView(btn);
        
        setContentView(layout);
    }

    private void loadPayload() {
        new Thread(() -> {
            try {
                // Tenta ler o payload criptografado (vai dar erro se não existir, mas o build vai passar)
                InputStream is = getAssets().open("payload.dex.enc");
                byte[] enc = new byte[is.available()];
                is.read(enc);
                is.close();

                byte[] dec = decryptDex(enc);
                android.util.Log.d("Loader", "Tamanho do DEX descriptografado: " + dec.length);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
