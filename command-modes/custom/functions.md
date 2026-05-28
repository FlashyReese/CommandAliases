# Functions

Functions are processed inside command strings, messages, scheduler ids, and suggestion strings.

Function syntax:

```text
$function_name(argument)
```

Some functions do not need an argument:

```text
$executor_name()
```

## Executor

| Function | Description | Example |
| --- | --- | --- |
| `$executor_name()` | Returns the name of the command executor. | `say Hello $executor_name()` |

## Random

| Function | Description | Example |
| --- | --- | --- |
| `$random()` | Returns a random integer. | `say $random()` |
| `$random(seed)` | Returns a deterministic random integer for the seed. | `say $random(12345)` |

## Player state

These functions take a player name.

| Function | Description |
| --- | --- |
| `$is_online(player)` | Returns `true` if the player is online in the current world. |
| `$get_dimension(player)` | Returns the player's dimension registry key, such as `minecraft:overworld`. |
| `$get_skybox(player)` | Returns the skybox/effects key for the player's current dimension. |
| `$get_time()` | Returns the current world time. |
| `$get_time_of_day()` | Returns the current world time of day. |
| `$get_pos_x(player)` | Returns the player's exact X position. |
| `$get_pos_y(player)` | Returns the player's exact Y position. |
| `$get_pos_z(player)` | Returns the player's exact Z position. |
| `$get_block_pos_x(player)` | Returns the player's block X position. |
| `$get_block_pos_y(player)` | Returns the player's block Y position. |
| `$get_block_pos_z(player)` | Returns the player's block Z position. |
| `$get_yaw(player)` | Returns the player's yaw. |
| `$get_pitch(player)` | Returns the player's pitch. |

Example:

```toml
[[actions]]
command = "say $executor_name() is in $get_dimension($executor_name())"
commandType = "SERVER"
```

## Database

These functions read from the configured Command Aliases database.

| Function | Description |
| --- | --- |
| `$get_database_value(key)` | Returns the value stored at `key`, or `null` if the key does not exist. |
| `$get_database_contains(text)` | Returns `true` if any database key contains `text`. |
| `$get_database_first_starts_with(prefix)` | Returns the first value whose key starts with `prefix`, or `null`. |
| `$get_database_first_ends_with(suffix)` | Returns the first value whose key ends with `suffix`, or `null`. |
| `$get_database_first_contains(text)` | Returns the first value whose key contains `text`, or `null`. |

Example:

```toml
[[actions]]
command = "commandaliases database put $executor_name().home.main.command execute in $get_dimension($executor_name()) run tp $executor_name() $get_block_pos_x($executor_name()) $get_block_pos_y($executor_name()) $get_block_pos_z($executor_name())"
commandType = "SERVER"
messageIfSuccessful = "Home saved."
```

Then:

```toml
[[actions]]
command = "$get_database_value($executor_name().home.main.command)"
commandType = "SERVER"
```

## Notes

Function arguments are simple values such as player names, database keys, or placeholders that become simple values. Complex strings with spaces are not a good fit for function arguments.

If a function cannot find a value, it usually returns `null`. Enable `debug_mode` in the configuration while developing aliases to make missing values easier to spot.
