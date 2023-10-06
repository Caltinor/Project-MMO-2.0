[Home](../home.md)

Project MMO has two types of configuration files: configs (`.toml`) and data (datapack jsons)

## Datapacks and JSONS
Project MMO used to have a folder `config/pmmo/` that contained many json files that could be used to configure PMMO.  Many of the changes in the rework made that impractical and a move was made to shift everything into datapacks.  Below are links to the specific configuration page for each datapack-able element

### Overrides
Pmmo ships with default settings so that the general user has something to play with, out of the box.  You may wish to change these defaults to your liking.  To ensure your setting takes precedence, add the property `"override": true` to your configurations.  This will also apply if you use multiple datapacks and wish to ensure your setting overrides all others.

### Getting Started
To save yourself time, pmmo provides a command to create a datapack for you.  `/pmmo genData` will create a datapack called "generated_data" in the datapacks folder for that world.  This pack will include files for every mod installed and all of their items, blocks, entities, dimensions, biomes, and enchantments.  You can rerun this command at any point to have additional mods' data included or to restore files you may have deleted.  This will not override current files.

***Note: Delete files you have not edited***

This command generates a lot of data.  For certain computers and network connections, this extra large pack with empty data can impact performance.  by deleting files you are not using, this is mitigated.  Remember, you can always rerun the command to regenerate files you delete.

***Note: this command requires OP permissions or cheats enabled***

### Command options
`/pmmo genData` is a "builder" style command.  This means you call the command multiple times to set it up and use the "create" command to finalize everything.  Below are the sub-commands and their function

| command                                      | purpose                                                                                                        |
|:---------------------------------------------|:---------------------------------------------------------------------------------------------------------------|
| `/pmmo genData begin`                        | resets all settings for a new command sequence                                                                 |
| `/pmmo genData withOverrides`                | makes all generated files override other datapacks, including the default data                                 |
| `/pmmo genData withDefaults`                 | generates all files with their current settings, including any AuotValues                                      |
| `/pmmo genData simplified`                   | removes all unused properties (for those familiar with pmmo data)                                              |
| `/pmmo genData modFilter <modid>`            | generates files for only this mod.  may be called multiple times to include additional mods                    |
| `/pmmo genData forPlayers <player selector>` | creates player-specific files for the selected players. maybe called multiple times to add multiple selections |
| `/pmmo genData disabler`                     | adds a pack filter which disables all default data and that of packs loaded before this pack                   |
| `/pmmo genData create`                       | builds the datapack using the settings you have enabled                                                        |

### Specific Configuration Details
- [Items](items.md)
- [Blocks](blocks.md)
- [Entities](entities.md)
- [Dimensions](dimensions.md)
- [Enchantments](enchantments.md)
- [Effects](effects.md)
- [Biomes](biomes.md)
- [Players](players.md)

## Config .Toml files
Forge provides three different types of config files: CLIENT, COMMON, and SERVER.  Project MMO uses all 3 and in the case of SERVER configs, multiple.

### CLIENT Config
There is one client config located in your minecraft directory at `/config/pmmo-client.toml`.  This config allows you to change settings related to visuals that only affect your game.  For example, you can change where the skill list appears on screen.  This change will only happen on your game and not others.

### COMMON Config
The common config is used by both the server and the player but not at the same time and is located in your minecraft directory at `/config/pmmo-common.toml`.  Your settings in the common config will still only apply to your instance of the game and the server's its own.  Currently, the only feature in the common config is the [logging system](../features/logging.md) "Ms. Loggy".  Changes made here affect how your side of the game logs pmmo in your log files.

### SERVER Configs
There are five server configs which are located in your minecraft directory at `/saves/<worldname>/serverconfig/<configname>.toml`.  If you are on a dedicated server they are located at `/<worldname>/serverconfig/<configname>.toml`.  The six configs are:

| config name          | purpose                                                                                                   |
|:---------------------|:----------------------------------------------------------------------------------------------------------|
| pmmo-server.toml     | contains general settings such as defaults for xp and reqs as well as certain enabling/disabling settings |
| pmmo-Skills.toml     | where custom skill colors and settings are defined                                                        |
| pmmo-Perks.toml      | where perks are configured                                                                                |
| pmmo-Globals.toml    | where NBT globals are defined                                                                             |
| pmmo-AutoValues.toml | contains all the settings for autovalues                                                                  |
| pmmo-AntiCheese.toml | where anti-cheese rules are defined                                                                       |

*Note that server configs are unique to the world they are nested under.  If you want your configs to carry over between worlds, be sure to make a copy of your config and place it in the `/defaultconfigs/` folder.  This is a Forge folder that copies server configs over to new worlds when they are created.*

[Home](../home.md)