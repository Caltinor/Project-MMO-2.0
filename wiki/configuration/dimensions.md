[Home](../home.md)

This page details the data format for json files in the `data/namespace/pmmo/dimensions/` data folder.

## Example File
```json5
{
    "isTagFor":[ //use to have settings copied to all below members. the filename is ignored and is not automatically added.
        "minecraft:overworld",
        "minecraft:nether"
    ],
    //When in the dimension, what bonuses does the player get
    "bonus":{ //if not adding bonuses, you can remove the entire section
        "DIMENSION":{
            "mining": 1.1, //this is a 10% increase in xp gain
            "flying": 0.75 //this is a 25% reduction in xp gain
        }
    },
    //this governs what skill is required to teleport to the dimension. affects teleportation by command too.
    "travel_req":{ //if not adding travel requirements, you can remove the entire section
        "agility": 5
    },
    //What blocks should not be permitted to be vein-mined
    "vein_blacklist":[ //if not adding, you can leave it, but it's cleaner to remove it
        "minecraft:bedrock",
        "minecraft:ancient_debris"
    ],
    //Which mobs, and by how much, should be modified in this dimension. 
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
When naming and adding a file you should name the file according to the data value of the dimension.  for example `overworld.json` for the vanilla overworld.  or `undergarden.json` for the undergarden dimension.  From there you need to place it under the correct namespace folder. eg `data/minecraft/pmmo/dimensions/overworld.json` or `data/undergarden/pmmo/dimensions/undergarden.json` respectively.

[Home](../home.md)