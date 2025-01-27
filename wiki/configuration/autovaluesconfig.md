[Back](./scripting.md#server-config-syntax-and-examples)

# AutoValues Config Syntax and Examples

## Example File
```
WITH config(autovalues)
    set(enabled).value(true);
    set(enabled_requirements).requirement(WEAR).value(true);
    set(enabled_requirements).requirement(TOOL).value(true);
    set(enabled_requirements).requirement(TRAVEL).value(false);
    set(enabled_xp_awards).event(DEATH).value(true);
    set(enabled_xp_awards).event(FISH).value(false);
    
    //the "xp_awards" section from the json
    set(rarities_muliplier).value(10);
    set(axe_breakable_override).value(woodcutting,10);
    set(hoe_breakable_override).value(farming,10);
    set(shovel_breakable_override).value(excavation,10);
    
    set(item_xp).event(ANVIL_REPAIR).value(smithing,10);
    set(item_xp).event(BLOCK_PLACE).value(building,10);
    set(item_xp).event(ENCHANT).value(magic,10);
    set(item_xp).event(FISH).value(fishing,10);
    set(item_xp).event(CONSUME).value(cooking,10);
    set(item_xp).event(SMELT).value(smithing,100);
    set(item_xp).event(CRAFT).value(crafting,10);
    
    set(block_xp).event(GROW).value(farming,1);
    set(block_xp).event(BLOCK_BREAK).value(mining,1);
    set(block_xp).event(BLOCK_PLACE).value(building,1);
    
    set(entity_xp).event(TAMING).value(taming,1);
    set(entity_xp).event(ENTITY).value(charisma,1);
    set(entity_xp).event(SHIELD_BLOCK).value(endurance,1);
    set(entity_xp).event(BREED).value(taming,1);
    set(entity_xp).event(RIDING).value(taming,1);
    set(entity_xp).event(DEATH).value(endurance,1);
    
    //the "requirements" section from the json
    set(axe_override).value(woodcutting,1);
    set(hoe_override).value(farming,1);
    set(shovel_override).value(excavation,1);
    set(sword_override).value(combat,1);
    set(penalties).value(
        "minecraft:weakness",1,
        "minecraft:slowness",1,
        "minecraft:mining_fatigue",1);
    set(block_default).value(mining,1);
    
    set(items).requirement(WEAR).value(endurance,1);
    set(items).requirement(WEAPON).value(combat,1);
    set(items).requirement(USE_ENCHANTMENT).value(magic,1);
    set(items).requirement(TOOL).value(mining,1);
    
    //the "tweaks" section from the json config
    set(hardness_modifier).value(0.65);
    set(entity_tweaks).value(
        Health,0.5,
        Damage,1.5,
        Move_Speed,0.15);
    
    set(tool_tweaks).type(SWORD).value(
        Dig_Speed,10.0,
        Durability,0.01,
        Attack_Speed,10.0,
        Damage,1.5,
        Tier,10.0);
    set(tool_tweaks).type(AXE).value(
        Dig_Speed,10.0,
        Durability,0.01,
        Attack_Speed,10.0,
        Damage,1.5,
        Tier,10.0);
    set(tool_tweaks).type(PICKAXE).value(
        Dig_Speed,10.0,
        Durability,0.01,
        Attack_Speed,10.0,
        Damage,1.5,
        Tier,10.0);
    set(tool_tweaks).type(SHOVEL).value(
        Dig_Speed,10.0,
        Durability,0.01,
        Attack_Speed,10.0,
        Damage,1.5,
        Tier,10.0);
    set(tool_tweaks).type(HOE).value(
        Dig_Speed,10.0,
        Durability,0.01,
        Attack_Speed,10.0,
        Damage,1.5,
        Tier,10.0);
    
    set(wearable_tweaks).type(BOOTS).value(
        Durability,0.01,
        Armor,10.0,
        Knockback_Resistance,10.0,
        Toughness,10.0);
    set(wearable_tweaks).type(CHEST).value(
        Durability,0.01,
        Armor,10.0,
        Knockback_Resistance,10.0,
        Toughness,10.0);
    set(wearable_tweaks).type(LEGS).value(
        Durability,0.01,
        Armor,10.0,
        Knockback_Resistance,10.0,
        Toughness,10.0);
    set(wearable_tweaks).type(HEAD).value(
        Durability,0.01,
        Armor,10.0,
        Knockback_Resistance,10.0,
        Toughness,10.0);
    set(wearable_tweaks).type(WINGS).value(
        Durability,0.01,
        Armor,10.0,
        Knockback_Resistance,10.0,
        Toughness,10.0);
END
```

## Syntax
Each configuration uses a combination of `set(config_keyword).value(some_value);`.  The table below lists each configuration node's keyword, purpose, the value node format, and any necessary special nodes.

|         set keyword         | setting function                                                                | special nodes                                                 | value node                                                                        |
|:---------------------------:|:--------------------------------------------------------------------------------|:--------------------------------------------------------------|:----------------------------------------------------------------------------------|
|          `enabled`          | whether autovalues are active                                                   |                                                               | true/false. default `value(true)`                                                 |
|   `enabled_requirements`    | whether automatic requirement values should calculate                           | `requirement(TYPE)` the req type being enabled/disabled       | true/false. default `value(true)`                                                 |
|     `enabled_xp_awards`     | whether automatic xp values should calculate                                    | `event(TYPE)` the event type being enabled/disabled           | true/false. default `value(true)`                                                 |
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
|         `penalties`         | the potion effects applied for not meeting requirements                         |                                                               | effect ids and effect levels separated by commas. `value("minecraft:slowness",0)`   |
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

[Back](./scripting.md#server-config-syntax-and-examples)
