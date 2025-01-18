[Back](./scripting.md#server-config-syntax-and-examples)

# Globals Config Syntax and Examples

## Example File
```
WITH config(globals)
    set(paths).key(tmat0).value(tic_materials[0]);
    set(constants).key(iron).value(shield/iron);
END
```

## Syntax
Each configuration uses a combination of `set(config_keyword).value(some_value);`.  The table below lists each configuration node's keyword, purpose and the value node format.

| set node key | setting function                         | special nodes                                                      | value node                             |
|:------------:|:-----------------------------------------|:-------------------------------------------------------------------|:---------------------------------------|
|   `paths`    | defines a path with the key provided     | `key(string)` sets the subsequent value to the string key provided | `value(path string)` an NBT path value |
| `constants`  | defines a constant with the key provided | `key(string)` sets the subsequent value to the string key provided | `value(path string)` an NBT path value |

[Back](./scripting.md#server-config-syntax-and-examples)