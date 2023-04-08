# Changelog

## [1.19.4-0.0.3] - 2023-04-08

Minimum Forge Version now 45.0.39
### Bugfixes / Tweaks
- Fixed: ClassDefNotFound error from breaking change to spawn event
- Added: default data for gravel, mushroom stems, basalt, blackstone, bricks, fungii, magma blocks, nether bricks, and netherrack
- Added: default data for Goblins & Dungeons
- Added: pmmo tags to genData
- Added: "multiplicative" boolean and "base" double values to attribute and damage_boost perks for better scaling control
- Fixed: xp values of zero displaying in tooltips
- Fixed: an IOOB exception from the stats menu
- Fixed: typo in default crossbow id of damage boost perk
- Fixed: damage_boost applies_to items now showing in the glossary

## [1.19.4-0.0.2] - 2023-03-24
### Bugfixes / Tweaks
- Fixed: crash from invalid UUID case

## [1.19.4-0.0.1] - 2023-03-22
### Major Changes
- Complete revision of the Perk System

### Minor Changes
- Damage `FROM_XXX` events now use damage type tags for classification

### Technical Notes
- This is a pre-1.20 build.  The officially supported version for 1.19 is 1.19.2.  This port is provided for addon developers and testing purposes.  You may use this in your world/packs, but no promise is made to stability.  Bug reports are appreciated by may be delayed until a 1.20 Forge release is available and pmmo is ported.
