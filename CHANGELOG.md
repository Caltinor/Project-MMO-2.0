# Changelog

## [1.19.2-3.1.1] - 2023-06-09
### Bugfixes / Tweaks
- Fixed typo in archery damage boost perk
- Fixed low-hardness blocks giving zero XP
- Fixed Swords having farming wear req
- Fixed Taming and Hunter skills having missing skill textures
- Fixed crash in glossary from items with special construction
- Updated glossary to now include variants like the creative menu
- Updated genData command to work like a builder and added new options.
  - "begin" resets your settings
  - "disabler" makes the pack.mcmeta a disabler 
  - "withOverride" still sets the override property to true
  - "withDefaults" uses current settings + AutoValues to fill files with what they show in game
  - "forPlayers" lets you select players to have player configs generated
  - "simplified" removes all unused sections from files to make them cleaner
  - "create" builds the datapack based on current settings