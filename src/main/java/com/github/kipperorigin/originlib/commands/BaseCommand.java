package com.github.kipperorigin.originlib.commands;

import com.github.kipperorigin.originlib.commands.parameters.CommandParameter;
import com.github.kipperorigin.originlib.utils.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class BaseCommand {

    private String command;
    private String description = "No description provided.";
    private String permission;
    private String[] helpMessageChunks;
    private boolean playerRequired;

    // Updated flag structure
    private record Flag(String description, String permission) {
    }

    private final Map<String, Flag> flags = new HashMap<>();
    private final Map<String, String> flagAliases = new HashMap<>();

    private final List<CommandParameter> parameters = new ArrayList<>();
    private final Map<String, CommandParameter> optionalParameters = new HashMap<>();
    private final Map<String, String> optionalParamAliases = new HashMap<>();
    private final List<List<String>> tabCompletes = new ArrayList<>();

    public BaseCommand(String command) {
        this.command = command;
        this.helpMessageChunks = new String[]{"Default help message for " + command};
    }

    public String getCommand() {
        return command;
    }

    public List<List<String>> getTabCompletes() {
        return tabCompletes;
    }

    public void addTabCompletes(List<String> completes) {
        tabCompletes.add(completes);
    }

    public void addParameter(CommandParameter parameter) {
        parameters.add(parameter);
        if (parameter.getTabCompletes() != null) {
            tabCompletes.add(parameter.getTabCompletes());
        }
    }

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

    public void addFlag(String flagName) {
        addFlag(flagName, null, null);
    }

    public void addFlag(String flag, String description, String permission) {
        flags.put(flag, new Flag(description, permission));
        flagAliases.put(flag, flag);
    }

    public void addFlag(String[] aliases, String description, String permission) {
        for (String alias : aliases) {
            flags.put(alias, new Flag(description, permission));
            flagAliases.put(alias, aliases[0]);
        }
    }

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

        runCommand(sender, passedFlags, passedParameters, passedOptionalParameters);
    }

    private boolean handleFlag(CommandSender sender, String argument, List<String> passedFlags) {
        if (!flags.containsKey(argument)) return false;

        String primary = flagAliases.get(argument);
        Flag flag = flags.get(argument);

        if (flag.permission != null && !sender.hasPermission(flag.permission)) {
            MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use " + primary + " for " + command + ".");
            return true;
        }

        passedFlags.add(primary);
        return true;
    }

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
            Set<String> shown = new HashSet<>();
            for (String alias : flags.keySet()) {
                String canonical = flagAliases.get(alias);
                if (shown.add(canonical)) {
                    String desc = flags.get(alias).description;
                    MessageUtil.sendCustomMessage(sender, "  &e" + canonical + "&7: " + (desc != null ? desc : "No description."));
                }
            }
        }

        for (String chunk : helpMessageChunks) {
            MessageUtil.sendCustomMessage(sender, chunk);
        }
    }

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

    public abstract void runCommand(
            CommandSender sender,
            List<String> flags,
            List<Object> parameters,
            Map<String, Object> optionalParameters
    );
}
