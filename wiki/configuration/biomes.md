[Home](../home.md)

This page details the data format for json files in the data/namespace/pmmo/biomes/ data folder.

## Example File
```json5
{
    "isTagFor":[ //use to have settings copied to all below members. the filename is ignored and is not automatically added.
        "minecraft:plains",
        "minecraft:meadow"
    ],
    "bonus":{
        "BIOME":{
            "mining": 1.1, //this is a 10% increase in xp gain
            "flying": 0.75 //this is a 25% reduction in xp gain
        }
    },
    //If a player meets this requirement, the "positive_effect" is applied, otherwise the "negative_effect" is applied.
    //Note that if you do not define a positive or negative effect, that behavior is skipped.  This means that you
    //can have a positive effect for meeting the requirement without a negative effect for not, or vice versa
    "travel_req":{ //remove if both positive and negative effects are undefined
        "agility": 5
    },
    "positive_effect":{ //remove if unused
        "minecraft:regeneration": 1 //the level of the effect - 1
    },
    "negative_effect":{ //remove if unused
        "minecraft:poision": 2
    },
    //What blocks should not be permitted to be vein-mined
    "vein_blacklist":[ //if not adding, you can leave it, but it's cleaner to remove it
        "minecraft:bedrock",
        "minecraft:ancient_debris"
    ],
    //Which mobs, and by how much, should be modified in this biome. 
    //Note: this applies on mob spawns.  Mobs will not get weaker/stronger by changing the biome they are in.
    "mob_multiplier":{
        "minecraft:zombie": {
          "minecraft:generic.max_health": 0.5, //half health
          "minecraft:generic.movement_speed": 2.0, //double speed
          "minecraft:generic.attack_damage": 1.1 // 10% increase in damage
        },
        "minecraft:skeleton": {
            "minecraft:generic.attack_damage": 1.15  //not all attributes need to have values, only what you want to modify.
        }
    }
}
```

## Tags and File names
When naming and adding a file you should name the file according to the data value of the biome. for example `plains.json` for the vanilla plain biome. or `redwood_forest.json` for the BYG biome. From there you need to place it under the correct namespace folder. eg `data/minecraft/pmmo/biomes/plains.json` or `data/byg/pmmo/biomes/redwood_forest.json` respectively.

[Home](../home.md)