package me.flashyreese.mods.commandaliases.command.impl;

import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Input Mapper and Formatting Type Processor
 * <p>
 * Maps the user input to the placeholder also formats them if the formatting type exists.
 *
 * @author FlashyReese
 * @version 0.8.0
 * @since 0.8.0
 */
public class InputMapper<S> {

    protected final FormattingTypeProcessor formattingTypeMap = new FormattingTypeProcessor();

    private final Pattern placeholderWithFormattingPattern = Pattern.compile("\\{\\{(?<placeholder>\\w+)?(@(?<formattingType>\\w+))?}}");

    public String formatAndMapInputs(String original, CommandContext<S> context, List<String> inputList, ArgumentTypeMapper argumentTypeMapper) {
        String modified = original;
        Matcher matcher = this.placeholderWithFormattingPattern.matcher(modified);
        while (matcher.find()) {
            String placeholder = matcher.group("placeholder");

            if (inputList.contains(placeholder)) {
                String formattingType = matcher.group("formattingType");

                String value = argumentTypeMapper.getInputString(context, placeholder); // Todo: Null check?

                if (formattingType != null) {
                    if (this.formattingTypeMap.getFormatTypeMap().containsKey(formattingType)) {
                        value = this.formattingTypeMap.getFormatTypeMap().get(formattingType).apply(value);
                    } else {
                        CommandAliasesMod.logger().error("Invalid formatting type of `{}` in `{}`", formattingType, original);
                        break;
                    }
                }

                modified = modified.replace(matcher.group(), value);
            } else {
                CommandAliasesMod.logger().error("Invalid placeholder of `{}` in `{}`", placeholder, original);
                break;
            }
            matcher = this.placeholderWithFormattingPattern.matcher(modified);
        }
        return modified;
    }
}
