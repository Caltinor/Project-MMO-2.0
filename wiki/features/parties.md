[Home](../home.md)

parties allow players to join together to share XP gains.  Players need to be in proximity of each other, and those that are in range split any xp earned between their nearby party members.

## Creating a Party
To create a party a player uses the command `/pmmo party create`. the player will not be able to create a party if they are already a member of another party

## Inviting players to a Party
invite online players to a party using `/pmmo party invite <player>`. This will send a message in chat to that user that they can accept or decline.  there is no expiration on these invitations.  If you would like to rescind an invitation, use the command `/pmmo party uninvite <player>`.

There is no party owner, and all players can invite and uninvite players as they wish.

## Responding to an Invitation
When you are invited to a party, you will receive a message in chat with links to accept or decline the invite.  These do not expire, but they may be rescinded by the party.  If you accept a rescinded invite, you will be notified the invite is no longer valid.  If you decline an invitation, you will need to be invited again to join the party.

### Multiple Invites
if you are invited to multiple parties at the same time, you can only be a member of one party.  If you accept the first invite, you will be added to the party.  If you then accept the second invite, you will be switched parties.  Note, however, that because you responded to the first invite, it has been used so you cannot click the link again to rejoin the first party.  You will need to be reinvited.  With that said, you do not need to leave a party to join another party

## Kicking party members
Since there is no owner, there is no authority to kick or prevent invites.  If you no longer want a specific person in your party, leave and reform a new one with the members you do want.

## Showing party members
use the command `/pmmo party list` to display all party members

## Leave a Party
To leave a party use the command `/pmmo party leave`

## Party XP, How it works
Sharing xp always originates from the player who earned it.  party members who are in range of that player get an equal split of the XP earned.  For example, in the example below each "-" is a 10 block distance and our config is set to award XP to party members within 40 blocks.
```
B---A---C
```
if player "A" earns 30 XP, both B and C are in range and will split the XP giving everyone 10 XP.  If player C earns 30 XP, only player A is in range, therefore players A and C get 15 XP each.

[Home](../home.md)