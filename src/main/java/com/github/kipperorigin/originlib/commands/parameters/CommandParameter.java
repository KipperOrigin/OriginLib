package com.github.kipperorigin.originlib.commands.parameters;

import java.util.Arrays;
import java.util.List;

public abstract class CommandParameter {

    private String errorMessage;
    private List<String> tabCompletionValues;
    private String permission;
    private String description = "No description provided."; // NEW

    public CommandParameter(String errorMessage, String... tabCompletes) {
        this.errorMessage = errorMessage;
        this.tabCompletionValues = Arrays.asList(tabCompletes);
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

    public CommandParameter setTabCompletionValues(String... values) {
        this.tabCompletionValues = Arrays.asList(values);
        return this;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public CommandParameter withPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CommandParameter setDescription(String description) {
        this.description = description;
        return this;
    }
}
