[Home](../home.md)

This page details the data format for json files in the `data/namespace/pmmo/players/` data folder.

## File Naming and Folders
The name of a file needs to correspond with a user's UUID.  You can do this for any user via https://gameplay.tools/minecraft/uuid.  for example my (Caltinor) UUID is "bd1c2ad6-849a-418a-89bc-6709c571c96b".  Therefore, the filename for my player json is `bd1c2ad6-849a-418a-89bc-6709c571c96b.json`

Because players are a minecraft object, they only go under the minecraft namespace.  `data/minecraft/pmmo/players/bd1c2ad6-849a-418a-89bc-6709c571c96b.json` is the only location my json can go.

## Example File
```json5
{
    "ignoreReq": true, //this player is not affected by requirements
    //this is a per-player bonus which means this player will always have the bonuses, but only this player.
    "bonuses": {
      "skillname": 1.5 // 50% increase
    }
}
```

[Home](../home.md)