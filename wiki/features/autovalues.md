[Home](../home.md)

# AutoValues
Project MMO's dynamic system for estimating values.  Since, the mod cannot contain all possible current and future mod blocks, items, entities, dimensions, biomes, enchantments, and effects, AutoValues uses properties of those objects to create them dynamically.

An important detail to note is that not all values in Project MMO have AutoValues.  Some settings are too nuanced, or the objects themselves do not provide a way to discern their properties to be able to make reasonable estimates.  What is listed below is all of what AutoValues adds.

## Configuring AutoValues
All settings can be set in `pmmo-AutoValues.toml`.  This is a server config, located in your world's `serverconfig/` folder.  The config is broken into 3 sections: 
- Toggles for disabling specific AutoValues
- values for events and req calculations
- multipliers (aka "tweaks") for controlling how objects are calculated

In general, settings in AutoValues mimic settings in an object's config file.  All exceptions will be listed in the sections below.

### Item Exceptions: Tool Overrides
Item requirements include an "XXX TOOL Override" option for giving non-pickaxes different requirements.  This works the same as the regular TOOL req, only for the specified tool type.  The list of overrides includes:
- Shovels
- Swords
- Axes
- Hoes
Note: for a tool to be detected as not being a pickaxe, it must be implemented by the mod as an extension of the corresponding vanilla tool.  If the mod implements their tools in any other way, pmmo will not detect it and may not even see it as a tool at all.

### Item Exceptions: Penalties
If a tool req is not met, these effects will be given to the player.  These settings apply to all items, even those configured manually, but only if the item does not specify its own negative effects.  

### Block Exceptions: Tool Breakable Overrides
For blocks that exist in the `minecraft:mineable/axe`, `minecraft:mineable/hoe`, and `minecraft:mineable/shovel` tags, respectively, a special override exists to award XP for breaking or placing those blocks.  This works the same as the default break and place XP only you can specify a different skill and ratio.

### Block Exceptions: Rarities Multiplier
if a block is a member of the `forge:ores` tag, any calculated XP will be multiplied by this value.  If a modded ore is not a member of this tag, it will not be interpretted as an ore by pmmo and will be awarded the default block xp.



## Tweaks
These are special values used in the math part of calculating a requirement or xp gain.

### Block Hardness Modifier
Takes the block's hardness, which is set in the code that implements it and determines how long it takes to break the block, and multplies this value by that one.  That value is then multiplied by the XP/Req setting to get your final value.

### Item Tweaks: XXX Attributes
There are ten different item types that have attribute calculations.  For each one, if the item has the listed attribute, that value will be multiplied by the value set in the config and all attribute values are then added together to get a final value.  That value is then multiplied by the xp/req setting to get your final value seen in game.

### Entity Tweaks
same as above, only for entities.

[Home](../home.md)