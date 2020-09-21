package me.flashyreese.mods.commandaliases;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FormattingTypeMap {

    private final Map<String, Function<String, String>> formatTypeMap = new HashMap<>();

    public FormattingTypeMap() {
        registerFormatTypes();
    }

    private void registerFormatTypes() {
        this.formatTypeMap.put("jsonString", this::escape);
        this.formatTypeMap.put("toLower", String::toLowerCase);
        this.formatTypeMap.put("toUpper", String::toUpperCase);
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
