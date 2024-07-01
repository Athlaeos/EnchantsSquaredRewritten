package me.athlaeos.enchantssquared.config;

import me.athlaeos.enchantssquared.EnchantsSquared;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Config {
    private final String name;
    private File file;
    private YamlConfiguration config;
    private final EnchantsSquared plugin = EnchantsSquared.getPlugin();

    public Config(String name) {
        this.name = name;
    }

    public Config save() {
        if ((this.config == null) || (this.file == null))
            return this;
        try {
            if (!config.getConfigurationSection("").getKeys(true).isEmpty())
                config.save(this.file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    public YamlConfiguration get() {
        if (this.config == null)
            reload();

        return this.config;
    }

    public Config saveDefaultConfig() {
        file = new File(plugin.getDataFolder(), this.name);

        plugin.saveResource(this.name, false);

        return this;
    }

    public Config reload() {
        if (file == null)
            this.file = new File(plugin.getDataFolder(), this.name);

        this.config = YamlConfiguration.loadConfiguration(file);

        Reader defConfigStream;
        try {
            defConfigStream = new InputStreamReader(plugin.getResource(this.name), StandardCharsets.UTF_8);

            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.config.setDefaults(defConfig);
            }
        } catch (NullPointerException ignored) {
        }
        return this;
    }

    public Config copyDefaults(boolean force) {
        get().options().copyDefaults(force);
        return this;
    }

    public Config set(String key, Object value) {
        get().set(key, value);
        return this;
    }

    public Object get(String key) {
        return get().get(key);
    }
}