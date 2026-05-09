# Changelog

## [1.21.1-2.10.43] - 2026-05-09
All changes credit to <@341258858901274646>'s PR

### Minor Version Changes-
-  Skills.json now nests skills under "skills" as the config now has a new category called "types"
  - "order" field (integer) allows setting type order in skill list
  - "skills" field (array<string>) defines which skills are in the type
  - "color" field (integer) sets the background for the group independent of the skill's own color.

### Bugfixes/Tweaks-
- Added "types" to skills which allow them to be grouped visually in the inventory side panel
- Updated inventory skill side panel to group skills by list
- Added search bar in inventory skill side panel for skill searching
- Updated inventory skill sections to better represent XP to level up.
- Added default types of "warfare", "athletics", "harvesting", "artisanry", "arcana", and "social"
 
