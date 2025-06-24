package com.github.kipperorigin.originlib.commands.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CommandParameter that accepts a list of values of a specified type.
 */
public class CommandParameterList extends CommandParameter {

    private final CommandParameter elementType;

    public CommandParameterList(CommandParameter elementType) {
        super("must be a list of: " + elementType.getErrorMessage());
        this.elementType = elementType;
    }

    @Override
    public boolean checkArgument(String argument) {
        String[] parts = argument.split(",");
        for (String part : parts) {
            if (!elementType.checkArgument(part.trim())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object asObject(String argument) {
        String[] parts = argument.split(",");
        List<Object> list = new ArrayList<>();
        for (String part : parts) {
            list.add(elementType.asObject(part.trim()));
        }
        return list;
    }

    @Override
    public List<String> getTabCompletes() {
        return Collections.singletonList("{list}");
    }
}