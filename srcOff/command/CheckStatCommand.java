package harmonised.pmmo.commands;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;

import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentTranslation;

public class CheckStatCommand
{
    public static int execute(  ) throws CommandException
    {
        EntityPlayer sender = (EntityPlayer) ;
        String[] args = context.getInput().split(" ");
        Skill skill = Skill.getSkill( args[3] );
        String skillName = skill.name().toLowerCase();

        if( skill.getValue() != 0 || args[3].toLowerCase().equals( "power" ) )
        {
            try
            {
                double level = 1;

                EntityPlayerMP target = EntityArgument.getPlayer( context, "player name" );
                if( args[3].toLowerCase().equals( "power" ) )
                {
                    level = XP.getPowerLevel( target.getUniqueID() );
                    sender.sendStatusMessage( new TextComponentTranslation( "pmmo.playerLevelDisplay", target.getDisplayName().getUnformattedText(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TextComponentTranslation( "pmmo.power" ).setStyle( new Style().setColor( TextFormatting.AQUA ) ) ), false );
                }
                else
                {
                    level = skill.getLevelDecimal( target.getUniqueID() );
                    sender.sendStatusMessage( new TextComponentTranslation( "pmmo.playerLevelDisplay", target.getDisplayName().getUnformattedText(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TextComponentTranslation( "pmmo." + skillName ).setStyle( XP.getSkillStyle( skill ) ) ), false );
                }

                //EXTRA INFO
                switch( skillName )
                {
                    case "fishing":
                        double fishPoolBaseChance = Config.forgeConfig.fishPoolBaseChance.get();
                        double fishPoolChancePerLevel = Config.forgeConfig.fishPoolChancePerLevel.get();
                        double fishPoolMaxChance = Config.forgeConfig.fishPoolMaxChance.get();
                        double fishPoolChance = fishPoolBaseChance + fishPoolChancePerLevel * level;
                        if( fishPoolChance > fishPoolMaxChance )
                            fishPoolChance = fishPoolMaxChance;

                        sender.sendStatusMessage( new TextComponentTranslation( "pmmo.fishPoolChance", DP.dp( fishPoolChance )  ).setStyle( XP.getSkillStyle( skill ) ), false );
                        break;
                }
            }
            catch( CommandSyntaxException e )
            {
                sender.sendStatusMessage(  new TextComponentTranslation( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            }
        }
        else
            sender.sendStatusMessage( new TextComponentTranslation( "pmmo.invalidSkill", args[3] ).setStyle( XP.textStyle.get( "red" ) ), false );

        return 1;
    }
}
