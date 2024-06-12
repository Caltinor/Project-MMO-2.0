## Commands
Project MMO has many commands that may be of use to players, server ops, and modpack developers. They are listed below.

|Command|Permission|Use|
|:---|:---:|:---|
|`/pmmo help`| None | Shows places where help can be obtained.|
|`/pmmo party`| None | Features for creating and managing a party. [See below](commands.md#pmmo-party)|
|`/pmmo debug`| ---- | Not currently implemented.|
|`/pmmo admin`|  OP  | Ability to add/remove XP/levels/bonuses or allow a player to ignore requirements. [See below](commands.md#pmmo-admin)|
|`/pmmo store`|  OP  | Takes a player and skill name arguments and stores those values onto the Scoreboard.|
|`/pmmo genData`|  OP  | For building datapacks used to configure Project MMO. Please see [Configuration](configuration/configuration.md#command-options)|

-----

## `/pmmo party`

|Option|Use|
|:---|:---|
|`accept`| Accepts an invite to a party.|
|`create`| Create a new party.|
|`decline`| Declines an sent party invite.|
|`invite`| Invite a player to your party.|
|`leave`| Leaves your current party.|
|`list`| Shows the members of your party.|
|`uninvite`| Rescinds a sent invite to a player.|

## `/pmmo admin`
Note: the first argument after `admin` is a target. The following table is the list of actions that can be taken on target(s).

|Option|Use|
|:---|:---|
|`add`| Add XP/levels to the selected target.|
|`adminBonus`| Set a bonus that all XP of the chosen type is multiplied by.|
|`clear`| Resets the target(s) levels and XP.|
|`ignoreReqs`| Allows the target(s) to ignore any requirements for actions.|
|`set`| Sets the XP/level in the chosen skill to an exact amount.|
