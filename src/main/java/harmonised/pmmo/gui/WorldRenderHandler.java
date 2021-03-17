package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageWorldXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class WorldRenderHandler
{
    private static long lastTime = System.nanoTime();
    private final static Map<ResourceLocation, List<WorldXpDrop>> xpDrops = new HashMap<>();

    public static float worldXpDropsSizeMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsSizeMultiplier.get() );
    public static float worldXpDropsDecaySpeedMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsDecaySpeedMultiplier.get() );
    public static boolean worldXpDropsShowSkill =  Config.forgeConfig.worldXpDropsShowSkill.get();

    @SubscribeEvent
    public void handleWorldRender( RenderWorldLastEvent event )
    {
//        temp1++;
        Minecraft mc = Minecraft.getInstance();
        World world = mc.world;
        if( world == null )
            return;
//        ResourceLocation dimResLoc = world.getDimensionType().getEffects();
        ResourceLocation dimResLoc = XP.getDimResLoc( world );
        if( !xpDrops.containsKey( dimResLoc ) )
            return;
        List<WorldXpDrop> dimXpDrops = xpDrops.get( dimResLoc );
        long currTime = System.nanoTime();
        double d = (currTime - lastTime) / 1000000000D;
        FontRenderer fr = mc.getRenderManager().getFontRenderer();
        MatrixStack stack = event.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
        Vector3d pos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        stack.push();
        for( int i = dimXpDrops.size()-1; i >= 0; i-- )
        {
            WorldXpDrop xpDrop = dimXpDrops.get( i );
            if( xpDrop == null )
                continue;
            if( xpDrop.xp <= 0 )
            {
                dimXpDrops.remove( i );
                continue;
            }
            stack.push();
            float scale = 0.02f * (float) ( xpDrop.xp / xpDrop.getStartXp() ) * xpDrop.getSize() * worldXpDropsSizeMultiplier;
            Vector3d xpDropPos = xpDrop.getPos();
            int color = xpDrop.getColor();
            String text = "+" + DP.dpSoft( xpDrop.xp );
            if( Config.forgeConfig.worldXpDropsShowSkill.get() )
                text += " " + xpDrop.getSkill();
            float textWidth = fr.getStringWidth( text );
            float textOffset = -textWidth/2;
            stack.translate( xpDropPos.getX() - pos.getX(), xpDropPos.getY() - pos.getY(), xpDropPos.getZ() - pos.getZ() );
//            stack.translate( 20 - pos.getX(), 4 - pos.getY(), 12 - pos.getZ() );
            stack.rotate( mc.getRenderManager().getCameraOrientation() );
            stack.rotate( Vector3f.ZP.rotationDegrees( xpDrop.getRotation() ) );
            stack.scale( -scale, -scale, scale );
            IReorderingProcessor reorderingProcessor = IReorderingProcessor.fromString( text, XP.getColorStyle( color ) );
            fr.func_238416_a_( reorderingProcessor, textOffset, 0, color, true, stack.getLast().getMatrix(), buffer, false, color, 225 );
//        stack.scale( 1, -1, 1 );
//        fr.func_238416_a_( a, textOffset, 0, 0xffffff, false, stack.getLast().getMatrix(), buffer, false, 0xffffff, 2 );
            xpDrop.xp -= Math.max( 0.01523, xpDrop.xp * xpDrop.getDecaySpeed() * ( 1 + i * 0.01 ) ) * worldXpDropsDecaySpeedMultiplier * d;
            stack.pop();
        }
        stack.pop();
        buffer.finish();
        lastTime = currTime;
    }

    public static void addWorldXpDropOffline( WorldXpDrop xpDrop )
    {
        Minecraft mc = Minecraft.getInstance();
//        System.out.println( "remote xp drop added at " + xpDrop.getPos() );
        PlayerEntity player = mc.player;
        if( player != null && Config.getPreferencesMap( player ).getOrDefault( "worldXpDropsEnabled", 1D ) != 0 )
        {
            ResourceLocation dimResLoc = xpDrop.getWorldResLoc();
            if( !xpDrops.containsKey( dimResLoc ) )
                xpDrops.put( dimResLoc, new ArrayList<>() );
            xpDrops.get( dimResLoc ).add( xpDrop );
        }
    }
}