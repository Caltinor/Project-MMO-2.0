# Changelog

## [1.18.2-6.0.4] - 2023-04-08
### Bugfixes / Tweaks
- Added: default data for Goblins & Dungeons to prevent a crash
- Added: PMMO's tags to genData for easy adding of values
- Updated: FTB Quests compat titles to make them easier to read at a glance
- Fixed: XP Awards of zero no longer show in tooltips
- Fixed: an IOOB exception from the stat menu when tinkering with level configurations

## [1.18.2-6.0.3] - 2023-03-23
### Bugfixes / Tweaks
- Added: NBT configurations can now access blockstates via path "state{}.property"
- Added: Vein Tool Blacklist config to server config which prevents listed tools from activating vein ability.
- Added: /pmmo genData forPlayers <player selector> which generates player config files in generated_data pack.
- Updated: sub-paths in IDs now properly generate via genData
- Updated: ore configs now propagate to their respective forge tag members.
- Fixed: Curio slots now properly give bonuses for the items.
- Fixed: boats not being included in default data for sailing xp.
- Fixed: AutoValues crash case caused by caching too early
