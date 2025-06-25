package com.github.kipperorigin.originlib.commands.parameters;

import java.util.Collections;
import java.util.List;

public class CommandParameterString extends CommandParameter {

    public CommandParameterString() {
        super("must be a string!");
        setDescription("<string> Value must be a string of text. To have spaces, enclose the text in quotes.");
    }

    @Override
    public boolean checkArgument(String argument) {
        return true;
    }

    @Override
    public Object asObject(String argument) {
        return argument;
    }

    @Override
    public List<String> getTabCompletes() {
        return Collections.singletonList("{string}");
    }
}
