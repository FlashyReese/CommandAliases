package me.flashyreese.mods.commandaliases.command.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents the String Formatting Type Processor
 * <p>
 * Used to convert String to a certain state.
 *
 * @author FlashyReese
 * @version 0.7.0
 * @since 0.1.2
 */
public class FormattingTypeProcessor {

    private final Map<String, Function<String, String>> formatTypeMap = new Object2ObjectOpenHashMap<>();

    public FormattingTypeProcessor() {
        registerFormatTypes();
    }

    private void registerFormatTypes() {
        this.formatTypeMap.put("jsonEscape", this::escape);
        this.formatTypeMap.put("jsonUnescape", this::unescape);
        this.formatTypeMap.put("toLower", String::toLowerCase);
        this.formatTypeMap.put("toUpper", String::toUpperCase);
        this.formatTypeMap.put("trim", String::trim);
        this.formatTypeMap.put("strip", String::strip);
        this.formatTypeMap.put("stripIndent", String::stripIndent);
        this.formatTypeMap.put("stripLeading", String::stripLeading);
        this.formatTypeMap.put("stripTrailing", String::stripTrailing);
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

    private String unescape(String raw) {
        String unescape = raw;
        unescape = unescape.replace("\\\\", "\\");
        unescape = unescape.replace("\\\"", "\"");
        unescape = unescape.replace("\\b", "\b");
        unescape = unescape.replace("\\f", "\f");
        unescape = unescape.replace("\\n", "\n");
        unescape = unescape.replace("\\r", "\r");
        unescape = unescape.replace("\\t", "\t");
        return unescape;
    }

    public Map<String, Function<String, String>> getFormatTypeMap() {
        return formatTypeMap;
    }
}
