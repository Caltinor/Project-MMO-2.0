package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Arrays;

public class PmmoCommand
{
    public static void register( CommandDispatcher<CommandSource> dispatcher )
    {
        String[] suggestSkill = new String[15];
        suggestSkill[0] = "Mining";
        suggestSkill[1] = "Building";
        suggestSkill[2] = "Excavation";
        suggestSkill[3] = "Woodcutting";
        suggestSkill[4] = "Farming";
        suggestSkill[5] = "Agility";
        suggestSkill[6] = "Endurance";
        suggestSkill[7] = "Combat";
        suggestSkill[8] = "Archery";
        suggestSkill[9] = "Smithing";
        suggestSkill[10] = "Flying";
        suggestSkill[11] = "Swimming";
        suggestSkill[12] = "Fishing";
        suggestSkill[13] = "Crafting";
        suggestSkill[14] = "Magic";

        String[] suggestClear = new String[1];
        suggestClear[0] = "iagreetothetermsandconditions";

        String[] levelOrXp = new String[2];
        levelOrXp[0] = "level";
        levelOrXp[1] = "xp";

        dispatcher.register( Commands.literal( "pmmo" ).requires( player -> { return player.hasPermissionLevel( 2 ); })
                  .then( Commands.literal( "level" )
                  .then( Commands.argument( "target", EntityArgument.players() )
                  .then( Commands.literal( "set" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "New Value", DoubleArgumentType.doubleArg() )
                  .executes( context ->
                  {
                      String[] args = context.getInput().split( " " );

                      System.out.println( Arrays.toString( args ) );
                      return 1;
                  })
                  ))))
                  .then( Commands.literal( "add" )
                  .then( Commands.argument( "Skill", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                  .then( Commands.argument( "Level|Xp", StringArgumentType.word() )
                  .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( levelOrXp, theBuilder ) )
                  .then( Commands.argument( "Value To Add", DoubleArgumentType.doubleArg() )
                  .executes( context ->
                  {
                      String[] args = context.getInput().split( " " );

                      System.out.println( Arrays.toString( args ) );
                      return 1;
                  })))))
                  .then( Commands.literal( "clear" )
                  .executes( context ->
                  {
                      System.out.println( "Clear command" );
                      return 1;
                  }
                  )))));
    }
}
