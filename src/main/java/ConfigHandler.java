
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;


import static org.bukkit.Bukkit.getLogger;

class ConfigHandler {
    private File configFile = new File(dataFolder , configName);
    private JSONObject config;
    private static final CraftXen instance = CraftXen.getInstance();
    private static final File dataFolder = instance.getDataFolder();
    private static final String configName = "config.json";

    ConfigHandler() {
    }

    public void createConfig() {

        try {
            if (!dataFolder .exists()) {
                dataFolder.mkdirs();
            }
            if (!configFile.exists()) {
                getLogger().info("Config.yml not found, creating!");
                instance.saveResource(configName, false);
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public String getConfig() {
        System.out.println(configFile);
        if (!configFile.exists()) {
            createConfig();
        }
        StringBuilder fileContents = new StringBuilder((int) configFile.length());
        try (Scanner scanner = new Scanner(configFile)) {

            String lineSeparator = System.getProperty("line.separator");

            try {
                while (scanner.hasNextLine()) {
                    fileContents.append(scanner.nextLine() + lineSeparator);
                }
                return fileContents.toString();
            } finally {
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getConfigData(String key) {
        String data = getConfig();
        JSONObject jsonData;
        String content = null;
        try {
            jsonData = new JSONObject(data);
            if (key != null) {
                content = jsonData.getString(key);
                System.out.println(content);
            } else {
                content = jsonData.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content;
    }

}
