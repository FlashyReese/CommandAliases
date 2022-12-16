# Functions

As of `0.7.0`

* `$executor_name()`
  * Returns the player name of the command executor.
* `$get_database_value(key)`
  * Returns the value of `key` from the database if it exists. Example: `commandaliases database put hello.key world.value` then `say $get_database_value(hello.key)` and it will say in chat `world.value`
* `$get_dimension(player)`
  * Returns the dimension registry key of the player in the argument. Example: `say $get_dimension(jeb_)` and it will say in chat `minecraft:overworld`
* `$get_pos_x(player)`
  * Returns player position x in the argument. Example: `say $get_pos_x(jeb_)` and it will say in chat `10.25`
* `$get_pos_y(player)`
  * Returns player position y in the argument. Example: `say $get_pos_y(jeb_)` and it will say in chat `64.5`
* `$get_pos_z(player)`
  * Returns player position z in the argument. Example: `say $get_pos_z(jeb_)` and it will say in chat `-10.25`
* `$get_yaw(player)`
  * Returns player yaw in the argument. Example: `say $get_yaw(jeb_)` and it will say in chat `0`
* `$get_pitch(player)`
  * Returns player pitch in the argument. Example: `say $get_pitch(jeb_)` and it will say in chat `90`
* `$get_block_pos_x(player)`
  * Returns player block position x in the argument. Example: `say $get_block_pos_x(jeb_)` and it will say in chat `10`
* `$get_block_pos_y(player)`
  * Returns player block position y in the argument. Example: `say $get_block_pos_y(jeb_)` and it will say in chat `64`
* `$get_block_pos_z(player)`
  * Returns player block position z in the argument. Example: `say $get_block_pos_z(jeb_)` and it will say in chat `-10`
