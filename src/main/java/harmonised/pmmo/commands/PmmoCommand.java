package harmonised.pmmo.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Reference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;

public class PmmoCommand
{
    //private static final Logger LOGGER = LogManager.getLogger();
    public static String[] suggestSkill;
    public static String[] levelOrXp = { "level", "xp" };
    public static String[] acceptOrDecline = { "accept", "decline" };

    public static String[] suggestSearchRegistry = { "item",
                                                     "block",
                                                     "biome",
                                                     "enchant",
                                                     "potionEffect",
                                                     "entity" };

    public static void init()
    {
        suggestSkill = new String[ Skill.getSkills().size() ];
        int i = 0;
        for(String skill : Skill.getSkills().keySet())
        {
            suggestSkill[ i++ ] = skill.toLowerCase();
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
//        int i = 0;
//
//        for(Biome biome : ForgeRegistries.BIOMES.getValues())
//        {
//            suggestBiome[i++] = biome.getRegistryName().toString();
//        }

        dispatcher.register(Commands.literal(Reference.MOD_ID)
                  //ADMIN
                    .then(Commands.literal("admin")
                    .requires(player -> player.hasPermission(2))
                    .then(Commands.argument("target", EntityArgument.players())
                    .then(Commands.literal("set")
                    .then(Commands.argument("Skill", StringArgumentType.word())
                    .suggests((ctx, theBuilder) -> SharedSuggestionProvider.suggest(suggestSkill, theBuilder))
                    .then(Commands.argument("Level|Xp", StringArgumentType.word())
                    .suggests((ctx, theBuilder) -> SharedSuggestionProvider.suggest(levelOrXp, theBuilder))
                    .then(Commands.argument("New Value", DoubleArgumentType.doubleArg())
                    .executes(SetCommand::execute)
                   ))))
                    .then(Commands.literal("add")
                    .then(Commands.argument("Skill", StringArgumentType.word())
                    .suggests((ctx, theBuilder) -> SharedSuggestionProvider.suggest(suggestSkill, theBuilder))
                    .then(Commands.argument("Level|Xp", StringArgumentType.word())
                    .suggests((ctx, theBuilder) -> SharedSuggestionProvider.suggest(levelOrXp, theBuilder))
                    .then(Commands.argument("Value To Add", DoubleArgumentType.doubleArg())
                    .executes(AddCommand::execute)
                    .then(Commands.argument("Ignore Bonuses", BoolArgumentType.bool())
                    .executes(AddCommand::execute))
                   ))))
                    .then(Commands.literal("clear")
                    .executes(ClearCommand::execute))))
                  //PARTY
                  .then(Commands.literal("party")
                    .executes(PartyCommand::execute)
                    .then(Commands.literal("accept")
                    .executes(AcceptPartyCommand::execute))
                    .then(Commands.literal("decline")
                    .executes(DeclinePartyCommand::execute))
                    .then(Commands.literal("invite")
                    .then(Commands.argument("target", EntityArgument.player())
                    .executes(InvitePartyCommand::execute)))
                    .then(Commands.literal("create")
                    .executes(CreatePartyCommand::execute))
                    .then(Commands.literal("leave")
                    .executes(LeavePartyCommand::execute)))
                    .then(Commands.literal("reload")
                    .requires(player ->  player.hasPermission(2))
                    .executes(ReloadConfigCommand::execute)
                 )
                  //RESYNC
                    .then(Commands.literal("resync")
                    .executes(context -> SyncCommand.execute(context, EntityArgument.getPlayers(context, "target")))
                   )
                    .then(Commands.literal("resync")
                    .executes(context -> SyncCommand.execute(context, null)))
                  //TOOLS
                  .then(Commands.literal("tools")
                    .then(Commands.literal("levelatxp")
                    .then(Commands.argument("xp", DoubleArgumentType.doubleArg())
                    .executes(LevelAtXpCommand::execute)
                   ))
                    .then(Commands.literal("xpatlevel")
                    .then( Commands.argument("level", DoubleArgumentType.doubleArg())
                    .executes(XpAtLevelCommand::execute)
                   ))
                    .then(Commands.literal("xpto")
                    .then( Commands.argument("level", DoubleArgumentType.doubleArg())
                    .executes(XpFromToCommand::execute)
                    .then( Commands.argument("goal level", DoubleArgumentType.doubleArg())
                    .executes(XpFromToCommand::execute)
                   ))))
                  //CHECK BIOME
                  .then(Commands.literal("checkbiome")
                    .executes(CheckBiomeCommand::execute)
                 )
                  //CHECK DEBUG
                    .then(Commands.literal("debug")
                    .then(Commands.literal("searchRegistry")
                    .then(Commands.argument("type", StringArgumentType.word())
                    .suggests((ctx, theBuilder) -> SharedSuggestionProvider.suggest(suggestSearchRegistry, theBuilder))
                    .then(Commands.argument("search query", StringArgumentType.word())
                    .executes(SearchRegCommand::execute)
                   )))
                    .then(Commands.literal("nearbyPowerLevel")
                    .executes(NearbyPowerLevelCommand::execute)
                    .then(Commands.argument("target", EntityArgument.player())
                    .executes(NearbyPowerLevelCommand::execute)
                   )))
                  //HELP
                  .then(Commands.literal("help")
                    .executes(HelpCommand::execute)
                 )
       );
    }
}
