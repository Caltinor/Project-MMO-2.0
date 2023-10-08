[Home](../home.md)

PMMO 2.0 adds a very simple solution to the previous systems of Treasure, Extra Drops, Rare Mob Drops, and Rare Fish Pool. Using Forge's "Global Loot Modifier" functionality, pmmo adds an on-chance means of adding an extra drop to any loot drop.

In your datapack, under any namespace, add a folder called loot_modifiers. Within this folder add json files with any name you like (must still conform to [datapack naming rules](https://minecraft.fandom.com/wiki/Resource_location#Legal_characters))

Your file will then have these required components

```json5
{
  "type":"pmmo:treasure", //specifies pmmo's treasure feature.  use "pmmo:rare_drop" for non-blocks
  "conditions":[], //we'll get to these soon
  "item":"minecraft:apple", //any item ID from a loaded mod
  "count": 1, //how many of this item will drop
  "chance": 0.25 //how likely it is to drop once conditions are met
}
```

In this example, we have a 25% chance of getting a single apple when loot is dropped. Since we do not have any conditions, this will apply to all loot tables including chests.
Conditions

Adding conditions specifies when this treasure is to be attempted. Minecraft provides many conditions (called "Predicates") that are used for existing loot tables, and you can use those in this section as well. You can read about thos on [The Minecraft Wiki](https://minecraft.fandom.com/wiki/Predicate). PMMO also adds 4 conditions as well such as player level requirements, and specific block targets. You can read about those [HERE](https://github.com/Caltinor/Project-MMO-2.0/wiki/Loot-Predicates)

Conditions work on an "AND" basis unless you use the "alternate" type. Which means that if you put a player skill and a specific block condition, the player must have the requisite skill AND be breaking the specified block to trigger the treasure chance.

Once you have the loot modifier complete, it will need to have an entry in Forge's GLM tag. Your GLM tag file \(`global_loot_modifiers.json`\) will resemble the following and be placed in `forge/loot_modifiers` folder


```json5
{
  "entries": [], //entries go in here
  "replace": false
}
```

### Example:
The following would add a 1% chance to find Netherite Scrap when mining Netherrack with at least 20 mining skill.

`data/pmmo/loot_modifiers/netherite_from_netherrack.json`
```json5
{
  "type": "pmmo:treasure",
  "chance": 0.01, //1% chance
  "conditions": [
    {
      "condition": "pmmo:skill_level", //checking skill level
      "level_max": 2147483647, //huge maximum, not required
      "level_min": 20, //minimum level
      "skill": "mining" //mining skill
    },
    {
      "condition": "pmmo:valid_block", //checking which block
      "block": "minecraft:netherrack" //netherrack
    }
  ],
  "count": 1, //one item
  "item": "minecraft:netherite_scrap" //reward
}
```

`data/forge/loot_modifiers/global_loot_modifiers.json`
```json5
{
  "entries": [
    "pmmo:netherite_from_netherrack" //points to the above loot modifier
  ],
  "replace": false
}
```

[Home](../home.md)