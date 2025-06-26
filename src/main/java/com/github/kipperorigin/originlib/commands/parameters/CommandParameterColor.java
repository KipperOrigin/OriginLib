package com.github.kipperorigin.originlib.commands.parameters;

import org.bukkit.Color;

public class CommandParameterColor extends CommandParameter {

    public CommandParameterColor() {
        super("Color error");
    }

    @Override
    public boolean checkArgument(String argument) {
        return (asObject(argument) != null);
    }

    @Override
    public Object asObject(String argument) {
        Color color = null;
        String[] values = argument.split("[ ,]+");
        if (values.length == 1) {
            try {
                color = Color.fromRGB(Integer.parseInt(values[0]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        } else if (values.length == 3) {
            try {
                color = Color.fromRGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        } else if (values.length == 4) {
            try {
                color = Color.fromARGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
        return color;
    }
}
