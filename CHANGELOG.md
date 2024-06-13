# Changelog

## [1.21.0-2.3.4] - 2024-06-13
### Preface
A lot has changed with Project MMO since 1.20.1.  This changelog serves as both a list of changes and a primer for updating to the new version.  

One major detail is the dropped support for Forge.  Project MMO will now only be releasing on NeoForge going forward.  NeoForge as a platform has better adapted to the community's needs and has shown greater agility in making relevant changes to the codebase that will keep modding fun and easy to work with into the future.  Additionally, NeoForge exists because of the toxicity of LexManos who runs Forge and this is my part in rejecting his toxicity.  My apologies for any inconvenience caused by other mods that have not yet made the switch to NeoForge and the impact that has on your modpacks.

### Major Changes
- Block Placed History and Vein Data moved to Data Attachments
- XP formulas have been revamped

### Minor Changes
- Default Data moved to optional Feature Packs
- Server Configs now live in `/data/pmmo/config/` and can be generated via the genData command

### Bugfixes / Tweaks
- Added TOOL_BREAKING event which fires when an item with durability breaks.
- Added new feature packs for Easy, Default, and Hardcore.
- Added damage type awareness for anticheese configurations
- Fixed damage sources not having damage type configuration options

[Full Technical Breakdown of Changes](https://github.com/Caltinor/Project-MMO-2.0/blob/main/UpdatePrimer.md)
