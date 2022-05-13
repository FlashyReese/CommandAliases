/*
 * Copyright © 2020-2021 FlashyReese
 *
 * This file is part of CommandAliases.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.mods.commandaliases.classtool;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents the String Formatting Type
 * <p>
 * Used to convert String to a certain state.
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.1.2
 */
public class FormattingTypeMap {

    private final Map<String, Function<String, String>> formatTypeMap = new HashMap<>();

    public FormattingTypeMap() {
        registerFormatTypes();
    }

    private void registerFormatTypes() {
        this.formatTypeMap.put("jsonString", this::escape);
        this.formatTypeMap.put("toLower", String::toLowerCase);
        this.formatTypeMap.put("toUpper", String::toUpperCase);
        this.formatTypeMap.put("removeDoubleQuotes", this::removeDoubleQuotes);
    }

    private String removeDoubleQuotes(String raw) {
        String removed = raw;
        removed = removed.replaceAll("\"", "");
        return removed;
    }

    private String escape(String raw) {
        String escaped = raw;
        escaped = escaped.replace("\\", "\\\\");
        escaped = escaped.replace("\"", "\\\"");
        escaped = escaped.replace("\b", "\\b");
        escaped = escaped.replace("\f", "\\f");
        escaped = escaped.replace("\n", "\\n");
        escaped = escaped.replace("\r", "\\r");
        escaped = escaped.replace("\t", "\\t");
        return escaped;
    }

    public Map<String, Function<String, String>> getFormatTypeMap() {
        return formatTypeMap;
    }
}
