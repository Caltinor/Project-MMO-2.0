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
        [[Perks.For_Event.EVENT_NAME]]
            perk = "modid:perkid"
        [[Perks.For_Event.EVENT_NAME]]
            perk = "modid:otherperkid"
        [[Perks.For_Event.OTHER_EVENT_NAME]]
            perk = "modid:perkid"
```
*Note that an event can have multiple of the same perk with different configuration, such as to apply to different skills.*

### Individual Perk Configurations
Perks are very dynamic and each perk can have any number of settings.  Because of this, you should consult with the perk author about what settings their perks have.  the PMMO default perk settings are shown at the bottom of this page.  Once you know what settings exist, you can define them in the config file.  for example let's use the perk `pmmo:health` which gives extra hearts to the player based on their skill level
```toml
[[Perks.For_Event.EVENT_NAME]]
    perk = "pmmo:attribute"
    skill = "endurance"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 10
```
This perk has 4 settings `skill`, `attribute`, `per_level` and `max_boost`.  When the `EVENT_NAME` event is triggered, this perk will use the player's endurance level.  This particular perk is then going to give the player extra hearts according to the settings.  In this case it's one half-heart per 1 level of the skill.  So at 9 endurance, the player gets 4.5 hearts.  The max boost sets a cap on how much extra health the player can get from skills.  In this case it is set at 10, or 5 hearts.

Suppose this was my configuration though
```toml
[[Perks.For_Event.EVENT_NAME]]
    perk = "pmmo:attribute"
    skill = "endurance"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 10
[[Perks.For_Event.EVENT_NAME]]
    perk = "pmmo:attribute"
    skill = "combat"
    attribute = "minecraft:generic.max_health"
    per_level = 1
    max_boost = 20
```
in this case the perk is calculated based on my endurance level AND my combat level.  And combat has a higher cap.  You can start to see how perks are dynamic.

As noted above, if you want two perks for the same event and skill, you need to define them separately.  example:
```toml
[[Perks.For_Event.SKILL_UP]]
    perk = "pmmo:attribute"
    skill = "agility"
    attribute = "minecraft:generic.movement_speed"
[[Perks.For_Event.SKILL_UP]]
    skill = "agility"
    perk = "pmmo:fireworks"
```

### Event Configuration
In the above section, we used a generic "EVENT_NAME", but you should be using actual event names.  The full list of event names can be found [HERE](events.md).

It is important to know what your perks do, so that you know what events to put them under.  For example, it may not make sense to use "pmmo:jump_boost" under "BLOCK_BREAK" and have the player hop every time they break a block (as funny as that looks).

### Default Properties
All perks are affected by the following properties.  The below table lists them and the effect it will have on your perk.

| property      | effect                                                                                                                                                                                                                                                                 |
|:--------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `cooldown`    | prevents this perk from activating until the duration has elapsed.  Note: this applies to all versions of this perk, not just the perk for that configuration.  For example a firework on level-up would not fire for the second skill if the cooldown had not elapsed |
| `chance`      | a value between 0.0 and 1.0 for the likelihood this perk will execute.  0.1 = 10%                                                                                                                                                                                      |
| `skill`       | specifies a skill to supply to this perk. When this is used, the player's level in the skill will be passed to the perk when executed.                                                                                                                                 |
| `min_level`   | the minimum level in the specified skill the player must have for this perk to execute.  omitting the skill will negate this property                                                                                                                                  |
| `max_level`   | the maximum level in the specified skill the player must have for this perk to execute.  omitting the skill will negate thsi property                                                                                                                                  |
| `per_x_level` | Executes the perk only if the player's level is divisible by this value.  omitting the skill will negate this property                                                                                                                                                 |
| `milestones`  | A list of levels that this perk executes for.  omitting the skill will negate this property                                                                                                                                                                            |

### PMMO Perks
These are all the perks PMMO provides without any addons.  The defaults listed below are the values if you omit the property in your configuration.  By using a property in your `pmmo-Perks.toml`, the default is ignored.

### <u>pmmo:break_speed</u>
Modifies the break-speed of blocks.  Only works with the `BREAK_SPEED` event.

| property      | default | description                                                  |
|:--------------|:-------:|:-------------------------------------------------------------|
| `pickaxe_dig` |    0    | how much faster pickaxes should break blocks per skill level |
| `axe_dig`     |    0    | how much faster axes should break blocks per skill level     |
| `shovel_dig`  |    0    | how much faster shovels should break blocks per skill level  |
| `hoe_dig`     |    0    | how much faster hoes should break blocks per skill level     |
| `shears_dig`  |    0    | how much faster shears should break blocks per skill level   |
| `sword_dig`   |    0    | how much faster swords should break blocks per skill level   |             
| `max_boost`   |  none   | see above.                                                   |  

### <u>pmmo:fireworks</u>
Shoots a firework into the air above the player.  Only works with the `SKILL_UP` event.

| property | default  | description                                  |
|:---------|:--------:|:---------------------------------------------|
| `skill`  | `"none"` | The skill this firework should activate for. |

### <u>pmmo:attribute</u>
Gives the player an attribute modifier.  This works with modded attributes.  For the full list of vanilla attribute IDs, go [HERE](https://minecraft.wiki/w/Attribute#Attributes)

| property         | default | description                                                                          |
|:-----------------|:-------:|:-------------------------------------------------------------------------------------|
| `attribute`      |  none   | The attribute ID being modified                                                      |
| `max_boost`      |    0    | see above.                                                                           |
| `per_level`      |    0    | multiplies the player's skill by this value to set the attribute value               |
| `base`           |    0    | base value added to attribute value on top of `per_level` value                      |
| `multiplicative` |  false  | if true uses the attribute MULTIPLY operation. If false, uses the ADDITION operation |

### <u>pmmo:temp_attribute</u>
Gives the player an attribute for a specific duration, then removes it.

| property         | default | description                                                                          |
|:-----------------|:-------:|:-------------------------------------------------------------------------------------|
| `attribute`      |  none   | The attribute ID being modified                                                      |
| `duration`       |    0    | How long the attribute should last.  Default would be an instantaneous removal       |
| `max_boost`      |    0    | see above.                                                                           |
| `per_level`      |    0    | multiplies the player's skill by this value to set the attribute value               |
| `base`           |    0    | base value added to attribute value on top of `per_level` value                      |
| `multiplicative` |  false  | if true uses the attribute MULTIPLY operation. If false, uses the ADDITION operation |


### <u>pmmo:jump_boost</u>
Gives the player a vertical boost.  If used with the JUMP event, will add to the jump, otherwise the perk will just launch the player into the air a certain amount.

| property    | default | description                                              |
|:------------|:-------:|:---------------------------------------------------------|
| `max_boost` |  0.25   | The maximum height added to the jump                     |
| `per_level` | 0.0005  | How much jump height each level of the skill contributes |
| `base`      |    0    | A flat amount to add to the jump amount                  |

### <u>pmmo:breath</u>
Restores a portion of the breath bar after the last breath bubble dissapears

| property    | default | description                                                                     |
|:------------|:-------:|:--------------------------------------------------------------------------------|
| `cooldown`  |   600   | How frequent the breath can refresh.  setting to zero will make breath infinite |
| `per_level` |    1    | The amount of breath restored per level in the skill                            |
| `base`      |    0    | A flat amount to add to the amount of breath restored~~~~                           |

### <u>pmmo:damage_reduce</u>
Reduces damage received when used with the `RECEIVE_DAMAGE` event.

| property         |   default   | description                                                                                                                                           |
|:-----------------|:-----------:|:------------------------------------------------------------------------------------------------------------------------------------------------------|
| `per_level`      |    0.025    | Amount of damage per level in the specified skill to reduce by                                                                                        |
| `for_damage`     | `"omitted"` | a Minecraft [Damage Type](../configuration/damagetypes.md) this reduction should apply to.  If using damage type tags, place a `#` before the tag ID. | 
| `max_boost`      |     n/a     | limits the max reduction that can be achieved                                                                                                         |
| `base`       |      0      | A flat amount added to the per_level amount for the damage reduced                                                                                    |

