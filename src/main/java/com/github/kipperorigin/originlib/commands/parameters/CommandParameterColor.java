package com.github.kipperorigin.originlib.commands.parameters;

import org.bukkit.Color;

import java.lang.reflect.Field;
import java.util.Arrays;

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
        String[] values = argument.split("[ ,]+");

        // First, try matching the color name
        Color color = getColorByName(values[0].toUpperCase());

        // If a match is found, return the color immediately
        if (color != null) {
            return color;
        }

        // If no color name matched, try parsing RGB/ARGB values
        if (values.length == 1) {
            try {
                color = Color.fromRGB(Integer.parseInt(values[0]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid RGB value: " + values[0], e);
            }
        } else if (values.length == 3) {
            try {
                color = Color.fromRGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid RGB values: " + Arrays.toString(values), e);
            }
        } else if (values.length == 4) {
            try {
                color = Color.fromARGB(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid ARGB values: " + Arrays.toString(values), e);
            }
        }

        // If no valid color is found, return null or throw an exception
        return color != null ? color : null; // or you could throw an exception if no valid color is found
    }

    // Method to get a Color by its name using reflection to fetch the static fields from Color class
    private Color getColorByName(String name) {
        try {
            // Get all the fields from the Color class
            Field[] fields = Color.class.getFields();

            // Iterate over the fields to find a match for the color name
            for (Field field : fields) {
                // Check if the field is static and its name matches the input (case-insensitive)
                if (field.getType().equals(Color.class) && field.getName().equalsIgnoreCase(name)) {
                    return (Color) field.get(null); // Get the static field value
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null; // Return null if no matching color is found
    }
}