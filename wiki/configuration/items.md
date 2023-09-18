This page details the configuration details for jsons placed under `/data/namespace/pmmo/items/`.

## File Naming Convention
Like all datapack jsons, the file name corresponds to the object path under the nested namespace.  this means that `data/minecraft/pmmo/items/stone.json` corresponds to `"minecraft:stone"`.

## Base Template
This template lists all root fields for this configuration. all fields are optional
```json
{
    "override": false,
    "isTagFor":[],
    "xp_values":{},
    "nbt_xp_values":{},
    "dealt_damage_xp": {},
    "requirements":{},
    "nbt_requirements":{},
    "bonuses":{},
    "nbt_bonuses":{},
    "negative_effect":{},
    "salvage":{},
    "vein_data":{}
}
```
the sections that follow will elaborate on each section

## Overrides
Pmmo ships with default settings so that the general user has something to play with, out of the box.  You may wish to change these defaults to your liking.  To ensure your setting takes precedence, add the property `"override": true` to your configurations.  This will also apply if you use multiple datapacks and wish to ensure your setting overrides all others.  If override is omitted or set to false and another configuration exists, the higher value will be used in all cases.

## "isTagFor": []
When this tag is used, the members of this array are given the same configuration as this object.  For example, if we wanted to give the same xp value for crafting stone tools we would define
```json
"isTagFor": [
  "minecraft:stone_pickaxe",
  "minecraft:stone_shovel",
  "minecraft:stone_axe",
  "minecraft:stoen_hoe"
]
```

## "xp_values":{} and "nbt_xp_values":{}
These setting determine what xp is given for this item for specific events.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "xp_values" looks as follows:
```json
"xp_values":{
  "CRAFT": {
    "crafting": 100,
    "smithing": 55
  },
  "ENCHANT": {
    "magic": 150
  }
}
```
In this example if we craft the item we will get 100 crafting XP and 55 smithing XP.  if we enchant the item we get 150 magic XP.  if we Smelt the item we get nothing since it is not defined.  The NBT variant uses the same root=>Event structure however, instead of skills and experience there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Event Types For Items
|Event| Context                                                          |
|:---|:-----------------------------------------------------------------|
|ANVIL_REPAIR| when this is the output item                                   |
|BLOCK_PLACE| if this is a block item, the xp for placing the block          |
|BREW| as brew ingredient, output does not matter                     |
|CONSUME| if eaten/drank                                                 |
|CRAFT| when this is the output itemm                                  |
|ENCHANT| when enchants are added to this item                           |
|FISH| when this is the item obtained from fishing                    |
|SMELT| when this is the item being smelted/cooked, NOT the output     |
|ACTIVATE_ITEM|  if this item has a right-click action, when that is activated |

## "dealt_damage_xp":{}
In 1.19.4, Mojang made damage a datapack object, which allowed users to add their own damage types via datapack.  Included in this new feature was the ability to make damage type tags.  To take advantage of this feature, PMMO uses those damage types and tags when determining what XP to give a player.  Using this system, you can give players a different type of XP based on the type of damage they dealt.  For a list of all the vanilla damage types, see [HERE](damagetypes.md).  If using damage tags, pmmo will treat an entry like a tag if it is preceded with a `#`.  An example default implementation for "dealt_damage_xp" looks as follows:
```json5
"dealt_damage_xp":{
  "minecraft:player_attack": { //gives the below XP for every point of damage dealt with melee attacks
    "combat": 10  
  },
  "#minecraft:is_projectile": { //using a tag, we can give archery XP for every point of damage dealt by projectiles
    "archery": 10
  },
  "magic_mod:spell_damage": { //if a mod adds their own damage type, you can use those here too.
    "magic": 100,
    "spellcasting": 50
  } 
}
```

## "requirements":{} and "nbt_requirements":{}
These settings determine whether an item is permitted to perform a particular function/action.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "requirements" looks as follows:
```json
"requirements": {
  "TOOL": {
    "mining": 10,
    "excavation": 5
  },
  "WEAPON": {
    "combat": 10
  }
}
```
In this example the player must have 10 mining levels and 5 excavation levels to use this item as a tool.  They must have 10 combat levels to use this item as a weapon, and if the item has a right click action, there is no requirement.  The NBT variant uses the same root=>ReqType structure however, instead of skills and levels there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Req Types for Items
| Req Type | Context                                                             |
|:---------|:--------------------------------------------------------------------|
| WEAR     | for placing in armor slots, curio slots, or either hand             |
| TOOL     | for use breaking blocks                                             |
| WEAPON   | for use harming entities                                            |
| USE      | the ability to right-click this item                                |
| PLACE    | if this is a block item, the ability to place it                    |
| BREAK    | if this is a block item, the ability to break it                    |
| INTERACT | the ability to use this item on blocks/entities. eg gold on piglins |


