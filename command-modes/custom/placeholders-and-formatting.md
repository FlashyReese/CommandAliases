# Placeholders and Formatting

Placeholders insert command arguments into messages, commands, scheduler ids, and suggestion strings.

## Basic placeholders

Placeholders use double braces:

```text
{{name}}
```

The placeholder name must match an argument from the current command path.

```toml
schemaVersion = 1
commandMode = "COMMAND_CUSTOM"
command = "hello"

[[children]]
child = "name"
type = "argument"
argumentType = "minecraft:word"
message = "Hello, {{name}}!"
```

Running:

```text
/hello Steve
```

Sends:

```text
Hello, Steve!
```

## Formatting placeholders

Add `@formatName` inside the placeholder:

```text
{{name@toLower}}
```

Example:

```toml
[[children]]
child = "name"
type = "argument"
argumentType = "minecraft:word"
message = "Saved as {{name@toLower}}."
```

## Formatting types

| Format | Description |
| --- | --- |
| `jsonEscape` | Escapes the value for use inside JSON text. |
| `jsonUnescape` | Unescapes JSON escape sequences. |
| `toLower` | Converts the value to lowercase. |
| `toUpper` | Converts the value to uppercase. |
| `trim` | Removes leading and trailing whitespace. |
| `strip` | Removes leading and trailing whitespace using Java's Unicode-aware strip behavior. |
| `stripIndent` | Removes incidental indentation from a multiline value. |
| `stripLeading` | Removes leading whitespace. |
| `stripTrailing` | Removes trailing whitespace. |
| `removeDoubleQuotes` | Removes all double quote characters. |

## JSON text example

Use `jsonEscape` when inserting user input into a JSON command string.

```toml
[[children]]
child = "message"
type = "argument"
argumentType = "minecraft:greedy_string"

[[children.actions]]
command = 'tellraw @a {"text":"{{message@jsonEscape}}"}'
commandType = "SERVER"
```

Without escaping, quotes or backslashes in the user input can break the JSON command.

## Notes

Formatting is applied before functions are processed. If a placeholder is invalid, the original text is left unchanged and an error is logged.
