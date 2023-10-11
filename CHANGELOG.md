# Changelog

## [1.20.1-1.2.7] - 2023-10-11
### Bugfixes / Tweaks
- Added Villager Boost Perk defaulting to Charisma
- Added toggle to turn of potion tracking.  This makes stackable potions work again.
- Updated HEALTH_CHANGE to be HEALTH_INCREASE and HEALTH_DECREASE.  the original event, HEALTH_CHANGE, will be removed in 1.20.1
- Fixed `genData withDefaults` now properly writes all objects.
- Fixed mob modifiers not applying for dimensions and biomes.
- Fixed Break speed perk only working for one configuration.  Now stacks.