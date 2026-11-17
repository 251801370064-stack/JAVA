package com.library.view;

import javax.swing.UIManager;

public final class UiDefaults {
    private UiDefaults() {
    }

    public static void install() {
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ScrollBar.width", 12);
    }
}
