[Home](../home.md)

Project MMO lets you restrict content based on player skill levels.  These are referred to as "requirements".  If a requirement is not met for a certain event, the event does not occur.  A simple example of this is having a requirement in "Mining" to be able to use a stone pickaxe.  If you craft/obtain a stone pickaxe before you have the required level, you won't be able to use the tool to mine.  The following sections will go into detail about requirements


## Requirement Configuration
The below table contains every requirement type in the mod as well as what it does.

|Requirement| Purpose                                                                                          |
|:---|:-------------------------------------------------------------------------------------------------|
|**WEAR**| requirement to put into an armor/curio slot or to hold in one's hand or offhand.                 |
|**USE_ENCHANTMENT**| for enchantments, this dictates if an item possessing the enchantment can be used in any context |
|**TOOL**| can the item be used for breaking blocks                                                         |
|**WEAPON**| can the item be used to deal damage                                                              |
|**USE**| if the item has some sort of use, like food, can the player perform that use?                    |
|**PLACE**| can this block be placed                                                                         |
|**BREAK**| can this block be broken                                                                         |
|**BIOME**| can the player enter this biome. if not, apply a negative status effect                          |
|**KILL**| can the player deal damage or kill this entity                                                   |
|**TRAVEL**| can the player enter this dimension                                                              |
|**RIDE**| can the player ride this vehicle/animal                                                          |
|**TAME**| can the player tame this animal                                                                  |
|**BREED**| can the player breed this animal                                                                 |
|**INTERACT**| can the player interact with this block                                                          |
|**ENTITY_INTERACT**| can the player interact with this entity                                                         |

## Requirement Negative Effects
There are two requirements that can trigger negative effects: `WEAR` and `BIOME`.  These are effects that are given to players who try to hold/wear an item they can't use or enter biome they aren't skilled enough for.  

[Home](../home.md)