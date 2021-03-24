/*
 * Copyright © 2020 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.command.builder.alias;


import me.flashyreese.mods.commandaliases.CommandAliasesMod;

import java.util.regex.Matcher;

/**
 * Represents the Alias Holder for Alias Builder
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public class AliasHolder {
    private final String holder;

    private String classTool;
    private String method;
    private String variableName;
    private String formattingType;

    private final boolean required;

    public AliasHolder(String holder, boolean required) {
        this.holder = holder;
        this.required = required;
        this.locateVariables();
    }

    private void locateVariables() {
        Matcher matcher = this.required ? AliasCommandBuilder.REQUIRED_COMMAND_ALIAS_HOLDER.matcher(this.holder) : AliasCommandBuilder.OPTIONAL_COMMAND_ALIAS_HOLDER.matcher(this.holder);
        if (matcher.matches()) {
            String classTool = matcher.group("classTool");
            String method = matcher.group("method");
            String variableName = matcher.group("variableName");
            String formattingType = matcher.group("formattingType");

            this.updateVariables(classTool, method, variableName, formattingType);
        } else {
            CommandAliasesMod.getLogger().error("Invalid Command Aliases Holder: {}", this.holder);
        }
    }

    private void updateVariables(String classTool, String method, String variableName, String formattingType) {
        String cT = classTool;
        String vN = variableName;

        if (method == null && vN == null) {
            vN = cT;
            cT = null;
        }

        this.classTool = cT;
        this.method = method;
        this.variableName = vN;
        this.formattingType = formattingType;
    }

    public String toString() {
        return this.holder;
    }

    public String getHolder() {
        return this.holder;
    }

    public String getClassTool() {
        return this.classTool;
    }

    public String getMethod() {
        return this.method;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public String getFormattingType() {
        return this.formattingType;
    }
}
