package com.cylan.jiafeigou.widget;

import com.cylan.jiafeigou.misc.JFGRules;

/**
 * Created by hds on 17-3-29.
 */

public class ToolbarTheme {
    private Theme theme;

    private ToolbarTheme() {
    }

    private static ToolbarTheme instance;

    public static ToolbarTheme getInstance() {
        if (instance == null) {
            synchronized (ToolbarTheme.class) {
                if (instance == null)
                    instance = new ToolbarTheme();
            }
        }
        return instance;
    }

    public Theme getCurrentTheme() {
        if (theme == null) {
            theme = new Theme();
        }
        theme.toolbarBackground = JFGRules.getTimeRule();
        return theme;
    }

    public static class Theme {
        private int toolbarBackground;

        public int getToolbarBackground() {
            return toolbarBackground;
        }
    }
}
