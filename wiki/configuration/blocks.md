[Home](../home.md)

This page details the configuration details for json files placed under `/data/namespace/pmmo/blocks/`.

## File Naming Convention
Like all datapack jsons, the file name corresponds to the object path under the nested namespace. this means that `data/minecraft/pmmo/blocks/stone.json` corresponds to `"minecraft:stone"`.

## Base Template
This template lists all root fields for this configuration. all fields are optional
```json
{
    "isTagFor":[],
    "xp_values":{},
    "nbt_xp_values":{},
    "requirements":{},
    "nbt_requirements":{},
    "vein_data":{
        "consumeAmount": 1
    }
}
```
the sections that follow will elaborate on each section

## "isTagFor": []
When this tag is used, the file naming convention is ignored.  Instead, the members of this array are used and all configurations in this file are applied to all of them.  For example, if we wanted to give the same xp value for breaking stone-like blocks we would define
```json
"isTagFor": [
  "minecraft:stone",
  "minecraft:granite",
  "minecraft:andesite",
  "minecraft:diorite"
]
```

## "xp_values":{} and "nbt_xp_values":{}
These setting determine what xp is given for this block for specific events.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "xp_values" looks as follows:
```json
"xp_values":{
  "BLOCK_BREAK": {
    "mining": 100,
    "excavation": 55
  },
  "BLOCK_PLACE": {
    "building": 150
  }
}
```
In this example if we break the block we will get 100 mining XP and 55 excavation XP.  if we place the block we get 150 building XP.  if we right-click the block we get nothing since it is not defined.  The NBT variant uses the same root=>Event structure however, instead of skills and experience there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Event Types For Blocks

| event          | context                                                  |
|:---------------|:---------------------------------------------------------|
| BLOCK_BREAK    | when a block is broken                                   |
| BLOCK_PLACE    | when a block is placed                                   |
| GROW           | when a crop block grows a stage                          |
| HIT_BLOCK      | when a player hits a block in the process of breaking it |
| ACTIVATE_BLOCK | when a player right-clicks a block                       |

## "requirements":{} and "nbt_requirements":{}
These settings determine whether a block is permitted to undergo a particular function/action.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "requirements" looks as follows:
```json
"requirements": {
  "BREAK": {
    "mining": 10,
    "excavation": 5
  },
  "PLACE": {
    "building": 10
  }
}
```
In this example the player must have 10 mining levels and 5 excavation levels to break this block.  They must have 10 building levels to place this block, and if the item has a right click action, there is no requirement.  The NBT variant uses the same root=>ReqType structure however, instead of skills and levels there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Req Types for Items

| requirement | action                                                  |
|:------------|:--------------------------------------------------------|
| PLACE       | the ability to place the block                          |
| BREAK       | the ability to break the block                          |
| INTERACT    | the ability to right click, such as activating a button |


## "vein_data":{}
Vein Data lets you give this block a vein consume amount.  This overrides the default in the server config when this value is defined and defines how much vein charge is used per block of this type when the vein ability is activated.
```json5
"vein_data":{
  "consumeAmount": 1  //how much charge this block consumes when veined
}
```

[Home](../home.md)