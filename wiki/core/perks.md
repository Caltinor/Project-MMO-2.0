[Home](../home.md)

Perks are a highly configurable system that allows users to trigger certain abilities when events happen in PMMO.  While PMMO ships with a number of built in perks, the API allows other mods to add their own, which you can then configure to your liking.

*For Developers go [HERE](../api/perks.md)*

## How do perks work
When a perk is configured, it will trigger during the specified event after the custom event trigger but before XP is awarded.  This means that perks do not fire if the requirements for an event are not met, and addons can intercept and even change information relevant to perks before they fire.  Lastly perks can also add XP awards before they are given and change values of events that pmmo has permitted.

In summary, perks allow any addon maker to add custom behavior to a pmmo event that the player can configure to their liking.

## Configuring Perks
all perk settings are in a server config located in your minecraft folder under `/saves/<save name>/serverconfig/pmmo-Perks.toml`.

the basic structure of the config looks like
```toml
[Perks]
    [Perks.For_Event]
        [Perks.For_Event.EVENT_NAME]
            perk = "modid:perkid"
        [Perks.For_Event.EVENT_NAME]
            perk = "modid:otherperkid"
        [Perks.For_Event.EVENT_NAME]
            perk = "modid:perkid"
        [Perks.For_Event.OTHER_EVENT_NAME]
            perk = "modid:perkid"
```
*Note that multiple perks for the same skill have their own skillname section, and that you can have multiple skills in the same event even with the same perk.*

### Individual Perk Configurations
Perks are very dynamic and each perk can have any number of settings.  Because of this, you should consult with the perk author about what settings their perks have.  the PMMO default perk settings are shown at the bottom of this page.  Once you know what settings exist, you can define them in the config file.  for example let's use the perk `pmmo:health` which gives extra hearts to the player based on their skill level
```toml
[Perks.For_Event.EVENT_NAME.endurance]
    perk = "pmmo:attribute"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 10
```
This perk has 3 settings `attribute`, `per_level` and `max_boost`.  Because we defined this perk under the "endurance" skill, when the `EVENT_NAME` event is triggered, this perk will be given the player's endurance level.  This particular perk is then going to give the player extra hearts according to the settings.  In this case it's one half-heart per 1 level of the skill.  So at 9 endurance, the player gets 4.5 hearts.  The max boost sets a cap on how much extra health the player can get from skills.  In this case it is set at 10, or 5 hearts.

Suppose this was my configuration though
```toml
[Perks.For_Event.EVENT_NAME.endurance]
    perk = "pmmo:attribute"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 10
[Perks.For_Event.EVENT_NAME.combat]
    perk = "pmmo:attribute"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 20
```
in this case the perk is calculated based on my endurance level AND my combat level.  And combat has a higher cap.  You can start to see how perks are dynamic.

As noted above, if you want two perks for the same event and skill, you need to define them separately.  example:
```toml
[[Perks.For_Event.SKILL_UP.agility]]
    perk = "pmmo:attribute"
    attribute = "minecraft:generic.movement_speed"
[[Perks.For_Event.SKILL_UP.agility]]
    skill = "agility"
    perk = "pmmo:fireworks"
```

### Event Configuration
In the above section, we used a generic "EVENT_NAME", but you should be using actual event names.  The full list of event names can be found [HERE](https://github.com/Caltinor/Project-MMO-2.0/wiki/Award-Events#event-configuration).

It is important to know what your perks do, so that you know what events to put them under.  For example, it may not make sense to use "pmmo:jump_boost" under "BLOCK_BREAK" and have the player hop every time they break a block (as funny as that looks).

### PMMO Perks
These are all the perks PMMO provides without any addons.

- pmmo:attribute
    - changes attributes about a player, much like the command does
    - "attribute" the id of the attribute.  vanilla attributes look like "minecraft:attribute_id".  "attribute_id" can be found [HERE](https://minecraft.fandom.com/wiki/Attribute#Attributes_available_on_all_living_entities)
    - "max_boost" the highest amount this attribute can reach
    - "per_level" a multiplier per level of the skill for this attribute to be modified by
- pmmo:jump_boost
    - increases the jump height
    - "per_level" how much boost per level of the skill
    - "max_boost" the highest jump level permitted
- pmmo:breath
    - restores a portion of the health bar after the last breath bubble dissapears
    - "cooldown" the time between when this perk should apply in miliseconds
    - "per_level" how much extra breath should the player get per skill level
- pmmo:damage_boost
    - increases damage to attacks.  is duplicative to "pmmo:damage" for melee attacks.  this is the only damage booster for archery, magic, and gunslinging.
    - "per_level" how much extra damage should be dealt per skill level
    - "applies_to" a list of item IDs that should get this benefit.
- pmmo:effect
    - gives the player an effect for a specific time
    - "per_level" sets duration of the effect times the skill level
    - "modifier" sets the potion strength
    - "ambient" if true makes the particles hidden
    - "visible" if false makes the effect not show in the inventory screen
- pmmo:command
    - runs the specified command or function when triggered
    - "command" a string containing the command to be run
    - "function" the ID of the function to be run.  mutually exclusive to command and always takes precedence.

[Home](../home.md)