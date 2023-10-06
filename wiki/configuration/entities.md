This page details the configuration details for jsons placed under `/data/namespace/pmmo/entities/`.

## File Naming Convention
Like all datapack jsons, the file name corresponds to the object path under the nested namespace.  this means that `data/minecraft/pmmo/items/zombie.json` corresponds to `"minecraft:zombie"`.

## A Very Special Case
There is one entity type that has very special configuration and that is "minecraft:player" There are certain settings that apply to this specific file that are not applicable to any other file.  Those will be annotated below with a `<4P>`.

## Base Template
This template lists all root fields for this configuration. all fields are optional
```json
{
    "isTagFor":[],
    "xp_values":{},
    "nbt_xp_values":{},
    "requirements":{},
    "nbt_requirements":{},
}
```
the sections that follow will elaborate on each section

## "isTagFor": []
When this tag is used, the file naming convention is ignored.  Instead, the members of this array are used and all configurations in this file are applied to all of them.  For example, if we wanted to give the same xp value for killing all zombie types we would define
```json
"isTagFor": [
  "minecraft:zombie",
  "minecraft:zombie_villager",
  "minecraft:husk",
  "minecraft:drowned"
]
```

## "xp_values":{} and "nbt_xp_values":{}
These setting determine what xp is given for this entity for specific events.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "xp_values" looks as follows:
```json
"xp_values":{
  "RANGED_TO_MOBS": {
    "archery": 100
  },
  "MELEE_TO_MOBS": {
    "combat": 100
  }
}
```
In this example if we hit the zombie with a bow we will get 100 archery XP.  if we hit the zombie with a sword we get 100 combat XP.  if we Smelt the item we get nothing since it is not defined.  The NBT variant uses the same root=>Event structure however, instead of skills and experience there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Event Types For Items
```
BREED //when breeding two entities
RECEIVE_DAMAGE //when the player receives damage that isn't captured in any other received damage event
FROM_MOBS //receive damage from an entity in the pmmo:mobs tag
FROM_PLAYERS //receive damage from a player
FROM_ANIMALS //receive damage from an entity in the pmmo:animals tag
FROM_PROJECTILES //receive damage from a projectile
DEAL_MELEE_DAMAGE //deal melee damage to an entity not captured in the sub-events
MELEE_TO_MOBS //deal melee damage to an entity in the pmmo:mobs tag
MELEE_TO_PLAYERS //deal melee damage to a player
MELEE_TO_ANIMALS //deal melee damage to an entity in the pmmo:animals tag
DEAL_RANGED_DAMAGE //deal ranged damage to an entity not captured in the sub-events
RANGED_TO_MOBS //deal ranged damage to an entity in the pmmo:mobs tag
RANGED_TO_PLAYERS //deal ranged damage to a player
RANGED_TO_ANIMALS //deal ranged damage to an entity in the pmmo:animals tag
DEATH //when an entity dies
ENTITY //when the player interacts with an entity, such as village trading
RIDING //experience per tick while the player is riding the entity
SHIELD_BLOCK //when the player blocks damage with a shield
SLEEP //not implemented yet
TAMING //when the player tames a creature.
```

## "requirements":{} and "nbt_requirements":{}
These settings determine whether an entity action is permitted.  These two settings are mutually exclusive.  If you have them both it won't crash anything, but the NBT setting will always supersede the regular one.  An example default implementation for "requirements" looks as follows:
```json
"requirements": {
  "KILL": {
    "combat": 10
  },
  "RIDE": {
    "farming": 10
  }
}
```
In this example the player must have 10 combat levels kill this entity.  They must have 10 farming levels to ride this entity, and if the entity is tamed, there is no requirement.  The NBT variant uses the same root=>ReqType structure however, instead of skills and levels there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Req Types for Items
```
KILL
RIDE
TAME
BREED
ENTITY_INTERACT
```