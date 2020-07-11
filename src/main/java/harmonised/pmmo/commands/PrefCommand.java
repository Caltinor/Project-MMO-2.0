package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

public class PrefCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        CompoundNBT prefsTag = XP.getPreferencesTag( player );
        Double value = null;
        if( args.length > 3 )
        {
            value = Double.parseDouble( args[3] );
            if( value < 0 )
                value = 0D;
        }


        boolean matched = false;
        String match = "ERROR";

        for( String element : PmmoCommand.suggestPref )
        {
            if( args[2].toLowerCase().equals( element.toLowerCase() ) )
            {
                match = element;
                matched = true;
            }
        }

        for( String element : PmmoCommand.suggestGui )
        {
            if( args[2].toLowerCase().equals( element.toLowerCase() ) )
            {
                match = element;
                matched = true;
            }
        }

        if( matched )
        {
            if( value != null )
            {
                prefsTag.putDouble( match, value );

                NetworkHandler.sendToPlayer( new MessageUpdateNBT( prefsTag, 0 ), (ServerPlayerEntity) player );
                AttributeHandler.updateAll( player );

                player.sendStatusMessage( new TranslationTextComponent( "pmmo.func_235901_b_BeenSet", match, args[3] ), false );
            }
            else if( prefsTag.contains( match ) )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.func_235901_b_TheValue", "" + match, "" + prefsTag.getDouble( match ) ), false );
            else
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.func_235901_b_UnsetValue", "" + match ), false );
        }
        else
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.invalidChoice", args[2] ).func_240703_c_( XP.textStyle.get( "red" ) ), false );

        return 1;
    }
}
