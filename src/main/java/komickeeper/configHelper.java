package komickeeper;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public class configHelper {
    // Makes our config dir and the data base dir
    public static void makeConfigDirs() {
        File configDir = new File(getConfigDir());
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        File databaseDir = new File(getDatabaseDir());
        if (!databaseDir.exists()) {
            databaseDir.mkdir();
        }
    }
    public static Ini getSettingFile() {
        try {
            if (!new File(getConfigDir() + File.separator + "settings.ini").exists()) {
                new File(getConfigDir() + File.separator + "settings.ini").createNewFile();
            }
            return new Ini(new File(getConfigDir() + File.separator + "settings.ini"));
        } catch (IOException e) {
            return null;
        }
    }

    public static void setSetting(String settingName, String value) {
        try {
            Ini ini = Main.config;
            ini.put("config", settingName, value);
            ini.store();
        } catch (IOException e) {
            Controller.log("Unable to set setting");
        }
    }

    public static String getSetting(Ini configFile, String setting, String defaultVal) {
        String val = configFile.fetch("config", setting);
        if (val == null) {
            return defaultVal;
        }
        return val;
    }

    private static String getConfigDir() {
        final String OS = System.getProperty("os.name").toLowerCase();
        String appName = "komickeeper";
        if (OS.contains("win")) {
            return System.getenv("LOCALAPPDATA") + File.separator + appName;
        } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("bsd")) {
            return System.getProperty("user.home") + File.separator + ".config" + File.separator + appName;
        } else if (OS.contains("mac")) {
            return System.getProperty("user.home")
                    + File.separator + "Library" + File.separator + "Application Support" + File.separator + appName;
        }
        return "./";
    }

    public static String getDatabaseDir() {
        return getConfigDir() + File.separator + "dbs";
    }

    public static String getDatabasePath() {
        return getDatabaseDir() + File.separator + Main.databaseName;
    }
}