### <u>pmmo:damage_boost</u>
Increases damage to attacks.  This is the only damage booster for archery, magic, and gunslinging.  Note that melee attack boosting should use `pmmo:attribute` and use the vanilla attack damage attribute.

| property         |     default     | description                                                                                                                                                                                                                                                |
|:-----------------|:---------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `applies_to`     | `["weapon:id"]` | Specifies which weapons will have boosted damage. The default is no weapons                                                                                                                                                                                |
| `per_level`      |      0.05       | How much per, level in the skill will the damage be boosted by.  Omitting the skill in this perk will result in no damage boosted                                                                                                                          |
| `base`           |        1        | Since damage is a multiplier by default, the base is added to make damage equal to at least 1x the base damage.  You could set this to zero to make players deal less damage before they reach a certain skill level, or if using `multiplicative = false` |
| `multiplicative` |      true       | Makes damage a multiplier.  If false, adds to the damage by a flat amount                                                                                                                                                                                  |

### <u>pmmo:effect</u>
Gives the player an effect for a specific time

| property    | default | description                                                                 |
|:------------|:-------:|:----------------------------------------------------------------------------|
| `duration`  |   100   | The number of ticks the effect should last per level                        |
| `per_level` |    1    | multiplies the level by this value before multiplying by duration           |
| `modifier`  |    0    | sets the level of the effect.  all effect levels are +1 than their modifier |
| `ambient`   |  false  | should an effect's particles be hidden                                      |
| `visible`   |  true   | should an effect show in the players inventory and hud                      |
| `max_boost` |   n/a   | the number of ticks the effect's duration should be limited to              |
| `base`      |    0    | A flat amount to add to the duration                                        |

### <u>pmmo:command</u>
Runs the specified command or function when triggered.  Note the Glossary entry for this perk will display the property literally.  It is advised to name your functions something intuitive, if using functions.

| property   | default | description                                                               | Example                                   |
|:-----------|:-------:|:--------------------------------------------------------------------------|:------------------------------------------|
| `command`  |  none   | Runs a single command line                                                | `command = "give @s minecraft:diamond 10` |
| `function` |  none   | Executes a function.  takes precedence over `command` if both are present | `function = "mypack:myfunction"`          |

### <u>pmmo:villager_boost</u>
Reduces the villager's trades when interacted with.  Only works with the `ENTITY` event.

| property    | default | description                                                          |
|:------------|:-------:|:---------------------------------------------------------------------|
| `per_level` |  0.05   | the amount of reputation to increase per level in the skill          |
| `cooldown`  |  1000   | the number of ticks before another reputation modification can occur |
| `base`      |    0    | A flat amount to add to the reputation                               |

### <u>pmmo:tame_boost</u>
Increases the attributes of the tamed animal.  Only works with the `TAMING` event.  Currently, all attributes modified are hardcoded to these values.

| attribute     | value |
|:--------------|:-----:|
| jump strength | 0.005 |
| health        |  1.0  |
| speed         | 0.01  |
| armor         | 0.01  |
| damage        | 0.01  |

| property    | default | description                                                                       |
|:------------|:-------:|:----------------------------------------------------------------------------------|
| `per_level` |  0.05   | Each attribute is increased by this value * the skill * the attribute value above |
| `skill`     | taming  | a default skill provided                                                          |

[Home](../home.md)
