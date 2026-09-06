# Changelog

## [26.2.0-2.9.38] - 2026-09-06
### New `/pmmo config mod_settings` Command
The command-based config feature has been expanded to include server configs.

### Minor Version Impact
in order to distinguish commands such as "enabled" amongst features like vein miner and mob scaling some keywords have been updated.  this will affect scripting and configs as the names of properties will change.

| Change                                 | Impact              |
|:---------------------------------------|:--------------------|
| `per_level` -> `xp_per_level`          | scripts & datapacks |
| `enabled` -> `mob_scale_enabled`       | scripts & datapacks |
| `base_level` -> `mob_scale_base_level` | scripts & datapacks |
| `per_level` -> `mob_scale_per_level`   | scripts & datapacks |
| `power_base` -> `mob_scale_power_base` | scripts & datapacks |
| `enabled` -> `vein_enabled`            | datapacks only      |

*Vein enabled only impacts datapacks because scripting added a prefix during parsing by default, so the change here only changes how the property appears in datapacks*

### Technical notes
- The pack generated this way behaves like a datapack which uses `withOverrides`, `withDefaults`, plus updates the setting in the command
- You can have `generated_data` and `command_pack` at the same time, and they will apply their respective override rules accordingly.




