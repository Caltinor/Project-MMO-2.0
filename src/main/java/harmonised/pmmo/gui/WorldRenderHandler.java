package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class WorldRenderHandler
{
    private static long lastTime = System.nanoTime();
    private final static FontRenderer fr = Minecraft.getInstance().fontRenderer;
    private final static Minecraft mc = Minecraft.getInstance();
    private final static IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
    private final static Map<ResourceLocation, List<WorldXpDrop>> xpDrops = new HashMap<>();
    private final static Map<ResourceLocation, List<WorldText>> worldTexts = new HashMap<>();
    private static long currTime;
    private static double d;

    @SubscribeEvent
    public void handleWorldRender( RenderWorldLastEvent event )
    {
        currTime = System.nanoTime();
        d = (currTime - lastTime) / 1000000000D;
        renderWorldText( event );
        renderWorldXpDrops( event );
        if( XP.isPlayerSurvival( mc.player ) )
            drawBoxHighlights( event );
    }

    public static void drawBoxHighlights( RenderWorldLastEvent event )
    {
        MatrixStack stack = event.getMatrixStack();
        Vector3d cameraPos = mc.getRenderManager().info.getProjectedView();
        stack.push();
        stack.translate( -cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ() );
        IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer( RenderType.getLines() );

        if( XPOverlayGUI.getVeinKey() && XPOverlayGUI.getLookingAtBlock() && XPOverlayGUI.getMetToolReq() && XPOverlayGUI.getCanBreak() )
        {
            for( BlockPos pos : XPOverlayGUI.veinShapeSet )
            {
                drawBoxHighlight( stack, builder, pos );
//                System.out.println( pos );
            }
        }
        stack.pop();
        RenderSystem.disableDepthTest();
        buffer.finish();
    }

    public static void drawBoxHighlight( MatrixStack stack, IVertexBuilder builder, BlockPos pos )
    {
        stack.push();
        Matrix4f matrix4f = stack.getLast().getMatrix();
        int red = 255;
        int green = 0;
        int blue = 255;
        int alpha = 255;

        for( int i = 0; i < 12; i++ )
        {
            int mode = i/4, j = i%4;
            float modulus = j%2, divide = j/2;
            switch( mode )
            {
                case 0:
                    builder.pos( matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ() ).color( red, green, blue, alpha ).endVertex();
                    builder.pos( matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ() + 1 ).color( red, green, blue, alpha ).endVertex();
                    break;

                case 1:
                    builder.pos( matrix4f, pos.getX(), pos.getY() + modulus, pos.getZ() + divide ).color( red, green, blue, alpha ).endVertex();
                    builder.pos( matrix4f, pos.getX() + 1, pos.getY() + modulus, pos.getZ() + divide ).color( red, green, blue, alpha ).endVertex();
                    break;

                case 2:
                    builder.pos( matrix4f, pos.getX() + divide, pos.getY(), pos.getZ() + modulus ).color( red, green, blue, alpha ).endVertex();
                    builder.pos( matrix4f, pos.getX() + divide, pos.getY() + 1, pos.getZ() + modulus ).color( red, green, blue, alpha ).endVertex();
                    break;
            }
        }
        stack.pop();
    }

    public static void drawText( MatrixStack stack, Vector3d cameraPos, Vector3d textPos, String text, float scale, float rotation, int color )
    {
        stack.push();
        float textWidth = fr.getStringWidth( text );
        float textOffset = -textWidth/2;
        stack.translate( textPos.getX() - cameraPos.getX(), textPos.getY() - cameraPos.getY(), textPos.getZ() - cameraPos.getZ() );
//            stack.translate( 20 - pos.getX(), 4 - pos.getY(), 12 - pos.getZ() );
        stack.rotate( mc.getRenderManager().getCameraOrientation() );
        stack.rotate( Vector3f.ZP.rotationDegrees( rotation ) );
        stack.scale( -scale, -scale, scale );
        IReorderingProcessor reorderingProcessor = IReorderingProcessor.fromString( text, XP.getColorStyle( color ) );
        fr.func_238416_a_( reorderingProcessor, textOffset, 0, color, true, stack.getLast().getMatrix(), buffer, false, color, 225 );
//        stack.scale( 1, -1, 1 );
//        fr.func_238416_a_( a, textOffset, 0, 0xffffff, false, stack.getLast().getMatrix(), buffer, false, 0xffffff, 2 );
        stack.pop();
    }

    private static void renderWorldText( RenderWorldLastEvent event )
    {
        //        temp1++;
        World world = mc.world;
        if( world == null )
            return;
//        ResourceLocation dimResLoc = world.getDimensionType().getEffects();
        ResourceLocation dimResLoc = XP.getDimResLoc( world );
        if( !worldTexts.containsKey( dimResLoc ) )
            return;
        List<WorldText> dimTexts = worldTexts.get( dimResLoc );
        MatrixStack stack = event.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
        Vector3d cameraPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        stack.push();
        for( int i = dimTexts.size()-1; i >= 0; i-- )
        {
            WorldText worldText = dimTexts.get( i );
            if( worldText == null )
                continue;
            if( !worldText.tick( d * ( 1 + i*0.01 ) ) )
            {
                dimTexts.remove( i );
                continue;
            }
            drawText( stack, cameraPos, worldText.getPos(), worldText.getText(), (float) worldText.getSize(), worldText.getRotation(), worldText.getColor() );
        }
        stack.pop();
        buffer.finish();
        lastTime = currTime;
    }

    private static void renderWorldXpDrops( RenderWorldLastEvent event )
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
        MatrixStack stack = event.getMatrixStack();
        Vector3d cameraPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

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
            float scale = 0.02f * ( xpDrop.xp / xpDrop.getStartXp() ) * xpDrop.getSize() * WorldText.worldXpDropsSizeMultiplier;
            int color = xpDrop.getColor();
            String text = "+" + DP.dpSoft( xpDrop.xp );
            if( WorldText.worldXpDropsShowSkill )
                text += " " + xpDrop.getSkill();
            drawText( stack, cameraPos, xpDrop.getPos(), text, scale, xpDrop.getRotation(), color );
            xpDrop.xp -= Math.max( 0.01523, xpDrop.xp * xpDrop.getDecaySpeed() * ( 1 + i * 0.01 ) ) * WorldText.worldXpDropsDecaySpeedMultiplier * d;
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
            {
                xpDrops.put( dimResLoc, new ArrayList<>() );
                lastTime = System.nanoTime();
            }
            xpDrops.get( dimResLoc ).add( xpDrop );
        }
    }

    public static void addWorldTextOffline( WorldText worldText )
    {
        Minecraft mc = Minecraft.getInstance();
//        System.out.println( "remote xp drop added at " + xpDrop.getPos() );
        PlayerEntity player = mc.player;
//        if( player != null && Config.getPreferencesMap( player ).getOrDefault( "worldXpDropsEnabled", 1D ) != 0 )
//        {
            ResourceLocation dimResLoc = worldText.getWorldResLoc();
            if( !worldTexts.containsKey( dimResLoc ) )
            {
                worldTexts.put( dimResLoc, new ArrayList<>() );
                lastTime = System.nanoTime();
            }
            worldTexts.get( dimResLoc ).add( worldText );
//        }
    }
}