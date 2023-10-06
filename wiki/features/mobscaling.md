Mob scaling is a feature that causes mobs spawned near players to have their attributes modified based on the skill level of those nearby players. All settings for mob scaling are located in the server config `pmmo-server.toml`

## Mob Scaling Formula
Like player xp gain, mob scaling can be linear or exponential.  This should be adjusted along with any changes to the default formula to ensure mobs scale in difficulty proportionately to the players increase in skill.

## Mob Scaling Ratios
Mob scaling affects three mob attributes: Health, Speed, Attack Damage.  Each of these has its own setting for what skills affect the attribute and how much.  You can have multiple skills with varying scales and even contradictory skills where one raises the difficulty while another lowers it.  Each attribute can have different skills and scales allowing you to give your players tradeoffs in skills-scaling effects.

## Scaling AOE
This setting determines how far away from the mob spawn should players be checked.  All players in range will have their skills averaged when calculating how to scale the mob.