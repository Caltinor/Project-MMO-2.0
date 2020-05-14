package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class CheckStatCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        String skillName = args[3].toLowerCase();

        if( Skill.getInt( skillName ) != 0 || skillName.toLowerCase().equals( "power" ) )
        {
            try
            {
                double level = 1;

                ServerPlayerEntity target = EntityArgument.getPlayer( context, "player name" );
                if( skillName.toLowerCase().equals( "power" ) )
                    level = XP.getPowerLevel( target );
                else
                    level = XP.levelAtXpDecimal( XP.getSkillsTag( target ).getDouble( skillName ) );

                sender.sendStatusMessage( new TranslationTextComponent( "pmmo.playerLevelDisplay", target.getDisplayName().getString(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TranslationTextComponent( "pmmo." + skillName ).setStyle( new Style().setColor( XP.skillTextFormat.get( skillName ) ) ) ), false );

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

                        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.fishPoolChance", DP.dp( fishPoolChance )  ).setStyle( new Style().setColor( XP.skillTextFormat.get( skillName ) ) ), false );
                        break;
                }
            }
            catch( CommandSyntaxException e )
            {
                sender.sendStatusMessage(  new TranslationTextComponent( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            }
        }
        else
            sender.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidSkill", skillName ).setStyle( XP.textStyle.get( "red" ) ), false );

        return 1;
    }
}
