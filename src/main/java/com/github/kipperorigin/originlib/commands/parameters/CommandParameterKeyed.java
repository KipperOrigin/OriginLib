package com.github.kipperorigin.originlib.commands.parameters;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A command parameter that resolves and tab-completes based on static {@link Keyed} fields
 * within a specified class. This is useful for supporting registries like {@code DamageTypeTags}
 * or {@code PotionEffectType} where constants implement the {@link Keyed} interface.
 *
 * <p>Example usage:
 * <pre>
 *     new CommandParameterKeyed(DamageTypeTags.class);
 * </pre>
 */
public class CommandParameterKeyed extends CommandParameter {
    private final Class<?> holderClass;

    /**
     * Constructs a new parameter that looks up {@link Keyed} objects from the given class.
     *
     * @param holderClass The class containing static fields that implement {@link Keyed}
     */
    public CommandParameterKeyed(Class<?> holderClass) {
        super(holderClass.getSimpleName().toLowerCase());
        this.holderClass = holderClass;
    }

    /**
     * Checks whether the input string corresponds to a valid {@link Keyed} object's key.
     *
     * @param argument The input argument to validate
     * @return true if a matching {@link Keyed} object was found, false otherwise
     */
    @Override
    public boolean checkArgument(String argument) {
        return getKeyedObject(argument) != null;
    }

    /**
     * Converts the input string into the matching {@link Keyed} object, or null if none found.
     *
     * @param argument The input argument
     * @return The matched {@link Keyed} object or null
     */
    @Override
    public Object asObject(String argument) {
        return getKeyedObject(argument);
    }

    /**
     * Returns a list of key names from all static {@link Keyed} fields in the class.
     *
     * @return A list of string completions (the key names)
     */
    @Override
    public List<String> getTabCompletes() {
        List<String> tabCompletes = new ArrayList<>();
        for (Field field : holderClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Keyed.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Keyed keyedObject = (Keyed) field.get(null);
                    tabCompletes.add(keyedObject.getKey().getKey());
                } catch (IllegalAccessException e) {
                    // Failed to access static field; likely a permission issue or security manager
                    // If this happens during dev, check field visibility and modifiers
                }
            }
        }
        return tabCompletes;
    }

    /**
     * Finds a {@link Keyed} object within the static fields of the class that matches the given key.
     *
     * @param key The key string to match (case-insensitive)
     * @return The matching {@link Keyed} object, or null if no match found
     */
    private Keyed getKeyedObject(String key) {
        for (Field field : holderClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Keyed.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Keyed keyedObject = (Keyed) field.get(null);
                    if (keyedObject.getKey().getKey().equalsIgnoreCase(key)) {
                        return keyedObject;
                    }
                } catch (IllegalAccessException e) {
                    // Unable to access the static field; ensure it's public or adjust accessibility
                }
            }
        }
        return null;
    }
}
