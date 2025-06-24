    package com.github.kipperorigin.originlib.commands;

    import org.bukkit.Bukkit;
    import org.bukkit.command.CommandSender;
    import com.github.kipperorigin.originlib.commands.parameters.CommandParameter;
    import com.github.kipperorigin.originlib.utils.MessageUtil;
    import org.bukkit.event.entity.EntityDamageByEntityEvent;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public abstract class BaseCommand {

        private String command;
        private List<String> flags;
        private List<CommandParameter> parameters;
        private Map<String,CommandParameter> optionalParameters;
        private List<List<String>> tabCompletes;
        private String permission;

        public BaseCommand(String command) {
            flags = new ArrayList<>();
            parameters = new ArrayList<>();
            optionalParameters = new HashMap<>();
            tabCompletes = new ArrayList<>();
            permission = null;
            this.command = command;
        }

        public void addFlag(String flag) {
            flags.add(flag);
        }

        public void execute(CommandSender sender, String[] arguments) {
            if (permission != null && !sender.hasPermission(permission)) {
                MessageUtil.sendCustomMessage(sender, "&cYou do not have permission to use the e\"" + command + "\"&c command!");
                return;
            }
            List<String> passedFlags = new ArrayList<>();
            List<Object> passedParameters = new ArrayList<>();
            Map<String, Object> passedOptionalParameters = new HashMap<>();
            for (String argument:arguments) {
                //Checks if the argument is a flag
                if (flags.contains(argument)) {
                    passedFlags.add(argument);
                } else {

                    CommandParameter parameter;
                    boolean next = false;

                    //Checks if the argument is an optional parameter, if it is an optional parameter it breaks and skips to the next iteration of the arguments loop
                    for (Map.Entry<String, CommandParameter> entry : optionalParameters.entrySet()) {
                        int keySize = entry.getKey().length();
                        if (argument.length() >= keySize) {
                            if (entry.getKey().equalsIgnoreCase(argument.substring(0,keySize))) {
                                String shortArgument = argument.substring(keySize+1);
                                parameter = entry.getValue();
                                if (parameter.checkArgument(shortArgument)) {
                                    passedOptionalParameters.put(argument.substring(0,keySize).toLowerCase(),parameter.asObject(shortArgument));
                                    next = true;
                                    break;
                                } else {
                                    MessageUtil.sendOptionalParameterErrorMessage(sender,command,entry.getKey(), parameter.getErrorMessage());
                                    return;
                                }
                            }
                        }
                    }
                    if (next) continue;

                    //Check if there are already enough parameters, if there is send an error and cancel the command;
                    if (parameters.size() == passedParameters.size()) {
                        MessageUtil.sendLongCommandMessage(sender, command, parameters.size());
                        return;
                    }

                    //Checks if the command is an parameter, if it is not sends and error and cancels the command;
                    parameter = parameters.get(passedParameters.size());
                    if (parameter.checkArgument(argument)) {
                        passedParameters.add(parameter.asObject(argument));
                    } else {
                        MessageUtil.sendParameterErrorMessage(sender,command,passedParameters.size() + 1,parameter.getErrorMessage());
                        return;
                    }
                }
            }

            //Checks if there are not enough parameters, if there is send an error anc cancel the command;
            if (passedParameters.size() < parameters.size()) {
                MessageUtil.sendShortCommandMessage(sender,command,parameters.size());
                return;
            }

            //Runs the command from the child class
            runCommand(sender,passedFlags,passedParameters,passedOptionalParameters);
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
