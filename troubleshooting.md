# Troubleshooting

This page lists common problems when writing or loading command aliases.

## My file does not load

Run:

```text
/commandaliases reload
```

Then check the server log. Command Aliases prints the alias directory tree and marks each file with a load status.

Common causes:

- The file extension is not supported.
- The file has invalid JSON, JSON5, TOML, or YAML syntax.
- `schemaVersion` is missing or unsupported.
- `commandMode` is missing or misspelled.
- The fields do not match the selected command mode.

Supported extensions:

| Format | Extensions |
| --- | --- |
| JSON | `.json` |
| JSON5 | `.json5` |
| TOML | `.toml` |
| YAML | `.yaml`, `.yml` |

## My command does not appear

Check these first:

- The file loaded successfully.
- You ran `/commandaliases reload` after editing the file.
- The command name does not conflict with an existing top-level command.
- The command path uses valid `literal` and `argument` child nodes.
- Your player has permission to see or run the command.

For redirects, `redirectTo` must point to a command path that already exists when aliases are registered.

## My placeholder is not replaced

Input placeholders use double braces:

```text
{{name}}
```

The placeholder name must match a command argument from the current command path.

Example:

```toml
[[children]]
child = "name"
type = "argument"
argumentType = "minecraft:word"
message = "Selected {{name}}"
```

Function calls use `$function_name(argument)`:

```text
$executor_name()
$get_database_value(player.home.main.command)
```

## My action does not stop after failure

Set `requireSuccess = true` on the action that must succeed.

```toml
[[actions]]
command = "commandaliases database match $executor_name().home.{{name}}.command"
commandType = "SERVER"
requireSuccess = true
messageIfUnsuccessful = "That home does not exist."

[[actions]]
command = "$get_database_value($executor_name().home.{{name}}.command)"
commandType = "SERVER"
```

Without `requireSuccess`, later actions continue even if the current command fails.

## My delayed or nested action runs out of order

The current action scheduler can queue delayed actions before their result is known. This is most visible with nested `actionsIfSuccessful` and `actionsIfUnsuccessful`, or with database writes followed by delayed reads.

Workarounds:

- Keep delayed actions simple.
- Avoid depending on delayed action success to control immediate parent actions.
- Use clear scheduler ids and remove pending events when the action is no longer needed.

## My database value is missing

Check the key with:

```text
/commandaliases database get <key>
```

If the value is missing:

- Confirm the write action ran.
- Confirm the key uses the same player name and feature prefix everywhere.
- Confirm the database mode is persistent if you expect values to survive restarts.

`IN_MEMORY` loses all data when the game or server closes. `LEVELDB`, `MYSQL`, and `REDIS` are persistent.

## Debug settings

The config file is:

```text
.minecraft/config/command-aliases-config.json
```

Useful debug options:

```json
{
  "debug_settings": {
    "debug_mode": true,
    "show_processing_time": true,
    "broadcast_to_ops": false
  }
}
```

Use `debug_mode` while developing aliases, then turn it off after the alias works.
