package com.github.kipperorigin.originlib.commands;

import org.bukkit.command.CommandSender;
import com.github.kipperorigin.originlib.commands.parameters.CommandParameter;
import com.github.kipperorigin.originlib.utils.MessageUtil;

import java.util.*;

public abstract class BaseCommand {

    private String command;
    private final List<String> flags;
    private final List<CommandParameter> parameters;
    private final Map<String, CommandParameter> optionalParameters; // primary -> parameter
    private final Map<String, String> aliasToPrimary;               // alias -> primary (optional params)
    private final List<List<String>> tabCompletes;
    private String permission;
    private String[] helpMessageChunks;
    private boolean playerRequired;

    // Flag alias support
    private final Set<String> allFlagAliases;
    private final Map<String, String> flagAliasToPrimary;

    public BaseCommand(String command) {
        this.command = command;
        this.flags = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.optionalParameters = new HashMap<>();
        this.aliasToPrimary = new HashMap<>();
        this.tabCompletes = new ArrayList<>();
        this.permission = null;
        this.helpMessageChunks = new String[]{"Default help message for " + command};
        this.playerRequired = false;

        this.allFlagAliases = new HashSet<>();
        this.flagAliasToPrimary = new HashMap<>();

        addFlag("-help");
    }

    // ===== Flag Handling =====
    public void addFlag(String flag) {
        String key = flag.toLowerCase();
        flags.add(key);
        allFlagAliases.add(key);
        flagAliasToPrimary.put(key, key);
    }

    public void addFlag(String primary, List<String> aliases) {
        addFlag(primary); // add primary
        for (String alias : aliases) {
            String aliasKey = alias.toLowerCase();
            allFlagAliases.add(aliasKey);
            flagAliasToPrimary.put(aliasKey, primary.toLowerCase());
        }
    }

    // ===== Optional Parameter Handling =====
    public void addOptionalParameter(String prefix, CommandParameter parameter) {
        String key = prefix.toLowerCase();
        optionalParameters.put(key, parameter);
        aliasToPrimary.put(key, key);
    }

    public void addOptionalParameter(String primary, List<String> aliases, CommandParameter parameter) {
        String primaryLower = primary.toLowerCase();
        optionalParameters.put(primaryLower, parameter);
        aliasToPrimary.put(primaryLower, primaryLower);
        for (String alias : aliases) {
            aliasToPrimary.put(alias.toLowerCase(), primaryLower);
        }
    }

    public void addOptionalParameter(String[] aliases, CommandParameter parameter) {
        if (aliases.length == 0) return;
        String primary = aliases[0];
        List<String> aliasList = Arrays.asList(aliases).subList(1, aliases.length);
        addOptionalParameter(primary, aliasList, parameter);
    }

    // ===== Parameter Handling =====
    public void addParameter(CommandParameter parameter) {
        parameters.add(parameter);
        if (parameter.getTabCompletes() != null) {
            addTabCompletes(parameter.getTabCompletes());
        }
    }

    public void addTabCompletes(List<String> tabCompletes) {
        this.tabCompletes.add(tabCompletes);
    }

    public void setHelpMessageChunks(String[] helpMessageChunks) {
        this.helpMessageChunks = helpMessageChunks;
    }

    public void setPlayerRequired(boolean playerRequired) {
        this.playerRequired = playerRequired;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<List<String>> getTabCompletes() {
        return tabCompletes;
    }

    // ===== Command Execution =====
    public void execute(CommandSender sender, String[] arguments) {
        if (playerRequired && !(sender instanceof org.bukkit.entity.Player)) {
            MessageUtil.sendCustomMessage(sender, "&cThis command can only be used by a player!");
            return;
        }

        List<String> passedFlags = new ArrayList<>();
        List<Object> passedParameters = new ArrayList<>();
        Map<String, Object> passedOptionalParameters = new HashMap<>();

        for (String argument : arguments) {
            String argLower = argument.toLowerCase();

            // Handle flags with alias mapping
            if (flagAliasToPrimary.containsKey(argLower)) {
                passedFlags.add(flagAliasToPrimary.get(argLower));
                continue;
            }

            // Handle optional parameters with alias mapping
            boolean matchedOptional = false;
            for (String alias : aliasToPrimary.keySet()) {
                if (argLower.startsWith(alias.toLowerCase() + ":")) {
                    String value = argument.substring(alias.length() + 1);
                    String primary = aliasToPrimary.get(alias.toLowerCase());
                    CommandParameter param = optionalParameters.get(primary);

                    if (param.checkArgument(value)) {
                        passedOptionalParameters.put(primary, param.asObject(value));
                        matchedOptional = true;
                    } else {
                        MessageUtil.sendOptionalParameterErrorMessage(sender, command, alias, param.getErrorMessage());
                        return;
                    }
                    break;
                }
            }

            if (matchedOptional) continue;

            // Handle regular parameters
            if (passedParameters.size() >= parameters.size()) {
                MessageUtil.sendLongCommandMessage(sender, command, parameters.size());
                return;
            }

            CommandParameter parameter = parameters.get(passedParameters.size());
            if (parameter.checkArgument(argument)) {
                passedParameters.add(parameter.asObject(argument));
            } else {
                MessageUtil.sendParameterErrorMessage(sender, command, passedParameters.size() + 1, parameter.getErrorMessage());
                return;
            }
        }

        if (passedParameters.size() < parameters.size()) {
            MessageUtil.sendShortCommandMessage(sender, command, parameters.size());
            return;
        }

        runCommand(sender, passedFlags, passedParameters, passedOptionalParameters);
    }

    private void sendHelpMessage(CommandSender sender) {
        for (String chunk : helpMessageChunks) {
            MessageUtil.sendCustomMessage(sender, chunk);
        }
    }

    public abstract void runCommand(
            CommandSender sender,
            List<String> flags,
            List<Object> parameters,
            Map<String, Object> optionalParameters
    );
}
