# Changelog

## [1.19.4-0.2.9] - 2023-08-31
### Minor version changes
- NBT Configs without override will now stack
- Reworked damage dealt and received events to work with DamageTypes
    - This completely deprecates all configurations that used the old `FROM_MOBS`, `RANGED_TO_ANIMALS`, etc events.  The new configuration adds `dealt_damage_xp` and `received_damage_xp` as root properties in item and entity configurations.  see the following example:
```json
"dealt_damage_xp": {
  "minecraft:player_attack": {"combat": 10},
  "#minecraft:is_projectile": {"archery": 10}
}
```
- This configuration supports `#` to denote tags allowing you to group your damage types with vanilla's damage tags.
- The server config now has a new damage type section, which uses these damage types.  Note that this is a fallback setting which means if the server config has a setting for a damage type and the entity does not, the server config will be used in its absence.
- default data for entities has been completely removed (minus boats)
- `mob_multiplier` in dimension and biome configs renamed to `mob_modifier`
    - this was to accont for the fact that dim/biome settings are additive not multiplicative as the old name implied.
    - Unlike mob scaling, dim and biome modifiers apply regardless of the level or proximity of the player, but they do combine with those settings if applicable.
### Bugfixes / Tweaks
- NBT configs with qualified compounds no longer NPE
- Submerged event now fires in all water contexts.
- Biome and Dimension mob modifiers now work
- `withPlayers` was uninentionally disabled and is now reenabled
- Archery skill boost added to leather armor
- perks which used duration no long run indefinitely
- replaced temp_attribute perk assigned to sprinting with a speed effect in default perk config.