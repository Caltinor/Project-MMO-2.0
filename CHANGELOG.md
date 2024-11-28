# Changelog

## [1.20.1-1.5.27] - 2024-11-28
### Minor Version changes
- Updated party xp ratio config property to have per-skill ratios

## [1.20.1-1.5.26] - 2024-11-28
### Minor version changes
- Added Scripting Feature.  [See wiki for more details](https://github.com/Caltinor/Project-MMO-2.0/blob/1-20-1/wiki/configuration/scripting.md)
  - Scripting overwrites datapack configurations and may impact how your configurations behave.  There are no default scripts shipped with the mod, so there will be no immediate impact on existing datapacks.

### Bugfixes / Tweaks
- Added tag `pmmo:gun` with default entries for weapon grouping
- Added damage boost perk to default config for guns
- Added new property `showInList` to skills to hide them from the skill list overlay
- Added new list property `for_damage` to damage boost perk which filters damage types
- Updated party range config to accept `-1` and `-2` as values to allow same dimension and any-dimension range to party xp respectively
- Fixed AutoValue config's enable/disable settings not working
- Fixed tooltips showing xp on items when value is zero 