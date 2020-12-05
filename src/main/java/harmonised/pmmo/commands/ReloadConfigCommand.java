package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

public class ReloadConfigCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        JsonConfig.init();  //Load Up Locally
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            AutoValues.setAutoValues(); //Set Auto Values

        context.getSource().getServer().getPlayerList().getPlayers().forEach( player ->
        {
            XP.syncPlayerDataAndConfig( player );
            XP.updateRecipes( player );
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.jsonConfigReload" ).setStyle( XP.textStyle.get( "green" ) ), false );
        });

        System.out.println( "PMMO Config Reloaded" );

        return 1;
    }
}
