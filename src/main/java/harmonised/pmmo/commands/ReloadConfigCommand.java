package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadConfigCommand
{
    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        JsonConfig.init();  //Load Up Locally
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            AutoValues.setAutoValues(); //Set Auto Values

        context.getSource().getServer().getPlayerList().getPlayers().forEach( player ->
        {
            XP.syncPlayerDataAndConfig( player );
            XP.updateRecipes( player );
            player.displayClientMessage( new TranslatableComponent( "pmmo.jsonConfigReload" ).setStyle( XP.textStyle.get( "green" ) ), false );
        });

        System.out.println( "PMMO Config Reloaded" );

        return 1;
    }
}
