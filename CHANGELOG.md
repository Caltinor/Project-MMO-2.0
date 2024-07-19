# Changelog

## [1.20.1-1.4.23] - 2024-07-19
### Minor Version Changes
- Mob Modifiers now have two sections `global_mob_modifiers` and `mob_modifiers` which allow for specifying the operation the scaling should apply
```json
{
  "global_mob_modifiers": [
    {
      "attribute": "minecraft:luck",
      "amount": 1.5,
      "operation": "ADDITION"
    }
  ],
  "mob_modifiers": {
    "minecraft:zombie": [
      {
        "attribute": "minecraft:generic.max_health",
        "amount": 2.0,
        "operation": "MULTIPLY_BASE"
      },
      {
        "attribute": "minecraft:generic.attack_damage",
        "amount": 0.005,
        "operation": "MULTIPLY_TOTAL"
      }
    ]
  }
}
```

### Bugfixes / Tweaks
- Added built-in support for iron spellbooks damage boost perks