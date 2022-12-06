package harmonised.pmmo.commands;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.fml.LogicalSide;

public class CmdNodeStore {
	private static final String TARGET_ARG = "Target";
	private static final String SKILL_ARG = "Skill Name";

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("store")
				.requires(p -> p.hasPermission(2))
				.then(Commands.argument(TARGET_ARG, EntityArgument.players())
						.then(Commands.argument(SKILL_ARG, StringArgumentType.word())
								.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SkillsConfig.SKILLS.get().keySet(), builder))
								.executes(ctx -> store(ctx))));
	}
	
	public static int store(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, TARGET_ARG);
		String skillName = StringArgumentType.getString(ctx, SKILL_ARG);
		MinecraftServer server = ctx.getSource().getServer();
		for (ServerPlayer player : players) {
			int skillLevel = getSkillLevel(skillName, player.getUUID());
			server.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), getOrCreate(server, skillName))
			.setScore(skillLevel);;
		}
		return 0;
	}
	
	private static Objective getOrCreate(MinecraftServer server, String objective) {
		Objective obtainedObjective = server.getScoreboard().getObjective(objective);
		if (obtainedObjective == null)
			obtainedObjective = server.getScoreboard().addObjective(objective, ObjectiveCriteria.DUMMY, Component.translatable("pmmo."+objective), ObjectiveCriteria.RenderType.INTEGER);
		return obtainedObjective;
	}
	
	private static int getSkillLevel(String skill, UUID pid) {
		Core core = Core.get(LogicalSide.SERVER);
		SkillData skillData = SkillsConfig.SKILLS.get().get(skill);
		if (skillData == null) return 0;
		if (skillData.isSkillGroup()) {
			int groupLevel = 0;
			for (Map.Entry<String, Double> portion : skillData.groupedSkills().get().entrySet()) {
				groupLevel += (core.getData().getPlayerSkillLevel(portion.getKey(), pid) * portion.getValue());
			}
			return groupLevel;
		}
		else return core.getData().getPlayerSkillLevel(skill, pid);
	}
}
