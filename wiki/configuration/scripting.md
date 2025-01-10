[Home](../home.md)

# Scripting
This feature allows you to define `.pmmo` files in your config folder that are read when your world loads and provide configurations that supersede datapacks.  This powerful feature simplifies the configuration process.

## Basic Syntax
`.pmmo` files use a "chaining" style where each line is a configuration that "chains" together nodes of information

A node is simply `keyword(data)`.  The keyword defines what happens behind the scenes, and the data is the specific information it needs to know what you want to do.

Nodes are connected with a period (`.`).  By connecting nodes with a period, you "chain" them together.  Let's look at an example:
```
color(yellow).type(ball).sport(tennis);
```
*Note: the semicolon (`;`) at the end is important and will be explained later*

## PMMO Basic Nodes
PMMO has 3 basic types of nodes, [__target__](scripting.md#target-nodes) nodes, [__value__](scripting.md#value-nodes) nodes, and [__feature__](scripting.md#feature-nodes) nodes.  All configurations must have a target node.  feature nodes are what actually do the work, so you will always have one, or else you line doesn't do anything.  The value node is a holder for special information that certain feature nodes will use.

### Target Nodes
PMMO has specific target nodes that correspond to object types in the game.  they are:
- `item`
- `block`
- `entity`
- `dimension`
- `biome`
- `effect`
- `enchantment`

Target nodes take one or multiple IDs.  for example:
```
item(minecraft:stick)
block(minecraft:obsidian)
entity(minecraft:zombie, iceandfire:wyvern, alexmobs:elephant)
```
*Note: when using multiple IDs, each id is separated by a commma*

Target nodes can also take tags and wildcards.  prefixing a `#` to an ID will tell PMMO this id is a tag. eg `blocks(#c:ores/copper)`.  wildcards let you provide a modid and have all objects for that mod configured by the line.  to use wildcards, use an ID like `modid:*`.  eg `entity(alexmobs:*)`

PMMO also provides advanced target nodes, which you can read about [here](#advanced-target-nodes)

### Value Nodes
Value nodes are blank space for feature nodes to grab additional information from.  They follow the same format of `keyword(data)` as other nodes.  See the table in the next section for what value nodes each feature node uses.

### Feature Nodes
These nodes take the target and perform a configuration action for that object.  Some feature nodes can use the value node for extra data.  The Feature nodes, their applicable data values, and what/if they use the value node are detailed below:

|      keyword      | data format                                                                   | value format                                                                                                                                                                                                                                                 |
|:-----------------:|:------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|       `xp`        | event name in all uppercase letters.  Eg. CONSUME, BREAK, PLACE, CRAFT        | keyword = `award`. the skills and experience separated by commas.  `skill,1`  for multiple skills continue the pattern `award(skill,1,otherskill,10)`                                                                                                        |
|   `deal_damage`   | the damage type id                                                            | keyword = `require`. the skills and experience separated by commas.  `skill,1`  for multiple skills continue the pattern `requires(skill,1,otherskill,10)`                                                                                                   |
| `receive_damage`  | the damage type id                                                            | keyword = `award`. the skills and experience separated by commas.  `skill,1`  for multiple skills continue the pattern `award(skill,1,otherskill,10)`                                                                                                        |
|       `req`       | req name in all uppercase letters.  EG. KILL, BREAK, TRAVEL                   | keyword = `award`. the skills and experience separated by commas.  `skill,1`  for multiple skills continue the pattern `award(skill,1,otherskill,10)`                                                                                                        |
|      `bonus`      | bonus type in all uppercase letters. Eg. HELD, WORN, BIOME, DIMENSION         | keyword = `value`. the skills and bonus percent as decimal separated by commas.  `skill,1.1`  for multiple skills continue the pattern `value(skill,1.1,otherskill,2.0)`                                                                                     |
|   `vein_charge`   | the amount of charge this item gives                                          | N/A                                                                                                                                                                                                                                                          |
|  `vein_capacity`  | the capacity this item adds                                                   | N/A                                                                                                                                                                                                                                                          |
|  `vein_consume`   | the amount of vein consumed by this block                                     | N/A                                                                                                                                                                                                                                                          |
|    `mob_scale`    | the id of the mob being scaled                                                | keyword = `attribute`. the attributed id and scaling value separated by a comma. `minecraft:generic.max_health,1.001` for multiple attributes, continue the pattern `attribute(minecraft:generic.max_health,1.001,minecraft:generic.movement_speed,1.00001)` |
| `positive_effect` | the effect id and magnitude separated by a comma.  Eg `minecraft:swiftness,2` | N/A                                                                                                                                                                                                                                                          |
| `negative_effect` | the effect id and magnitude separated by a comma.  Eg `minecraft:weakness,2`  | N/A                                                                                                                                                                                                                                                          |
|     `salvage`     | the Item ID for the item returned when salvaged.                              | see table below                                                                                                                                                                                                                                              |
|       `set`       | *used only with the `config` target selector*, accepts a config key           | see links below for server config keys and examples                                                                                                                                                                                                          |


### Salvage value keywords
|    keyword    | data format                                                                                                                                  |
|:-------------:|:---------------------------------------------------------------------------------------------------------------------------------------------|
|`chance_level` | the skills and chance separated by commas.  `skill,0.1`  for multiple skills continue the pattern `chance_level(skill,0.3,otherskill,0.001)` |
| `chance_base` | percent as a decimal `chance_base(0.1)` 10% chance.                                                                                          |
| `chance_max`  | percent as decimal `chance_max(0.8)` 80% max chance.                                                                                         |
|  `level_req`  | the skills and levels separated by commas.  `skill,10`  for multiple skills continue the pattern `level_req(skill,10,otherskill,5)`          |
|`salvage_award`| the skills and xp separated by commas.  `skill,10`  for multiple skills continue the pattern `salvage_award(skill,10,otherskill,5)`          |
|  `max_drops`  | whole number `max_drops(3)`                                                                                                                  |

### Server config syntax and examples
[Server Config](./serverconfig.md)

## Advanced Syntax 
Long repetitive lines over the length of a file are both tedious and an eyesore.  To help with that, there are some special formatting tools you can use to make your life easier.

## Comments
Any text on the same line and after `//` will be ignored by the script.  You can use this to document your scripts.

## WITH statement
By starting a line with `WITH ` the nodes that follow will be used with every line after until you define another `WITH`.  For example, if we wanted to write out how much xp breaking blocks should be, we can do the following 
```
WITH xp(BLOCK_BREAK)
    block(stone, andesite, granite, diorite).award(mining,10);
    block(obsidian).award(mining,500);
    block(extra_ores:ultra_diamond).award(mining,5000);
```
Without the `WITH` statement before, each line would need to repeat the `.xp(BREAK)`.

To remove the prefix create a line that just has `END`

## Omitting The Semicolon
Earlier the semicolon (`;`) was mentioned.  Here it is explained.  Semicolons end chains.  Which means if you omit the semicolon you can continue on the next line.  this is helpful for really long lines to make them more readable
```
//very long line
item(iron_sword).vein_charge(1.5).vein_capacity(30).deal_damage(minecraft:player_attack).award(combat,10);

//same line but broken up for readability
item(iron_sword)
    .vein_charge(1.5)
    .vein_capacity(30)
    .deal_damage(minecraft:player_attack).award(combat,10);
    
//a simple example of breaking up the value to make the skills easier to read
item(diamond_helmet).xp(CRAFT)
    .award(crafting,100,
           smithing,1000);
```

## Advanced Target Nodes
These selectors allow you to configure objects based on common properties without knowing the item ID in advance.  You can use this to create configurations that apply to new mods as they are added without needing to explicitly add the object IDs to your script.

*Note: if a line contains both a basic target node and an advanced selector, the latter in the chain will be used*

Here are the advanced nodes and their parameters.  Advanced selectors do not use value nodes

| selector keyword | parameters                                                                                                                                                                                                                                                                                                                                                                                           |
|:----------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|      `food`      | takes two optional expressions separated by a comma `(expr1,expr2)`. the expressions start with a keyword of either `nutrition` or `saturation` followed by an operator >, <, =, >=, or <= and the value to filter by.  eg `(nutrition>=5,saturation>0)` applies your settings to all food of 5 or more nutrition and any saturation.  (omitting saturation in this case would have the same effect) |
|      `tool`      | configures any item with the `TOOL` data component.  accepts an optional item tag as a parameter. eg `tool()` will configure all items with the data component, but `tool(minecraft:pickaxes)` will configure all items with the data component AND are in the pickaxes tag.                                                                                                                         |
|     `armor`      | configures any item extending `ArmorItem`.  accepts an optional item tag as a parameter. eg `armor()` will configure all items of the class, but `armor(minecraft:chest_armor)` will configure all items of the class AND are in the chest_armor tag.                                                                                                                                                |
|     `weapon`     | configures any item with the `DAMAGE` data component.  accepts an optional item tag as a parameter. eg `weapon()` will configure all items with the data component, but `weapon(minecraft:swords)` will configure all items with the data component AND are in the swords tag.                                                                                                                       |
|     `config`     | configures a serverconfig file and accepts the config name as the parameter                                                                                                                                                                                                                                                                                                                          | one of `server`, `autovalues`, `skills`, `perks`, `globabls`, `anticheese`                                                                                                                                                                                   |

[Home](../home.md)
