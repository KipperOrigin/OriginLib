package com.github.kipperorigin.originlib.commands.parameters;

import org.bukkit.Bukkit;

import java.util.UUID;

public class CommandParameterEntity extends CommandParameter {

    public CommandParameterEntity() {
        super("Must be a valid Entity UUID.", "{UUID}");
    }

    @Override
    public boolean checkArgument(String argument) {
        return (asObject(argument) != null);
    }

    @Override
    public Object asObject(String argument) {
        return Bukkit.getEntity(UUID.fromString(argument));
    }
}
