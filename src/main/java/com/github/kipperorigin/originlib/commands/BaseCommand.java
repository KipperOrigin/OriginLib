package com.github.kipperorigin.originlib.commands;

import com.github.kipperorigin.originlib.commands.parameters.CommandParameter;
import com.github.kipperorigin.originlib.utils.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

// Core abstract class for creating commands with parameters, optional parameters, and flags.
// Supports permissions, tab completion, usage help, and detailed error handling.
public abstract class BaseCommand {

    // Command metadata
    private String command;
    private String description = "No description provided.";
    private String permission;
    private String[] helpMessageChunks;
    private boolean playerRequired;

    // Command structure maps
    private final Map<String, CommandParameter> flags = new HashMap<>();
    private final Map<String, String> flagAliases = new HashMap<>();
    private final List<CommandParameter> parameters = new ArrayList<>();
    private final Map<String, CommandParameter> optionalParameters = new HashMap<>();
    private final Map<String, String> optionalParamAliases = new HashMap<>();
    private final List<List<String>> tabCompletes = new ArrayList<>();

    public BaseCommand(String command) {
        this.command = command;
        this.helpMessageChunks = new String[]{"Default help message for " + command};
    }

    /**
     * Returns the command name.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns all tab completion lists for this command's parameters.
     */
    public List<List<String>> getTabCompletes() {
        return tabCompletes;
    }

    // Add required parameter
    public void addParameter(CommandParameter parameter) {
        parameters.add(parameter);
        if (parameter.getTabCompletes() != null) tabCompletes.add(parameter.getTabCompletes());
    }

    // Add optional parameter by a single alias or multiple aliases
    public void addOptionalParameter(String prefix, CommandParameter parameter) {
        optionalParameters.put(prefix, parameter);
        optionalParamAliases.put(prefix, prefix);
    }

    public void addOptionalParameter(String[] aliases, CommandParameter parameter) {
        for (String alias : aliases) {
            optionalParameters.put(alias, parameter);
            optionalParamAliases.put(alias, aliases[0]);
        }
    }

    // Add flag with or without aliases
    public void addFlag(String flag, CommandParameter parameter) {
        flags.put(flag, parameter);
        flagAliases.put(flag, flag);
    }

    public void addFlag(String[] aliases, CommandParameter parameter) {
        for (String alias : aliases) {
            flags.put(alias, parameter);
            flagAliases.put(alias, aliases[0]);
        }
    }

