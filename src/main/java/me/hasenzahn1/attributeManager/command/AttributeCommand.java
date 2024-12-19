package me.hasenzahn1.attributeManager.command;

import me.hasenzahn1.attributeManager.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AttributeCommand implements CommandExecutor, TabCompleter {

    private final Attribute attribute;
    private final String name;
    private final String permission;

    public AttributeCommand(Attribute attribute) {
        this.attribute = attribute;

        String value = attribute.getKey().value();
        name = value.replace("generic.", "").replace("player.", "");
        permission = "attributemanager.command." + name;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!commandSender.hasPermission("attributemanager.command." + attribute.getKey().value())){
            commandSender.sendMessage(AttributeManager.getPrefixedLang("invalidPermission"));
            return true;
        }

        if(args.length < 2){
            commandSender.sendMessage(AttributeManager.getPrefixedLang("invalidCommand", "attribute", name));
            return true;
        }

        List<Entity> entities = Bukkit.selectEntities(commandSender, args[0]);

        if(entities.isEmpty()){
            commandSender.sendMessage(AttributeManager.getPrefixedLang("noEntitiesSelected", "selector", args[0]));
            return true;
        }

        if(args[1].equalsIgnoreCase("set")){
            handleSetAttribute(commandSender, entities, Arrays.copyOfRange(args, 2, args.length));
        } else if (args[1].equalsIgnoreCase("reset")){
            handleResetAttribute(commandSender, entities);
        } else {
            commandSender.sendMessage(AttributeManager.getPrefixedLang("invalidCommand", "attribute", name));
        }
        return true;
    }

    private void handleSetAttribute(CommandSender commandSender, List<Entity> entities, String[] args){
        if(args.length != 1){
            commandSender.sendMessage(AttributeManager.getPrefixedLang("invalidCommandSet", "attribute", name));
            return;
        }

        if(!isDouble(args[0])){
            commandSender.sendMessage(AttributeManager.getPrefixedLang("invalidValue", "value", args[0]));
            return;
        }

        double value = Double.parseDouble(args[0]);

        int successCount = 0;
        int failedCount = 0;

        for(Entity entity : entities){
            if(!(entity instanceof Attributable)) {
                failedCount++;
                continue;
            }

            AttributeInstance instance = ((Attributable) entity).getAttribute(attribute);
            if(instance == null) {
                failedCount++;
                continue;
            }
            instance.setBaseValue(value);
            successCount++;
        }

        commandSender.sendMessage(AttributeManager.getPrefixedLang("successSet", "name", name, "valid", successCount, "failed", failedCount));
    }

    private void handleResetAttribute(CommandSender commandSender, List<Entity> entities){
        int successCount = 0;
        int failedCount = 0;

        for(Entity entity : entities){
            if(!(entity instanceof Attributable)) {
                failedCount++;
                continue;
            }

            AttributeInstance instance = ((Attributable) entity).getAttribute(attribute);
            if(instance == null) {
                failedCount++;
                continue;
            }
            instance.setBaseValue(instance.getDefaultValue());
            successCount++;
        }

        commandSender.sendMessage(AttributeManager.getPrefixedLang("successReset", "name", name, "valid", successCount, "failed", failedCount));

    }

    private boolean isDouble(String args){
        try{
            Double.parseDouble(args);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length == 1){
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.startsWith(strings[0])).sorted().toList();
        }
        if(strings.length == 2){
            return Stream.of("set", "reset").filter(n -> n.startsWith(strings[1])).sorted().toList();
        }
        return List.of();
    }

    public String getName() {
        return name;
    }
}

// /scale <player> set 10
// /scale <player> reset