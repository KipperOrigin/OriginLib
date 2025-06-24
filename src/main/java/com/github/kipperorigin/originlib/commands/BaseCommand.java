package com.github.kipperorigin.originlib.commands;

import org.bukkit.command.CommandSender;
import com.github.kipperorigin.originlib.commands.parameters.CommandParameter;
import com.github.kipperorigin.originlib.utils.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand {

    private String command;
    private List<String> flags;
    private List<CommandParameter> parameters;
    private Map<String, CommandParameter> optionalParameters;
    private List<List<String>> tabCompletes;
    private String permission;
    private String[] helpMessageChunks; // Array of help message chunks
    private boolean playerRequired;

    public BaseCommand(String command) {
        flags = new ArrayList<>();
        parameters = new ArrayList<>();
        optionalParameters = new HashMap<>();
        tabCompletes = new ArrayList<>();
        permission = null;
        this.command = command;
        this.helpMessageChunks = new String[] { "Default help message for " + command }; // Default chunk
        this.playerRequired = false;

        // Add the default help flag
        addFlag("-help");
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    // Set the help message chunks (can be overridden in subclasses)
    public void setHelpMessageChunks(String[] helpMessageChunks) {
        this.helpMessageChunks = helpMessageChunks;
    }

    // Set whether the command requires a player
    public void setPlayerRequired(boolean playerRequired) {
        this.playerRequired = playerRequired;
    }

    public void execute(CommandSender sender, String[] arguments) {
        // Check if the command sender is a player if required
        if (playerRequired && !(sender instanceof org.bukkit.entity.Player)) {
            MessageUtil.sendCustomMessage(sender, "&cThis command can only be used by a player!");
            return;
        }

        // If the "-help" flag is present, show the help message and cancel the command
        if (flags.contains("-help")) {
            sendHelpMessage(sender);
            return;
        }

        if (permission != null && !sender.hasPermission(permission)) {
            MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use the \"" + command + "\"&c command!");
            return;
        }

        List<String> passedFlags = new ArrayList<>();
        List<Object> passedParameters = new ArrayList<>();
        Map<String, Object> passedOptionalParameters = new HashMap<>();

        for (String argument : arguments) {
            // Handle flags
            if (flags.contains(argument)) {
                passedFlags.add(argument);
            } else {
                CommandParameter parameter;
                boolean next = false;

                // Handle optional parameters
                for (Map.Entry<String, CommandParameter> entry : optionalParameters.entrySet()) {
                    int keySize = entry.getKey().length();
                    if (argument.length() >= keySize) {
                        if (entry.getKey().equalsIgnoreCase(argument.substring(0, keySize))) {
                            String shortArgument = argument.substring(keySize + 1);
                            parameter = entry.getValue();
                            if (parameter.checkArgument(shortArgument)) {
                                passedOptionalParameters.put(argument.substring(0, keySize).toLowerCase(), parameter.asObject(shortArgument));
                                next = true;
                                break;
                            } else {
                                MessageUtil.sendOptionalParameterErrorMessage(sender, command, entry.getKey(), parameter.getErrorMessage());
                                return;
                            }
                        }
                    }
                }
                if (next) continue;

                // Check if the command has enough parameters
                if (parameters.size() == passedParameters.size()) {
                    MessageUtil.sendLongCommandMessage(sender, command, parameters.size());
                    return;
                }

                // Handle regular parameters
                parameter = parameters.get(passedParameters.size());
                if (parameter.checkArgument(argument)) {
                    passedParameters.add(parameter.asObject(argument));
                } else {
                    MessageUtil.sendParameterErrorMessage(sender, command, passedParameters.size() + 1, parameter.getErrorMessage());
                    return;
                }
            }
        }

        // Check if there are enough parameters
        if (passedParameters.size() < parameters.size()) {
            MessageUtil.sendShortCommandMessage(sender, command, parameters.size());
            return;
        }

        // Execute the command
        runCommand(sender, passedFlags, passedParameters, passedOptionalParameters);
    }

    private void sendHelpMessage(CommandSender sender) {
        // Send each chunk in the help message array
        for (String chunk : helpMessageChunks) {
            MessageUtil.sendCustomMessage(sender, chunk);
        }
    }

    public void addParameter(CommandParameter parameter) {
        parameters.add(parameter);
        if (parameter.getTabCompletes() != null) addTabCompletes(parameter.getTabCompletes());
    }

    public void addOptionalParameter(String prefix, CommandParameter parameter) {
        optionalParameters.put(prefix, parameter);
    }

    public void addTabCompletes(List<String> tabCompletes) {
        this.tabCompletes.add(tabCompletes);
    }

    public List<List<String>> getTabCompletes() {
        return tabCompletes;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public abstract void runCommand(CommandSender sender, List<String> flags, List<Object> parameters, Map<String, Object> optionalParameters);
}
