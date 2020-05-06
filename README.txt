https://www.curseforge.com/minecraft/mc-mods/project-mmo

I am a new mod dev, so please let me know if the following is not correct:
I believe I have API support for my mod! You should be able to access my mod's Levels by the methods


XP.getLevel (takes in Skill + player, returns Floored level as int, works on client/server)
Skill.SKILLNAME.getLevel( player ), same function as XP.getLevel
XP.getLevelDecimal (takes in skillName + player, returns level as double, works on client/server)
XP.xpAtLevel (takes in a level as int, returns double xp value)
XP.xpAtLevelDecimal (takes in a level as double, returns double xp value)
XP.awardXp (the skill Integer can be found in the Skill class, or retrieved by Skill.getInt( skillName )
XP.getSkillsTag (takes in player, returns PMMO Skills CompoundNBT of all the player's xp values, as doubles)

To use the API, include these two lines in your gradle.build

repositories { maven { url "http://dvs1.progwml6.com/files/maven/" } }
dependencies { compileOnly fg.deobf("curse.maven:project-mmo:${version}") }

If you have issues with this, or know what could be wrong, please contact me on Discord, or otherwise!