package me.hasenzahn1.attributeManager;

import me.hasenzahn1.attributeManager.command.NewAttributeCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class AttributeManager extends JavaPlugin {

    private static String PREFIX;
    private static AttributeManager instance;

    @Override
    public void onEnable() {
        instance = this;

        if(!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
            reloadConfig();
        }

        FileConfiguration configuration = getConfig();
        PREFIX = configuration.getString("prefix", "&7[&bAttribute&7] ");


        for(Attribute attribute : Attribute.values()) {
            String name = NewAttributeCommand.getNameFromAttribute(attribute);
            if(!configuration.getBoolean(name, false)) {
                continue;
            }

            registerCommand(attribute);
            getComponentLogger().debug("Registered Command: " + name);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommand(Attribute attribute) {
        Bukkit.getServer().getCommandMap().register(getName(), new NewAttributeCommand(this, attribute));
    }

    public static Component getPrefixedLang(String path, Object... replacements) {
        String lang = instance.getConfig().getString("lang." + path, "&cUnknown language key \"&6lang." + path + "&c\"");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            lang = lang.replace("%" + replacements[i].toString() + "%", replacements[i + 1].toString());
        }
        return LegacyComponentSerializer.legacy('§').deserialize(ChatColor.translateAlternateColorCodes('&', PREFIX + lang));
    }
}
