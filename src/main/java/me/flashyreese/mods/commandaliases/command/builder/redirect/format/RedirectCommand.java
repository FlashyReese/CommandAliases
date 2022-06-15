package me.flashyreese.mods.commandaliases.command.builder.redirect.format;

/**
 * Represents a redirection command
 * <p>
 * JSON Serialization Template
 *
 * @author FlashyReese
 * @version 0.5.0
 * @since 0.5.0
 */
public class RedirectCommand {
    private String command;
    private String redirectTo;

    public String getCommand() {
        return command;
    }

    public String getRedirectTo() {
        return redirectTo;
    }
}
