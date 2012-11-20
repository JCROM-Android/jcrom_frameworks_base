
package com.android.internal.os;

import android.os.Environment;

import java.io.FileInputStream;
import java.util.Properties;

public class AutoRun {
    private static final String CONF_FILENAME = "autorun.conf";
    private static final String PROPERTY_PACKAGE = "package";
    private static final String PROPERTY_UITYPE = "ui";
    private static final String PROPERTY_STATUSBAR = "statusbar";
    private static final String PROPERTY_NAVIGATIONBAR = "navigationbar";
    private static final String PROPERTY_SYSTEMNAVBAR = "systemnavbar";

    private final String mPackageName;
    private final String mClassName;
    private final String mUIType;
    private final boolean mStatusBar;       // Phone UI status bar
    private final boolean mNavigationBar;   // Phone UI navigation bar
    private final boolean mSystemNavBar;    // Tablet UI system navigation bar

    private static AutoRun sInstance = null;

    public static String getExecPackage() {
        return getInstance().mPackageName;
    }

    public static String getExecClass() {
        return getInstance().mClassName;
    }

    public static String getUIType() {
        return getInstance().mUIType;
    }

    public static boolean hasStatusBar() {
        return getInstance().mStatusBar;
    }

    public static boolean hasNavigationBar() {
        return getInstance().mNavigationBar;
    }

    public static boolean hasSystemNavBar() {
        return getInstance().mSystemNavBar;
    }

    private static AutoRun getInstance() {
        if (sInstance == null) {
            sInstance = new AutoRun();
        }
        return sInstance;
    }

    private AutoRun() {
        final Properties p = new Properties();

        // load Properties
        final String files[] = new String[] {
            "/mnt/sdcard-ext/" + CONF_FILENAME,
            "/mnt/sdcard/" + CONF_FILENAME,
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CONF_FILENAME,
        };
        for (String filepath: files) {
            try {
                p.load(new FileInputStream(filepath));
                break;
            } catch (Exception e) {
            }
        }

        // parse package/class component
        String pkg = "", cls = "";
        String component = p.getProperty(PROPERTY_PACKAGE, "");
        if (!"".equals(component)) {
            int pos = component.indexOf('/');
            if (pos > 0) {
                // pkg.name/pkg.name.class.name
                pkg = component.substring(0, pos);
                cls = component.substring(pos + 1);
                if (cls.length() < 1 || cls.charAt(0) == '.') {
                    // pkg.name/.class.name
                    cls = pkg + cls;
                }
            } else {
                // pkg.name.class.name
                pos = component.lastIndexOf('.');
                pkg = component.substring(0, pos);
                cls = component;
            }
        }

        String uitype = p.getProperty(PROPERTY_UITYPE, "").toLowerCase();
        if ("phone".equals(uitype)) {
            uitype = "0";
        } else
        if ("large".equals(uitype)) {
            uitype = "1";
        } else
        if ("tablet".equals(uitype)) {
            uitype = "2";
        }

        mPackageName = pkg;
        mClassName = cls;
        mUIType = uitype;
        mStatusBar = toBoolean(p.getProperty(PROPERTY_STATUSBAR, ""));
        mNavigationBar = toBoolean(p.getProperty(PROPERTY_NAVIGATIONBAR, ""));
        mSystemNavBar = toBoolean(p.getProperty(PROPERTY_SYSTEMNAVBAR, ""));
    }

    private static boolean toBoolean(String value) {
        value = value.toLowerCase();
        if ("false".equals(value) || "no".equals(value) || "off".equals(value) || "0".equals(value)) {
            return false;
        } else {
            return true;
        }
    }
}
