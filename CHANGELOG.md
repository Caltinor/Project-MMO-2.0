# Changelog

## [1.19.4-0.2.10] - 2023-10-12
### Minor version changes
- Updated wiki to be part of the repo.
- Updated HEALTH_CHANGED event to HEALTH_INCREASE and HEALTH_DECREASE event.  HEALTH_CHANGED is deprecated for removal in 1.21
- Added admin command to ignore reqs.
- Added hover behavior to inventory skill list to show xp to next level.
- Added API hooks for player XP maps
- Added Villager perk
- Fixed explosions now respect break and kill reqs
- Fixed Perk stop behavior
- Fixed edge case to compound NBT qualifiers
- Fixed `genData withDefaults` not including data from non-items
- Fixed mob modifiers not applying correctly.
- Fixed break speed perks not stacking when multiple are configured for the same tool.