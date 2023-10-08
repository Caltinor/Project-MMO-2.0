[Home](../home.md)

Effects are placed under the `data/namespace/pmmo/effects/` folder of your datapack.  These configurations let you define what experience is awarded to the player while the effect is active.

## Example and Format
example `data/minecraft/pmmo/effect/slowness.json`
```json5
{
  "levels": [//the level of the effect corresponds to its order in the list
      {"magic": 10}, //lvl 1 xp
      {"magic": 20}, //lvl 2 xp
      {"magic": 40}  //lvl 3 xp
  ]
}
```
In this example the slowness effect gives 10 xp in magic every half tick.  Level 2 effect gives 20 xp in magic.  Level 3 effect gives 40 xp in magic.

if you want to omit a level from getting XP, put an empty `{}` where that level's xp would have been.  Example:
```json5
{
  "levels": [//the level of the effect corresponds to its order in the list
      {"magic": 10}, //lvl 1 xp
      {},            //lvl 2 xp is nothing
      {"magic": 40}  //lvl 3 xp
  ]
}
```

If you have an enchantment from another mod, be sure to nest it under the proper namespace folder.  Unlike other areas of pmmo's config, there is no "isTagOf" for enchantments.  for example "modid:modeffect" would be under `data/modid/pmmo/effects/modeffect.json`

[Home](../home.md)