## "bonuses":{} and "nbt_bonuses":{}
These settings determine what bonuses the item gives when used in the prescribed way.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "bonuses" looks as follows:
```json
"bonuses": {
  "HELD": {
    "farming": 1.5,
    "flying": 0.25
  },
  "WORN": {
    "endurance": 2.0,
    "mining": 0.5
  }
}
```
In this example if the player holds the item either hand they get a 50% increase in farming XP and a 75% penalty on flying (reduced to 25% gain if that makes more sense).  If the player is able to put this item into an armor/curio slot they instead get double xp in endurance and half xp in mining.  The value used in this configuration is a raw multiplier of the XP.  Therefore, a value of 1.0 does nothing.  The NBT variant uses the same root=>Type structure however, instead of skills and multipliers there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Bonus Types For Items
| Type| Context                   |
|:----|:--------------------------|
| HELD| (in either hand)          |
| WORN| (in an armor/curio slot)  |

## "negative_effect":{}
A negative effect is applied when the player holds/wears an item but does not meet the HELD/WORN requirements.  The format for this section is the ID of the status effect you want to give to the player.  Modded status effects work as well as vanilla.  the number is the level you want for the status effect.  Zero is the lowest level.  Here is an example configuration:
```json
"negative_effect": {
  "minecraft:slowness": 0,
  "minecraft:mining_fatigue": 1
}
```
In this example the player will get a continuous slowness 1 and mining fatigue 2 while they hold this item without meeting its requirement.

## "salvage":{}
Salvage allows you to define what this item can salvage into, if anything.  Salvaging has two parts: the output items and their salvage chance info.  To start simple, we are going to list all the items we want to return.  for this example let's assume this is an enchanting table.
```json
"salvage": {
  "minecraft:obsidian": {},
  "minecraft:diamond": {},
  "minecraft:book": {}
}
```
Before we get into the specific salvage settings, we can see that this item has values for each of the component items, but we don't have to just use recipe items.  I could also put something like a nether_star as an output.  What items salvage into is completely up to you.  Perhaps you think that salvaging a stone pickaxe should give regular stone instead of cobblestone.  That's up to you.  each of this output items is a "salvage entry".

Next we have to define our salvage criteria for each item.  Unlike other settings, all of these settings are mandatory, though, you may leave some of them empty.  I will give examples of each.  Here is each of the fields needed to set up salvage:

| Property          |Purpose|
|:------------------|:---|
| `"chancePerLevel"` |a map where the keys are skill names and the values are how much extra chance this skill adds to the salvage chance|
| `"levelReq"` |a map of skills and the level in that skill necessary to unlock this salvage entry|
|`"xpPerItem"`|a map of skills and the XP to award to the player for each time this salvage is obtained|
|`"salvageMax"`|the number of times this salvage will attempt to be returned.  Each time is another roll of the chance amount|
|`"baseChance"`|the starting chance before `chancePerLevel` is calculated|
|`"maxChance"`|an upper limit to chance.  This can be used to prevent an item from being 100% obtainable|

Let's look at a more realistic example
```json
"minecraft:obsidian": {
  "chancePerLevel": {
    "smithing": 0.05,
    "crafting": 0.01
  },
  "levelReq": {
    "mining": 50
  },
  "xpPerItem": {
    "smithing": 100
  },
  "salvageMax": 2,
  "baseChance": 0.25,
  "maxChance": 0.5
}
```
this salvage entry will attempt to give us Obsidian twice because of the `salvageMax`.  Our base chance is 0.25 or 25% chance for each try.  but if we have skill in smithing or crafting we get an extra 0.05 and 0.01 per level respectively.  let's assume we are at 100 levels in both. That means we get an extra 5.0 and 1.0 chance respectively giving us a 625% chance.  That means we would get it every time and get both except.... we have a `maxChance` of 0.5 which means that despite our high skill, this item is capped at a 50% chance.  We go to salvage anyway, and we get one obsidian.  the first was a success, but we didn't get it the second time.  Because we got one we also get 100 xp in smithing.  Had we been lucky and gotten both, we would have gotten 200 xp in smithing.  

The last entry that I didn't cover was the level req.  for any of the above to happen I had to have 50 mining.  without that level req, I would never get any obsidian. If we have multiple entries such as for the diamonds and the book, and they have different level reqs, it is possible to salvage an item for its lesser entries.  If no reqs are met, the salvage attempt will not happen, but if just one req is met, the salvage will try to return what it can.

repeat these entries for every item you want to give as salvage.

## "vein_data":{}
Vein Data lets you give this item vein charge and capacity.  This means that the item can be used to provide the player with charge towards the vein ability. you can read more about Vein Mining [HERE](../features/vein.md)  Items have two settings:

|Property| Context                                                          |
|:---|:-----------------------------------------------------------------|
|`"chargeCap"`| the max charge this item provides (whole numbers)                |
|`"chargeRate"`| the rate at which this item recovers used charge (decimal value) |
```