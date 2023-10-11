[Home](../home.md)

Perks are an extensible feature of Project MMO that allows other mods and addons to register custom behavior that players can then configure.

### A Basic implementation
At its core, a Perk is just special behavior that players can configure to invoke under specific conditions.  They configure them under events, then define properties to alter the behavior to their liking.  The level and breadth of configuration depends on the perk implementation, though, there are some defaults that the perk system itself utilizes.

|Property| Purpose                              |
|:---|:-------------|
|BiPredicate<Player, CompoundTag> conditions| An extra check, above the built-in properties, to determine if this perk should start, continue ticking, or even execute its stop behavior |
|CompoundTag propertyDefaults| Default configuration properties. Use this to ensure you always have a value in case users omit them  |
|BiFunction<Player, CompoundTag, CompoundTag> start| Takes the player and the perk configuration and returns a compound that the event can consume |
|TriFunction<Player, CompoundTag, Integer, CompoundTag> tick| Takes the player, the perk configuration, and the number of ticks elapsed and returns a compound with properties you want to persist.  These properties are not used elsewhere.  the tick behavior is defined by the `duration` property.  If left at the default of zero, the tick behavior will be skipped. |
|BiFunction<Player, CompoundTag, CompoundTag> stop| Takes the player and the perk configuration and returns a compound that isn't used at all.  Executes after the last tick (which may be zero ticks)|
|MutableComponent description|Used in the glossary to describe your perk|
|BiFunction<Player, CompoundTag, List<MutableComponent>> status|Takes the player and perk configuration and returns text lines to be printed in the glossary.  The difference with this property is that you are given the real-time player and configuration which you can use to calculate the perk's attributes and give detailed information.  whereas the description is static|


## Registering a Perk
in order for players to be able to use your perk in their configurations, you need to register your perk during `FMLCommonSetupEvent` using `harmonised.pmmo.api.APIUtils.registerPerk`.  There are three parameters to this registration:

|Property| Purpose|
|:---|:-------------------------| 
|ResourceLocation perkID| a unique identifier for your perk.  This will be used by players to reference your perk.  eg `perk = "pmmo:jump_boost"` |
|Perk perk| A perk instance whose properties are invoked when a perk executes.  Note that the same instance is used for all perks, so any static or locally stored variables will persist for all players and all executions.|
|PerkSide side| whether this should fire on the `CLIENT`, `SERVER`, `BOTH`.  If your implementation varies based on side, it might be good to register a perk for each side using the same identifier. a good example of a dual-implementation both-sided perk is the [pmmo jump perk](../../src/main/java/harmonised/pmmo/core/perks/FeaturePerks.java) which does different things on each side, but both are needed for the perk to behave correctly. |

## Technical Notes
1. **Perks are skill agnostic:** This means that you are not meant to know what skill your perk is being configured for.  You may think this is a combat perk, but I want to use it with my custom zombie_hugging skill.  However, if a player supplies a skill in the configuration via `skill = "skillname"`, PMMO will grab the player's skill level for that skill and include it and the skill name in the configuration that your executions are provided. 
2. **Make your settings optional:** you should assume that players won't look up your settings.  This is why `defaults` exists as a property in the perk itself.  Use this to ensure you always have a value.  This also allows you to have balanced defaults so that users need less writing to get a fun perk into their game.
3. **Server Perks:** Any perk you register only server-side does not need to be present on the client.  This can allow you to have proprietary perks on your server that the client doesn't need.  Note that there are limitations related to latency and rendering that you need to factor in.

## XP Award Maps
Most events allow perks to set the initial values of the xp awards before other award logic is executed.  to provide PMMO with a custom map, use the method `APIUtils.serializeAwardMap(Map<String, Long> awardMap)` to get a `ListTag` object representing your map and add that to your output CompoundTag using the key `APIUtils.SERIALIZED_AWARD_MAP`.

### Event Reference
Except for the EventTypes listed below, all perk registrations will receive no additional inputs and only utilize xpAwardMap outputs.  Only the config settings will be passed to your perk and outputs will be pointless.

***Note: All tag keys are referenced in `APIUtils.class`***

- BREAK_SPEED
    - -> BREAK_SPEED_INPUT_VALUE = the original break speed
    - -> BLOCK_POS = the position of the block being broken
    - <- BREAK_SPEED_OUTPUT_VALUE = the new speed to be set.
- DEAL_MELEE_DAMAGE, MELEE_TO_MOBS, MELEE_TO_ANIMALS, MELEE_TO_PLAYERS, DEAL_RANGED_DAMAGE, RANGED_TO_MOBS, RANGED_TO_ANIMALS, RANGED_TO_PLAYERS
    - -> DAMAGE_IN = the original damage supplied by the event
    - <- DAMAGE_OUT = the new damage to be applied
- RECEIVE_DAMAGE, FROM_PLAYERS, FROM_ENVIRONMENT, FROM_IMPACT, FROM_MAGIC, FROM_MOBS, FROM_ANIMALS, FROM_PROJECTILES
    - -> DAMAGE_IN = the original damage supplied by the event
    - <- DAMAGE_OUT = the new damage to be applied
- ENCHANT (server-side only)
    - -> STACK = the item being enchanted
    - -> PLAYER_ID = the UUID of the player enchanting
    - -> ENCHANT_LEVEL = the level of the enchantment
    - -> ENCHANT_NAME = the name of the enchantment
- SMELT
    - -> STACK the serialized itemstack being smelted/cooked
- JUMP, SPRINT_JUMP, CROUCH_JUMP
    - <- JUMP_OUT = an arbitrary value that the xp is scaled off of.  does not actually affect jump height unless done in the perk itself.
- SHIELD_BLOCK
    - -> DAMAGE_IN = damage that is being blocked

## Note To Devs
It is incredibly easy to add extra inputs and outputs to this system and has negligible performance impacts. If there is anything you would like added to any event for a custom perk you are building, contact me on the discord or open an issue here.  I will happily add them.

[Home](../home.md)