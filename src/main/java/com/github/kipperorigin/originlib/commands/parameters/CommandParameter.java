package com.github.kipperorigin.originlib.commands.parameters;

import java.util.Arrays;
import java.util.List;

public abstract class CommandParameter {

    private String errorMessage;    
    private List<String> tabCompletionValues;

    public CommandParameter(String errorMessage,String... tabCompletes) {
        this.errorMessage = errorMessage;
        tabCompletionValues = Arrays.asList(tabCompletes);
    }

    public abstract boolean checkArgument(String argument);
    public abstract Object asObject(String argument);

    public void setErrorMessage(String message) {
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getTabCompletes() {
        return tabCompletionValues;
    }

    public CommandParameter setTabCompletionValues(String... value) {
        tabCompletionValues = Arrays.asList(value);
        return this;
    }
}
