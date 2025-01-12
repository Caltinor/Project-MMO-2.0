# Perks Config Syntax and Examples

## Example File
```
WITH config(perks)
    set(clear_all);  //removes all perks from defaults before we start adding our own.  not required.
    set().event(SKILL_UP)
        .perk(pmmo:fireworks)
        .skill(mining);
    set().event(DEAL_DAMAGE)
        .perk(pmmo:damage_boost)
        .skill(archery)
        .applies_to(["minecraft:bow","minecraft:crossbow","minecraft:trident"]);
        //notice for lists, you must write is as a standard JSON array
END

//Another approach for less typing
WITH config(perks).event(SKILL_UP).perk(pmmo:fireworks);
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

**Note that each perk must have the `perk(perkID)` node to be valid, but omitting it will simply cause that configuration to be skipped.**
