# Changelog

## [1.20.1-1.2.8] - 2023-10-12
### Bugfixes / Tweaks
- HOTFIX: health change not working as intended.
- Added Villager Boost Perk defaulting to Charisma
- Added toggle to turn of potion tracking.  This makes stackable potions work again.
- Updated HEALTH_CHANGE to be HEALTH_INCREASE and HEALTH_DECREASE.  the original event, HEALTH_CHANGE, will be removed in 1.20.1
- Fixed `genData withDefaults` now properly writes all objects.
- Fixed mob modifiers not applying for dimensions and biomes.
- Fixed Break speed perk only working for one configuration.  Now stacks.