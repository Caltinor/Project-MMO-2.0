# Changelog

## [26.1.2-2.8.34] - 2026-07-18
### Reimplemented Break_Speed perks
Since Mojang got rid of tool tiers, we had to update to use tags.  So the new format matches item tags to block tags.  The examples below show specifics, but here is what it looks like as a schema

```json
"ratios": {
  "item_tag_that_tool_belongs_to" : {
    "block_tag_that_speed_should_be_applied_to": 0.000 //the speed per level      
  } 
}
```

old format
```json
{
  "perk": "pmmo:break_speed",
  "pickaxe_dig": 0.005,
  "skill": "mining"
}
```
new format
```json
{
  "perk": "pmmo:break_speed",
  "ratios": {
    "minecraft:pickaxes": {
      "minecraft:mineable/pickaxes": 0.005
    }
  },
  "skill": "mining"
}
```


