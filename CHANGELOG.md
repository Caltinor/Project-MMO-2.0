# Changelog

## [1.19.2-3.0.4]
### Bugfixes / Tweaks
- Added: `pmmo:skill_up` trigger for advancements

## [1.19.2-3.0.3]
### Bugfixes / Tweaks
- Added: NBT configurations can now access blockstates via path "state{}.property"
- Added: Vein Tool Blacklist config to server config which prevents listed tools from activating vein ability.
- Added: `/pmmo genData forPlayers <player selector>` which generates player config files in `generated_data` pack.
- Updated: sub-paths in IDs now properly generate via genData
- Updated: ore configs now propagate to their respective forge tag members.
- Fixed: Curio slots now properly give bonuses for the items.
- Fixed: boats not being included in default data for sailing xp.
- Fixed: AutoValues crash case caused by caching too early
