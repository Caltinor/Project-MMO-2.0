[Home](../home.md)

This page details the configuration details for JSON files placed under `/data/namespace/pmmo/entities/`.

## File Naming Convention
Like all datapack jsons, the file name corresponds to the object path under the nested namespace.  this means that `data/minecraft/pmmo/entities/zombie.json` corresponds to `"minecraft:zombie"`.

## Base Template
This template lists all root fields for this configuration. all fields are optional
```json
{
    "isTagFor":[],
    "xp_values":{},
    "nbt_xp_values":{},
    "requirements":{},
    "nbt_requirements":{},
    "dealt_damage_xp": {},
    "received_damage_xp": {}
}
```
the sections that follow will elaborate on each section

## "isTagFor": []
When this tag is used, the members of this array will have the configurations in this file copied to all of them.  For example, if we wanted to give the same xp value for killing all zombie types we would define
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
  "SHIELD_BLOCK": {
    "endurance": 100
  },
  "DEATH": {
    "slayer": 100
  }
}
```
In this example if we block the zombie damage with a shield we will get 100 endurance XP.  if we kill the zombie we get 100 slayer XP.  The NBT variant uses the same root=>Event structure however, instead of skills and experience there is an entirely different structure.  You can read about that structure [HERE](https://github.com/Caltinor/PMMO-and-NBT-Compat/wiki/Config-Structure-Overview)

### Valid Event Types For Items

| event        | context                                                              |
|:-------------|:---------------------------------------------------------------------|
| BREED        | when this entity is bred                                             |
| DEATH        | when this entity dies                                                |
| ENTITY       | when the player interacts with this entity, such as villager trading |
| RIDING       | experience per tick while the player is riding this entity           |
| SHIELD_BLOCK | when the player blocks damage with a shield dealt by this entity     |
| TAMING       | when the player tames this entity.                                   |


## "dealt_damage_xp":{} and "received_damage_xp"
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

| requirement     | context                                                           |
|:----------------|:------------------------------------------------------------------|
| KILL            | dealing any damage to the entity                                  |
| RIDE            | mounting the entity                                               |
| TAME            | successfully taming.  note this does not prevent attempts         |
| BREED           | breeding two of this entity.  note this does not prevent attempts |
| ENTITY_INTERACT | right-clicking the entity                                         |

## A Very Special Case
There is one entity type that has very special configuration and that is "minecraft:player".  Settings defined in this file will affect PVP and will govern how interactions between players are handled.  Note that the more common interaction settings are governed in `pmmo-server.toml` and it may not be necessary to generate or edit this file.

[Home](../home.md)