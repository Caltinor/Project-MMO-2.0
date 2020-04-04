package harmonised.pmmo.commands;

import java.util.Arrays;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandSet
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        System.out.println(Arrays.toString(args));


        if (Skill.getInt( args[2] ) != 0)
        {
            boolean setLevel = false;
            float newXp = -1;
            try
            {
                if ( args[3].toLowerCase().charAt( args[3].length() - 1) == 'l')
                {
                    args[3] = args[3].toLowerCase().replaceFirst("l", "");
                    setLevel = true;
                }
                newXp = Float.parseFloat( args[3].replace(',', '.') );
            }
            catch (NumberFormatException e)
            {
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidNumberWarning", args[3] ), false);
                return 1;
            }

            if ( setLevel )
            {
                if ( newXp >= XP.maxLevel )
                    newXp = XP.maxLevel;
                if( newXp < 1 )
                    newXp = 1;
                XP.setXp( player, args[2], XP.xpAtLevelDecimal(newXp) );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.setLevel", "pmmo.text." + args[2], "" + newXp, false, 0 ), (ServerPlayerEntity) player );
            }
            else if (newXp >= 0 && newXp <= 2000000000)
            {
                XP.setXp( player, args[2], newXp );
                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.text.setXp", "pmmo.text." + args[2], "" + newXp, false, 0 ), (ServerPlayerEntity) player );
            }
            else
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.xpCap" ), false);
        }
        else

             player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.invalidSkillWarning", args[2] ), false);
    return 1;
    }
}
