# Changelog

## [1.21.1-2.5.15] - 2024-11-29
### Minor Version Changes
- Party Config is now a map of skills and bonuses.
  - easy pack config now provides 50% bonus on all skills
  - default pack gives 5% for combat and 10% for endurance
- Default data moved to datagen, some values changed to balance them
- Vein base capacity and recharge are now player attributes.  The config setting has been removed
  - The easy pack now includes an attribute perk to give players the previous minimum values after gaining any mining xp
    - players who generated configs will not see this change reflected in their files
  - This change reflects a general design choice to move more behavior to attributes
### Bugfixes/Tweaks
- Fixed `negative_effect` script keyword applying to positive effects
- Fixed AutoValue Enable/Disable settings not working
- Fixed an issue with item ID lookups, which included some codebase cleanup on lookups in general
- Fixed the new break speed toggle breaking on dedicated servers
- Added `pmmo:gun` damage type tag, default values from common mods, and a corresponding perk.
- Added `showInList` property to skills which, when set to false, will remove it from the overlay list
- Added `for_damage` list property to damage boost perk which further filters the perks application on damage type
- Added Curios Continuation compat

