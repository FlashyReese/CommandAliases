# Suggestion Provider Component

| Suggestion Mode        | Description                                                                                        |
| ---------------------- | -------------------------------------------------------------------------------------------------- |
| `DATABASE_STARTS_WITH` | Fetches a list of keys that starts with the suggestion, list key's value as a suggestion provider. |
| `DATABASE_CONTAINS`    | Fetches a list of keys that contains the suggestion, list key's value as a suggestion provider.    |
| `DATABASE_ENDS_WITH`   | Fetches a list of keys that ends with the suggestion, list key's value as a suggestion provider.   |
| `JSON_LIST`            | Uses suggestion as JSON string list as a suggestion provider.                                      |
| `COMMAND_LIST_LOOKUP`  | Asks the server using a command as a suggestion. Requires using the vanilla command tree.          |

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
