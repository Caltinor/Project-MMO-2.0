[Home](../home.md)

For those familiar with PMMO's features called "extra chance", "rare mob drops", "rare fish loot", and "treasure", This section covers how PMMO 2.0 implements those same features.  Instead of individual configs, you now have 4 new PMMO-related conditions you can use in loot tables and Global Loot Modifiers to define drops.  These conditions let you set specific any source of loot (entities, blocks, chests) to be conditional on a player's skill.  The most basic conditions are `pmmo:skill_level` and `pmmo:skill_level_kill`.  The latter works for kills, while the former works on everything else, including chest loot.  That's right, you can make chests give different loot based on a player's skills.  the format for this condition in the loot table is:
```json5
{"condition":"pmmo:skill_level",
 "skill":"skillname",  //required
 "level_min": 10,      //optional
 "level_max": 20       //optional
}
```
*leaving out both optional keys is pointless since the condition will always be true in that case.*

The third condition is `pmmo:highest_skill` which returns true only if the target skill is the highest of those it's compared to.
```json5
{"condition":"pmmo:highest_skill",
 "target_skill":"skillname",
 "comparable_skills": ["skill1", "skill2"]
}
```
All keys are required.  The comparable skills are those that the target skill is compared against.  if the target skill has the highest level of all the comparables, the condition will return true.  You can use this to give different items based on a player's highest skill.  for example a strong bow for an archer, but a strong sword to a combatant.  Or a pickaxe to a miner, an axe to a lumberjack, and a shovel to an excavator.

the forth condition is `pmmo:valid_block` which checks if the block broken is the correct one.
```json5
{"condition":"pmmo:valid_block",
 "tag":"forge:stone"
}
```
or
```json5
{"condition":"pmmo:valid_block",
 "block":"minecraft:stone"
}
```
"tag" and "block" are mutually exclusive. If you do use both, however, the tag always takes precedence and the block is ignored.  In the above example this loot condition makes it so that if any member of the forge stone tag is broken, this loot pool or GLM will be applied.  In the second example this only applies specifically to minecraft's stone block.

[Home](../home.md)