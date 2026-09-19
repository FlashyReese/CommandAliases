# Suggestion Provider Component

| Suggestion Mode        | Description                                                                                        |
| ---------------------- | -------------------------------------------------------------------------------------------------- |
| `DATABASE_STARTS_WITH` | Fetches a list of keys that starts with the suggestion, list key's value as a suggestion provider. |
| `DATABASE_CONTAINS`    | Fetches a list of keys that contains the suggestion, list key's value as a suggestion provider.    |
| `DATABASE_ENDS_WITH`   | Fetches a list of keys that ends with the suggestion, list key's value as a suggestion provider.   |
| `JSON_LIST`            | Uses suggestion as JSON string list as a suggestion provider.                                      |
| `COMMAND_LIST_LOOKUP`  | Uses an existing command tree's suggestion provider. Requires using the vanilla command tree.     |

## `COMMAND_LIST_LOOKUP`

`COMMAND_LIST_LOOKUP` copies the suggestion provider from an existing argument in the command tree.
The `suggestion` value is a node path, not a command to execute and not an example value.

For a command shaped like this:

```text
/essentialcommands:home tp <home>
```

Use the names of the command nodes, including the target argument node:

```json
{
    "suggestionMode": "COMMAND_LIST_LOOKUP",
    "suggestion": "essentialcommands:home tp home"
}
```

The final `home` must be replaced with the actual argument node name exposed by the target command.
A path that ends at `essentialcommands:home tp` stops at a literal node, so it does not identify a
suggestion provider and will be rejected.

The target command must already be registered on the same command dispatcher when aliases are loaded.

* JSON or JSON5

```json5
{
    "suggestionMode": "DATABASE_STARTS_WITH", // Required | Suggestion Mode
    "suggestion": "$executor_name().home.suggestions" // Required | Suggestion
}
```

* TOML

```toml
suggestionMode = "DATABASE_STARTS_WITH" # Required | Suggestion Mode
suggestion = "$executor_name().home.suggestions" # Required | Suggestion
```

* YAML

```yaml
suggestionMode: DATABASE_STARTS_WITH # Required | Suggestion Mode
suggestion: '$executor_name().home.suggestions' # Required | Suggestion
```
