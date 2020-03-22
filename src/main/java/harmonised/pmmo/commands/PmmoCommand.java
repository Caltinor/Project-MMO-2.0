package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;

import harmonised.pmmo.skills.XP;

public class PmmoCommand
{
    public static void register( CommandDispatcher<CommandSource> dispatch )
    {
        String[] suggestCommand = new String[1];
        suggestCommand[0] = "Set";
//        suggestCommand[1] = "Clear";
//        suggestCommand[2] = "LevelAtXp";
//        suggestCommand[3] = "XpAtLevel";

        String[] suggestSkill = new String[14];
        suggestSkill[0] = "Mining";
        suggestSkill[1] = "Building";
        suggestSkill[2] = "Excavation";
        suggestSkill[3] = "Woodcutting";
        suggestSkill[4] = "Farming";
        suggestSkill[5] = "Agility";
        suggestSkill[6] = "Endurance";
        suggestSkill[7] = "Combat";
        suggestSkill[8] = "Archery";
        suggestSkill[9] = "Repairing";
        suggestSkill[10] = "Flying";
        suggestSkill[11] = "Swimming";
        suggestSkill[12] = "Fishing";
        suggestSkill[13] = "Crafting";

        String[] suggestClear = new String[1];
        suggestClear[0] = "iagreetothetermsandconditions";

        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("pmmo")
                .requires( src -> src.hasPermissionLevel(0 ) )
                .requires( src -> src.getEntity() instanceof ServerPlayerEntity )
                .then( Commands.argument("command", StringArgumentType.word() )
                .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestCommand, theBuilder ) )
                .executes( CommandClear::execute ) );

        builder.then( Commands.argument("Set", StringArgumentType.word() )
                .then( Commands.argument("skill", StringArgumentType.word() )
                .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestSkill, theBuilder ) )
                .then( Commands.argument("new xp", StringArgumentType.word() )
                .executes( CommandSet::execute ) ) ) );

//        builder.then( Commands.argument("Clear", StringArgumentType.word() )
//                .executes( CommandClear::execute )
//                .then( Commands.argument("agreement", StringArgumentType.word() )
//                .suggests( ( ctx, theBuilder ) -> ISuggestionProvider.suggest( suggestClear, theBuilder ) )
//                .executes( CommandClear::execute ) ) );

        dispatch.register( builder );
    }
}
