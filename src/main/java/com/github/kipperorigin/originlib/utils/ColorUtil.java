package com.github.kipperorigin.originlib.utils;

import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {

    private static final Map<String, Color> predefinedColors = new HashMap<>();
    private static final Map<String, String> colorCodes = new HashMap<>();

    static {
        predefinedColors.put("BLACK", Color.BLACK);
        predefinedColors.put("DARK_BLUE", Color.NAVY);
        predefinedColors.put("DARK_GREEN", Color.GREEN);
        predefinedColors.put("DARK_AQUA", Color.TEAL);
        predefinedColors.put("DARK_RED", Color.MAROON);
        predefinedColors.put("DARK_PURPLE", Color.PURPLE);
        predefinedColors.put("GOLD", Color.ORANGE);
        predefinedColors.put("GRAY", Color.GRAY);
        predefinedColors.put("DARK_GRAY", Color.SILVER);
        predefinedColors.put("BLUE", Color.BLUE);
        predefinedColors.put("GREEN", Color.LIME);
        predefinedColors.put("AQUA", Color.AQUA);
        predefinedColors.put("RED", Color.RED);
        predefinedColors.put("LIGHT_PURPLE", Color.FUCHSIA);
        predefinedColors.put("YELLOW", Color.YELLOW);
        predefinedColors.put("WHITE", Color.WHITE);

        colorCodes.put("BLACK", "&0");
        colorCodes.put("DARK_BLUE", "&1");
        colorCodes.put("DARK_GREEN", "&2");
        colorCodes.put("DARK_AQUA", "&3");
        colorCodes.put("DARK_RED", "&4");
        colorCodes.put("DARK_PURPLE", "&5");
        colorCodes.put("GOLD", "&6");
        colorCodes.put("GRAY", "&7");
        colorCodes.put("DARK_GRAY", "&8");
        colorCodes.put("BLUE", "&9");
        colorCodes.put("GREEN", "&a");
        colorCodes.put("AQUA", "&b");
        colorCodes.put("RED", "&c");
        colorCodes.put("LIGHT_PURPLE", "&d");
        colorCodes.put("YELLOW", "&e");
        colorCodes.put("WHITE", "&f");
    }

    public static Color getClosestColor(Color input) {
        Color closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Color c : predefinedColors.values()) {
            double distance = getColorDistance(input, c);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = c;
            }
        }

        return closest;
    }

    public static String getClosestColorName(Color input) {
        String closestName = null;
        double closestDistance = Double.MAX_VALUE;

        for (Map.Entry<String, Color> entry : predefinedColors.entrySet()) {
            double distance = getColorDistance(input, entry.getValue());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestName = entry.getKey();
            }
        }

        return closestName;
    }

    public static String getClosestColorCode(Color input) {
        String name = getClosestColorName(input);
        return colorCodes.getOrDefault(name, "&f"); // default to white
    }

    private static double getColorDistance(Color c1, Color c2) {
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();

        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();

        return Math.sqrt(
                Math.pow(r1 - r2, 2) +
                        Math.pow(g1 - g2, 2) +
                        Math.pow(b1 - b2, 2)
        );
    }
}

