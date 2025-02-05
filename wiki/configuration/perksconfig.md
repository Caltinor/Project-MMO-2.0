[Back](./scripting.md#server-config-syntax-and-examples)

# Perks Config Syntax and Examples

## Example File
```
WITH config(perks)
    set(clear_all);  //removes all perks from defaults before we start adding our own.  not required.
    set().event(SKILL_UP)
        .perk("pmmo:fireworks")
        .skill(mining);
    set().event(DEAL_DAMAGE)
        .perk("pmmo:damage_boost")
        .skill(archery)
        .applies_to(["minecraft:bow","minecraft:crossbow","minecraft:trident"]);
        //notice for lists, you must write is as a standard JSON array
END

//Another approach for less typing
WITH config(perks).event(SKILL_UP).perk("pmmo:fireworks");
    set().skill(archery);
    set().skill(mining);
    set().skill(dragon_slaying);
END
```

## Syntax
Each configuration uses a combination of `set(config_keyword).value(some_value);`.  The table below lists each configuration node's keyword, purpose and the value node format.

| set node key | setting function                         | special nodes                                                      | value node                             |
|:------------:|:-----------------------------------------|:-------------------------------------------------------------------|:---------------------------------------|
| `clear_all`|clears the perks config of all perks|||
| blank|start each perk with `set()`|`event(EVENT_TYPE)` specifies which event this perk is for.|`property(value)` perks take multiple values chained together where the attribute you are setting is the value node and the value inside the `()` is the value|

[Built-in Perk Properties](../core/perks.md)

### IMPORTANT NOTES
- each perk must have the `perk(perkID)` node to be valid, but omitting it will simply cause that configuration to be skipped.
- values with `:` included such as perk IDs must be enclosed in quotes eg `perk("pmmo:effect")` or else the parser will omit everything after the `:`.  This only applies to perk settings in scripting since perks use a dynamic properties system that has to be parsed differently.

[Back](./scripting.md#server-config-syntax-and-examples)
