package harmonised.pmmo.gametests;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.gametest.ParametrizedGameTestSequence;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.impl.test.AbstractTest;
import net.neoforged.testframework.registration.RegistrationHelper;

import java.util.List;
import java.util.Map;

@TestHolder(
        value = "event_tests",
        description = "Tests each EVENT_TYPE for valid behavior"
)
public class EventTests extends AbstractTest {
    @TestHolder(description = "Tests direct manipulation of XP.")
    @EmptyTemplate(floor = true)
    @GameTest
    public static void xpAwardTest(final DynamicTest test) {
        test.onGameTest(helper -> helper
                .startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> Core.get(player.level()).getData().setXp(player.getUUID(), "test", 10L))
                .thenMap(player -> APIUtils.getXp("test", player))
                .thenWaitUntil(xp -> helper.assertTrue(xp == 10, "explicitly set xp does not equal 10, is "+xp))
                .thenSucceed()
        );
    }

    @TestHolder(description = "Checks if a player is prohibited from using a tool, then gives them the xp to use the tool and test that they now can.")
    @EmptyTemplate(floor = true)
    @GameTest
    public static void reqToolTest(final DynamicTest test) {
        final var pos = new BlockPos(1,2,1);
        test.onGameTest(helper -> helper
                .startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                .thenExecute(player -> Core.get(player.level()).getData().setLevel("mining", player.getUUID(), 0))
                .thenExecute(player -> Core.get(player.level()).getLoader().ITEM_LOADER.getData(RegistryUtil.getId(Items.STONE_PICKAXE)).setReqs(ReqType.TOOL, Map.of("mining", 10L)))
                .thenExecute(player -> player.setItemSlot(EquipmentSlot.MAINHAND, Items.STONE_PICKAXE.getDefaultInstance()))
                .thenWaitUntil(player -> helper.assertFalse(Core.get(player.level()).isActionPermitted(ReqType.TOOL, player.getMainHandItem(), player), "Player TOOL req ignored in test"))
                //update player skills to now have the required level
                .thenExecute(player -> Core.get(player.level()).getData().setLevel("mining", player.getUUID(), 20L))
                //Block should now be absent
                .thenWaitUntil(player -> helper.assertTrue(Core.get(player.level()).isActionPermitted(ReqType.TOOL, player.getMainHandItem(), player), "Player TOOL req prevented when it should permit"))
                .thenSucceed());
    }
}