    // Metadata setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setPlayerRequired(boolean playerRequired) {
        this.playerRequired = playerRequired;
    }

    public void setHelpMessageChunks(String[] helpMessageChunks) {
        this.helpMessageChunks = helpMessageChunks;
    }

    // Command entry point — parses input, checks permissions, and delegates to runCommand
    public void execute(CommandSender sender, String[] arguments) {
        if (playerRequired && !(sender instanceof Player)) {
            MessageUtil.sendCustomMessage(sender, "&cThis command can only be used by a player!");
            return;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use the \"" + command + "\" command!");
            return;
        }

        List<String> passedFlags = new ArrayList<>();
        List<Object> passedParameters = new ArrayList<>();
        Map<String, Object> passedOptionalParameters = new HashMap<>();

        for (String argument : arguments) {
            if (handleFlag(sender, argument, passedFlags)) continue;
            if (handleOptionalParameter(sender, argument, passedOptionalParameters)) continue;
            if (!handleParameter(sender, argument, passedParameters)) return;
        }

        if (passedParameters.size() < parameters.size()) {
            MessageUtil.sendShortCommandMessage(sender, command, parameters.size());
            return;
        }
    }

    // Handle single-word flags like "--debug"
    private boolean handleFlag(CommandSender sender, String argument, List<String> passedFlags) {
        if (!flags.containsKey(argument)) return false;

        CommandParameter flagParam = flags.get(argument);
        String primary = flagAliases.get(argument);

        if (flagParam.getPermission() != null && !sender.hasPermission(flagParam.getPermission())) {
            MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use " + primary + " for " + command + ".");
            return true;
        }

        passedFlags.add(primary);
        return true;
    }

    // Handle optional parameters like "player:Steve"
    private boolean handleOptionalParameter(CommandSender sender, String argument, Map<String, Object> passedOptionalParameters) {
        for (Map.Entry<String, CommandParameter> entry : optionalParameters.entrySet()) {
            String key = entry.getKey();
            CommandParameter parameter = entry.getValue();
            int keySize = key.length();

            if (argument.length() < keySize + 1 || argument.charAt(keySize) != ':' || !argument.substring(0, keySize).equalsIgnoreCase(key)) {
                continue;
            }

            String value = argument.substring(keySize + 1);
            String primary = optionalParamAliases.get(key);

            if (parameter.getPermission() != null && !sender.hasPermission(parameter.getPermission())) {
                MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use " + primary + " for " + command + ".");
                return true;
            }

            if (!parameter.checkArgument(value)) {
                MessageUtil.sendOptionalParameterErrorMessage(sender, command, key, parameter.getErrorMessage());
                return true;
            }

            passedOptionalParameters.put(primary, parameter.asObject(value));
            return true;
        }
        return false;
    }

    // Handle positional parameters like "<target> <amount>"
    private boolean handleParameter(CommandSender sender, String argument, List<Object> passedParameters) {
        if (passedParameters.size() >= parameters.size()) {
            MessageUtil.sendLongCommandMessage(sender, command, parameters.size());
            return false;
        }

        CommandParameter param = parameters.get(passedParameters.size());
        if (!param.checkArgument(argument)) {
            MessageUtil.sendParameterErrorMessage(sender, command, passedParameters.size() + 1, param.getErrorMessage());
            return false;
        }

        passedParameters.add(param.asObject(argument));
        return true;
    }

    // Sends usage/help information for the command
    private void sendHelpMessage(CommandSender sender) {
        MessageUtil.sendCustomMessage(sender, "&6Command: /" + command);
        MessageUtil.sendCustomMessage(sender, "&7" + description);
        MessageUtil.sendCustomMessage(sender, "&eUsage: /" + command + " " + generateUsageString());

        if (!parameters.isEmpty()) {
            MessageUtil.sendCustomMessage(sender, "&6Parameters:");
            for (int i = 0; i < parameters.size(); i++) {
                String label = "<param" + (i + 1) + ">";
                String desc = parameters.get(i).getDescription();
                MessageUtil.sendCustomMessage(sender, "  &e" + label + "&7: " + desc);
            }
        }

        if (!optionalParameters.isEmpty()) {
            MessageUtil.sendCustomMessage(sender, "&6Optional Parameters:");
            for (Map.Entry<String, CommandParameter> entry : optionalParameters.entrySet()) {
                String label = entry.getKey() + ":<value>";
                String desc = entry.getValue().getDescription();
                MessageUtil.sendCustomMessage(sender, "  &e" + label + "&7: " + desc);
            }
        }

        if (!flags.isEmpty()) {
            MessageUtil.sendCustomMessage(sender, "&6Flags:");
            Set<String> used = new HashSet<>();
            for (Map.Entry<String, CommandParameter> entry : flags.entrySet()) {
                String alias = entry.getKey();
                String canonical = flagAliases.get(alias);
                if (!used.add(canonical)) continue;
                String desc = entry.getValue().getDescription();
                MessageUtil.sendCustomMessage(sender, "  &e" + alias + "&7: " + desc);
            }
        }

        for (String chunk : helpMessageChunks) {
            MessageUtil.sendCustomMessage(sender, chunk);
        }
    }

    // Generate usage string based on command structure
    private String generateUsageString() {
        StringBuilder usage = new StringBuilder();

        for (int i = 0; i < parameters.size(); i++) {
            usage.append("<param").append(i + 1).append("> ");
        }

        for (String opt : optionalParameters.keySet()) {
            usage.append("[").append(opt).append(":<value>] ");
        }

        Set<String> seen = new HashSet<>();
        for (String flag : flags.keySet()) {
            String canonical = flagAliases.get(flag);
            if (seen.add(canonical)) {
                usage.append("[").append(canonical).append("] ");
            }
        }

        return usage.toString().trim();
    }

    // Required method for command behavior — return null for no message, or a string for success/failure message
    public abstract void runCommand(CommandSender sender, List<String> flags, List<Object> parameters, Map<String, Object> optionalParameters);
}
