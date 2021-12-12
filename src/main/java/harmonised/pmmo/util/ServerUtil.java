package harmonised.pmmo.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class ServerUtil
{
    private static boolean serverSide = false;
    private static MinecraftServer server;
    private static final Map<ResourceLocation, ServerWorld> worlds = new HashMap<>();
    private static long overworldTime = 0;

    public static ServerWorld overworld;
    public static ServerWorld nether;
    public static ServerWorld end;

    public static void init(MinecraftServer server)
    {
        serverSide = true;
        ServerUtil.server = server;
        worlds.clear();
        for(ServerWorld world : server.getWorlds())
        {
            worlds.put(XP.getDimResLoc(world), world);
        }
        overworld = getWorld(ServerWorld.OVERWORLD.getLocation());
        nether = getWorld(ServerWorld.THE_NETHER.getLocation());
        end = getWorld(ServerWorld.THE_END.getLocation());
    }

    public static void tick(TickEvent.WorldTickEvent event)
    {
        overworldTime = event.world.getGameTime();
    }

    public static long getGameTime()
    {
        return overworldTime;
    }

    public static List<ServerPlayerEntity> getPlayers()
    {
        return server == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
    }

    public static ServerWorld getWorld(ResourceLocation dimResLoc)
    {
        return worlds.get(dimResLoc);
    }

    public static void sendMsgToAll(ITextComponent msg, boolean mid)
    {
        sendMsg(server.getPlayerList().getPlayers(), msg, mid);
    }

    public static void sendMsg(Collection<ServerPlayerEntity> players, ITextComponent msg, boolean mid)
    {
        for(ServerPlayerEntity player : players)
            player.sendStatusMessage(msg, mid);
    }
}
