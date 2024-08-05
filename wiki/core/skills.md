[Home](../home.md)

Skills are one of the core features of Project MMO.  They allow you to separate out how experience is collected.  Unlike vanilla experience which accumulates into one experience bar, PMMO accumulates experience into specific, named, skills.  There is no limit to the number of skills you can have, and you can name them whatever you like.

## Using Skills
To use a skill in PMMO, all you need to do is use it in a config.  That's it.

If you configure dirt to give 10 experience when broken, whatever skill name you put will be given to the player.  You can use the default "excavation". You can use "digger".  You can even put something ridiculous like "dirty hands".  No matter what you put, when the player breaks a block of dirt, they will get 10 experience in that skill.  This rule applies to anywhere that skills are used in a configuration like requirements and bonuses.

*Note that PMMO will use the skill exactly how you have written it, so beware that "combat" and "Combat" are different skills (notice the capitalization)*.

## Defining Skills Settings
Simply adding a custom skill name to a config is simple enough, but it isn't pretty.  for example if I use the skill "testing" in my configuration and do nothing else, then when i earn xp in that skill, my skill list will display "pmmo.testing" in white.  That isn't very pretty, so let's fix that.

There are nine settings you can configure for each skill: 
1. [formatted name](#formatted-name)
2. [color](#color-and-afk-penalty)
3. [afkExempt](#color-and-afk-penalty)
4. [displayGroupName](#usetotallevels-and-displaygroupname)
5. [useTotalLevels](#usetotallevels-and-displaygroupname)
6. [groupedSkills](#skill-groups)
7. [maxLevel](#maxlevel)
8. [icon](#icon-and-iconsize)
9. [iconSize](#icon-and-iconsize)

### Formatted Name
If you don't want your skill to display as "pmmo.skillname", you need to add a language entry to a resource pack and use that resource pack on your server or personal game.  Your language entry just needs to look like `"pmmo.myskillname":"My Skill Name Beautified"`.  If you are unfamiliar with creating resource packs, you can read more about them [HERE](https://minecraft.wiki/w/Resource_Pack)

### Color and AFK Penalty
By default, skills display in white.  If that is okay with you, then you don't need to do anything here.  If you do want a custom color though, You will need to add your custom skill to the `pmmo-Skills.toml` file.  This is a server config.  You will find it in your Minecraft folder under `/saves/<save name>/serverconfig/`.  You will then add an entry for your new skill that should look like:
```toml
[Skills.Entry.skillname]
    color = 16777215
    noAfkPenalty = false
```
The color is a hex color in integer form.  if you know your hex color you can get the int value [HERE](https://www.programiz.com/javascript/online-compiler/) using the following code.  the output will be the number for your config. 
```js
let color = 0xE7EE48 // put your hex after the 0x
console.log(color);
```

`noAfkPenalty` lets you decide if this skill is exempt from the afk penalty applied by the anti-cheese system.  If set to true, an afk player will not be given reduced XP in this skill if they go afk for too long.

### useTotalLevels and displayGroupName
When using skill groups from the next section, these two properties help configure how the skill group behaves.  if `useTotalLevels = true` the skill group will still use the distributed values for bonuses and xp, but requirements will instead use the sum of all member skills to see if they reach the configured threshold.  For example of req of 100 could be satisfied by 50 lvls of A, 30 lvls of B, and 20 lvls of C.  or any combination adding up to the requirement.  `displayGroupName` is an option that lets the skill group display in tooltips instead of the group members.  For very large groups, this helps keep tooltips smaller, but requires the user know what that group consists of.

### Skill Groups
Skill groups are a skill identifier that represents a group of skills.  Skill groups do not show up in the skills list.  they are only used within the configs.  For example, you might want Combat, Archery, and Endurance to be represented as one skill called "fighting_skills".  You could then use this "fighting_skills" in a requirement to say that the total of those 3 skills must equal 100 to use this item.  Here is an example configuration
```toml
[Skills.Entry.fighting_skills]
    noAfkPenalty = false

    [Skills.Entry.fighting_skills.groupFor]
        combat = 0.5
        endurance = 0.3
        archery = 0.2
```
In this example the config shows "fighting_skills" as being 50% combat, 30% endurance and 20% archery.  So if a requirement is set to fighting_skills 100, what displays in-game is combat 50, endurance 30, and archery 20.

additionally, you can use groups in bonuses and the proportion is divided so.  If fighting_skills is set to 2.0 xp bonus, then combat gets a 50% bonus, endurance 30% and archery 20%.

When experience is earned for a skill group, the experience is divided among all grouped skills according to their proportion.

*Technical Note: You do not need to force your values to add up to 1.  PMMO will scale your values for you.*

### maxLevel
sets the max level for this specific skill.  Unlike the global max level, this skill will not increase even if XP is earned in the skill after reaching max level.

### icon and iconSize
When viewing skills in the inventory menu, an icon of your choosing will display.  Any icon in the resources of your world can be used.  This includes resources added via resourcepack.  simply put the ID of the resource as the icon property.

Icon size lets you use icons other than 16x16 dimensions.  By telling pmmo what the side length of the icon is, it can scale the icon to the size needed for the screen.

[Home](../home.md)