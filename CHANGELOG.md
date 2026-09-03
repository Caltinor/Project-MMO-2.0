# Changelog

## [1.20.1-1.7.42] - 2026-09-03
### New `/pmmo config` Command
A new command has been added allowing you to make persistent edits to your world straight from the chat menu.

`/pmmo config` creates files one at a time in a new datapack called "command_pack".  Here are a few examples:
- `/pmmo config blocks minecraft:dirt xp BLOCK_BREAK set excavation 1`
- `/pmmo config items minecraft:cobblestone salvage set minecraft:iron_nugget base_chance 0.01`
- `/pmmo config dimension minecraft:overworld bonuses DIMENSION clear`

### Technical notes
- The pack generated this way behaves like a datapack which uses `withOverrides`, `withDefaults`, plus updates the setting in the command
- You can have `generated_data` and `command_pack` at the same time, and they will apply their respective override rules accordingly.