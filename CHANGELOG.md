# Changelog

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
