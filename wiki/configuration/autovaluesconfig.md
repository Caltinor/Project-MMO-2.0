# AutoValues Config Syntax and Examples

## Example File
```
WITH config(autovalues)

END
```

## Syntax
Each configuration uses a combination of `set(config_keyword).value(some_value);`.  The table below lists each configuration node's keyword, purpose, the value node format, and any necessary special nodes.

|         set keyword         | setting function                                                                | special nodes                                                 | value node                                                                        |
|:---------------------------:|:--------------------------------------------------------------------------------|:--------------------------------------------------------------|:----------------------------------------------------------------------------------|
|          `enabled`          | whether autovalues are active                                                   |                                                               | true/false. default `value(true)`                                                 |
|    `enable_requirements`    | whether automatic requirement values should calculate                           | `requirement(TYPE)` the req type being enabled/disabled       | true/false. default `value(true)`                                                 |
|     `enable_xp_awards`      | whether automatic xp values should calculate                                    | `event(TYPE)` the event type being enabled/disabled           | true/false. default `value(true)`                                                 |
|    `rarities_multiplier`    | mulitplies xp calculations for blocks in the ores tag                           |                                                               | decimal value. default `value(10)`                                                |
|          `item_xp`          | sets the skill and xp multiplier for item xp events                             | `event(TYPE)` the event being configured                      | skills and whole numbers separated by commas. `value(skill,1,skill_two,5)`        |
|         `block_xp`          | same as above for blocks                                                        | `event(TYPE)` the event being configured                      | same as above                                                                     |
|         `entity_xp`         | same as above for entities                                                      | `event(TYPE)` the event being configured                      | same as above                                                                     |
|  `axe_breakable_override`   | for all events where axes are the corret tool, sets the skill and multiplier    |                                                               | skills and whole numbers separated by commas. `value(skill,1)`                    |
|  `hoe_breakable_override`   | for all events where hoes are the corret tool, sets the skill and multiplier    |                                                               | skills and whole numbers separated by commas. `value(skill,1)`                    |
| `shovel_breakable_override` | for all events where shovels are the corret tool, sets the skill and multiplier |                                                               | skills and whole numbers separated by commas. `value(skill,1)`                    |
|      `shovel_override`      | for all requirements related to shovels.                                        |                                                               | a skill and level ratio separated by commas. `value(skill,3)`                     |
|      `sword_override`       | for all requirements related to swords.                                         |                                                               | a skill and level ratio separated by commas. `value(skill,3)`                     |
|       `axe_override`        | for all requirements related to axes.                                           |                                                               | a skill and level ratio separated by commas. `value(skill,3)`                     |
|       `hoe_override`        | for all requirements related to hoes.                                           |                                                               | a skill and level ratio separated by commas. `value(skill,3)`                     |
|         `penalties`         | the potion effects applied for not meeting requirements                         |                                                               | effect ids and effect levels separated by commas. `value(minecraft:slowness,0)`   |
|           `items`           | the skills and ratio values for each requirement type.                          | `requirement(TYPE)` specifies the req type                    | a skill and ratio separated by commas. `value(skill,1)`                           |
|     `hardness_modifier`     | How block hardness scales block ratios                                          |                                                               | decimal value. default `value(0.65)`                                              |
|        `tool_tweaks`        | How tool properties scale autovalues                                            | `type(...)` one of `SWORD`, `PICKAXE`, `AXE`, `SHOVEL`, `HOE` | an attribute type (see below) and ratio value as decimal. `value(Durablity,10.0)` |
|      `wearable_tweaks`      | How armor properties scale autovalues                                           | `type(...)` one of `HEAD`, `CHEST`, `LEGS`, `BOOTS`, `WINGS`  | same as above                                                                     |
|       `entity_tweaks`       | How monster attributes scale autovalues                                         |                                                               | same as above                                                                     |

### Attribute Types
| keyword              | use                                    |
|:---------------------|:---------------------------------------|
| Durability           | factor of tool max durability          |
| Tier                 | factor of tool tier                    |
| Damage               | factor of entity or tool attack damage |
| Attack_Speed         | factor of tool attack speed            |
| Dig_Speed            | factor of tool block breaking speed    |
| Armor                | factor of item armor value             |
| Knockback_Resistance | factor of item knockback value         |
| Toughness            | factor of item toughness value         |
| Health               | factor of entity hitpoints             |
| Move_Speed           | factor of entity movement speed        |
