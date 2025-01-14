# Anti-Cheese Config Syntax and Examples

## Example File
```
WITH config(anticheese)
    set(afk_can_subtract).value(false);
    set(afk).event(SWIMMING)
        .reduction(0.1)
        .cooloff_amount(1)
        .min_time_to_apply(200);
    set(diminishing_xp).event(RIDING)
        .reduction(0.005)
        .source(minecraft:horse,minecraft:boat)
        .retention_duration(200);
    set(normalization).event(SPRINTING)
        .tolerance_percent(0.1)
        .retention_duration(400)
        .tolerance_flat(10);
END
```

## Syntax
Each configuration uses a combination of `set(config_keyword).value(some_value);`.  The table below lists each configuration node's keyword, purpose and the value node format.

|    set node key    | setting function                                                                | special nodes                                                  | value node                         |
|:------------------:|:--------------------------------------------------------------------------------|:---------------------------------------------------------------|:-----------------------------------|
| `afk_can_subtract` | if true, being AFK too long will cause losses in XP for that same action        |                                                                | true/false, default `value(false)` |
|       `afk`        | adds a setting for xp reductions while AFK                                      | `event(EVENT_TYPE)` sets the event this afk setting applies to | see `Setting` values below         |
|  `diminishing_xp`  | adds a setting for xp reductions when repeating the same action                 | `event(EVENT_TYPE)` sets the event this afk setting applies to | see `Setting` values below         |
|  `normalization`   | adds a setting to make xp closer to previous earnings for events with xp spikes | `event(EVENT_TYPE)` sets the event this afk setting applies to | see `Setting` values below         |

### `Setting` value nodes
|       node key       | value format                   | default | function                                                                                                        |
|:--------------------:|:-------------------------------|:-------:|:----------------------------------------------------------------------------------------------------------------|
|       `source`       | a list of object ID strings    |  none   | filters this anti-cheese setting by the source type.  varies by event type                                      |
| `min_time_to_apply`  | a whole number in milliseconds |    0    | for AFK settings defining the minimum time being AFK before reductions start                                    |
|     `reduction`      | a decimal value                |   0.0   | the amount per duration of the anti-cheese to reduce xp by                                                      |
|   `cooloff_amount`   | a whole number                 |    0    | the amount of reduction to fall off per tick after no longer meeting the anti-cheese criteria                   |
| `tolerance_percent`  | a decimal value                |   0.0   | for normalization settings, what percent difference from the last xp is the limit                               |
|   `tolerance_flat`   | a whole number                 |    0    | same as above but a flat difference, but for afk tracking applies to distance moved to still be considered AFK. |
| `retention_duration` | a whole number                 |    0    | how long after no longer meeting criteria before reductions start to fall off                                   |
|  `strict_tolerance`  | true/false                     |  true   | used in AFK tracking. if true, the player's facing angle must also be within tolerance to be "afk"              |                                             