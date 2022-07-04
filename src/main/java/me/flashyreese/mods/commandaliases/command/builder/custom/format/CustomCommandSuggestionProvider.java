package me.flashyreese.mods.commandaliases.command.builder.custom.format;

/**
 * Represents a custom command suggestion provider
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.8.0
 */
public class CustomCommandSuggestionProvider {
    public CustomCommandSuggestionMode suggestionMode;
    public String suggestion;

    public CustomCommandSuggestionMode getSuggestionMode() {
        return suggestionMode;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
