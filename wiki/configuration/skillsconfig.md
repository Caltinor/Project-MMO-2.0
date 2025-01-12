# Skills Config Syntax and Examples

## Example File
```
WITH config(skills)
    set(mining)
        .color(123456)
        .maxLevel(100)
        .noAfkPenalty(true);
    set(fightgroup)
        .groupFor(combat,0.5,archery0.5)
        .useTotalLevels(true)
        .showInList(false);
END
```

## Syntax
Each skill setting starts with `set(skillname)`' followed by an optional number of value nodes.  the table below lists the value nodes, the format for their values and the default value if you omit that keyword.

|    node keyword    | value format                                                                              | default value                                | What it does                                                                         |
|:------------------:|:------------------------------------------------------------------------------------------|:---------------------------------------------|:-------------------------------------------------------------------------------------|
|      `color`       | whole number                                                                              | `16777215`                                   | Sets the skill's display color                                                       |
|     `iconSize`     | whole number                                                                              | `18`                                         | Lets pmmo know the size of the raw image scaling                                     |
|     `maxLevel`     | whole number                                                                              | same as server config max level              | The max level for this skill independent of the global max level                     |
|   `noAfkPenalty`   | true/false                                                                                | `false`                                      | If true this skill will never be affected by anti-cheese                             |
| `displayGroupName` | true/false                                                                                | `false`                                      | Should the group name show on tooltips, if false each member skill will show instead |
|  `useTotalLevels`  | true/false                                                                                | `false`                                      | Should this skill be a sum of its group members when checking skill levels           |
|    `showInList`    | true/false                                                                                | `true`                                       | Should this skill appear in the skill list on the user's screeen                     |
|       `icon`       | namespace:textures/texurename.png                                                         | `minecraft:textures/skills/missing_icon.png` | the location of your desired texture from vanilla or a resource pack                 |
|     `groupFor`     | skills and ratios separated by commas. eg `groupFor(combat,0.3,archery,0.3,endurance0.4)` | none.                                        | If populated, defines this skill as a "skill group"                                  | 