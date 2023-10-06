# MsLoggy Logging System
This is a custom system for PMMO that allows for granular control over what logged by the mod.  If you are not familiar, information such as statuses and errors are only printed to the game's logs if a mod does so.  Most mods do this indiscriminately or rely on logging levels defined when you launch the game.  MsLoggy takes this one step further and adds deeper control.

## Logging Levels
Universal to all logging platforms are the five (5) main logging levels.
|Logging Level|General Purpose|
|:---:|:---:|
|INFO|General information such as loading statuses and progress|
|DEBUG|Technical information used in solving issues|
|WARN|Problmatic issues that may or may not need attention|
|ERROR|Incorrect behavior that may be causing problems|
|FATAL|Incorrect behavior catastrophic to program function|

Project MMO uses these but adds an extra layer on top to let you filter what is shown by each of these levels.  By default all levels are enabled and will show all log information that is logged through them.

## Logging Categories
MsLoggy adds in logging categories which filter what each logging level shows according to what you want to see.

|Log Code|General Purpose|
|:---:|:---:|
|"api"|Logs actions related to API usage, such as registrations|
|"autovalues"|Logs information about AutoValues functionality|
|"chunk"|Logs data related to chunk storage, such as placed block records|
|"data"|Logs information related to datapacks and configurations and the underlying processing of that information|
|"event"|Logs event information|
|"feature"|Logs information related to features like vein mining, mob scaling, etc|
|"gui"|Logs gui-related information|
|"loading"|Logs information related to the loading of pmmo|
|"network"|Logs information related to packet flow|
|"xp"|Logs information related to the gain and manipulation of xp|
|"none"|A placeholder for logging levels when you want nothing logged for that level|

### Using Categories
MsLoggy is configured in `pmmo-common.toml`.  Each Logging Level has an array `[]` which allows you to add categories you want logged.  For example if you wanted to log what your datapack configurations look like as well as what Autovalues are being generated, you would use:
```toml
INFO = ["data", "autovalues"]
```
If you were having issues with datapacks you might want more technical information and so adding "data" to the DEBUG list might also be helpful.
```toml
INFO = ["data", "autovalues"]
DEBUG = ["data"]
```
The next section will break down every logging aspect and category so you can see exactly what you are logging when adding categories to these lists.

## All Logging Levels, Categories, and Data Points
WIP