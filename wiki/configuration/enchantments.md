[Home](../home.md)

Enchantments are placed under the `data/namespace/pmmo/enchantments/` folder of your datapack.  These configurations let you define what experience is required to use specific enchantments regardless of the item it is on.

## Example and Format
example `data/minecraft/pmmo/enchantments/unbreaking.json`
```json5
{
  "levels": [ //the level of the enchantment corresponds to its order in the list
      {"mining": 10, "smithing": 5}, //lvl 1 enchantment
      {"mining": 20, "magic": 5},    //lvl 2 enchantment
      {"magic": 40}                  //lvl 3 enchantment
  ]
}
```
In this example the unbreaking enchantment requires 10 levels in mining and 5 levels in smithing to use.  Level 2 unbreaking requires 20 levels in mining and 5 levels in magic.  Level 3 Unbreaking requires 40 levels in magic.

if you want to omit a level from having a requirement, put an empty `{}` where that level's xp would have been.  Example:
```json5
{
  "levels": [//the level of the effect corresponds to its order in the list
      {"mining": 10, "smithing": 5}, //lvl 1 requirement
      {},            //lvl 2 requirement is nothing
      {"magic": 40}  //lvl 3 requirement
  ]
}
```

If you have an enchantment from another mod, be sure to nest it under the proper namespace folder.  Unlike other areas of pmmo's config, there is no "isTagOf" for enchantments.  for example "thermal:holding" would be under `data/thermal/pmmo/enchantments/holding.json`

[Home](../home.md)