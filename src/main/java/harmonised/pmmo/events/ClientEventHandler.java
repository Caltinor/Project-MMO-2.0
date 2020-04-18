package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.PMMOPoseSetter;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasCrawling = false;

    public static void subscribeClientEvents( IEventBus eventBus )
    {
        eventBus.register( harmonised.pmmo.events.ClientEventHandler.class );
    }

    @SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( Minecraft.getInstance().player != null )
        {
            if( wasCrawling != ClientHandler.CRAWL_KEY.isKeyDown() )
            {
                wasCrawling = ClientHandler.CRAWL_KEY.isKeyDown();
                NetworkHandler.sendToServer( new MessageCrawling( ClientHandler.CRAWL_KEY.isKeyDown() ) );
            }

            if( ClientHandler.CRAWL_KEY.isKeyDown() )
                XP.isCrawling.add( Minecraft.getInstance().player.getUniqueID() );
            else
                XP.isCrawling.remove( Minecraft.getInstance().player.getUniqueID() );
        }
    }

    private static void addTooltipTextSkill( String tKey, String type, Map<String, Object> theMap, ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();
        List<ITextComponent> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        int level, value;

        if( theMap.size() > 0 )
        {
            if( XP.checkReq( player, item.getRegistryName(), type ) )
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            for( String key : theMap.keySet() )
            {
                if(XPOverlayGUI.skills.containsKey( key ))
                    level = XP.levelAtXp( XPOverlayGUI.skills.get( key ).goalXp );
                else
                    level = 1;

                if( theMap.get( key ) instanceof Double )
                {
                    value = (int) Math.floor( (double) theMap.get( key ) );

                    if( level < value )
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                }
            }
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();

        if( player != null )
        {
            ItemStack itemStack = event.getItemStack();
            Item item = itemStack.getItem();
            List<ITextComponent> tooltip = event.getToolTip();
            int level;
            String regKey = item.getRegistryName().toString();
            float hardness;
            double dValue;
            Material material = null;

            Map<String, Object> wearReq = Requirements.wearReq.get( regKey );
            Map<String, Object> toolReq = Requirements.toolReq.get( regKey );
            Map<String, Object> weaponReq = Requirements.weaponReq.get( regKey );
            Map<String, Object> useReq = Requirements.useReq.get( regKey );
            Map<String, Object> placeReq = Requirements.placeReq.get( regKey );
            Map<String, Object> breakReq = Requirements.breakReq.get( regKey );
            Map<String, Object> xpValue = Requirements.xpValue.get( regKey );

            if( xpValue != null && xpValue.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.xpValue" ) );

                for( String key : xpValue.keySet() )
                {
                    if( xpValue.get( key ) instanceof Double )
                    {
                        dValue = (double) xpValue.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( item instanceof BlockItem )
            {
                material = ( (BlockItem) item).getBlock().getDefaultState().getMaterial();

                hardness = ((BlockItem) item).getBlock().getBlockHardness( ((BlockItem) item).getBlock().getDefaultState(), null, null );
                if( hardness > 0 )
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text.hardness", DP.dp( hardness ) ).getString() ) );
            }

            if( wearReq != null && wearReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.armor", "wear", wearReq, event );

            if( toolReq != null && toolReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.tool", "tool", toolReq, event );

            if( weaponReq != null && weaponReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.weapon", "weapon", weaponReq, event );

            if( useReq != null && useReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.use", "use", useReq, event );

//            if( wearReq != null && wearReq.size() > 0 )
//                addTooltipTextSkill( "pmmo.text.wear", "mob", mobReq, event );

            if( placeReq != null && placeReq.size() > 0 )
            {
                if( Requirements.plantInfo.containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.text.plant", "place", placeReq, event );
                else
                    addTooltipTextSkill( "pmmo.text.placeDown", "place", placeReq, event );
            }

            if( breakReq != null && breakReq.size() > 0 )
            {
                if( XP.correctHarvestTool( material ).equals( "axe" ) )
                    addTooltipTextSkill( "pmmo.text.chop", "break", breakReq, event );
                else if( Requirements.plantInfo.containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.text.harvest", "break", breakReq, event );
                else
                    addTooltipTextSkill( "pmmo.text.break", "break", breakReq, event );
            }

            if( XP.getExtraChance( player, item.getRegistryName(), "ore" ) > 0 )  //ORE EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraChance", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "ore" ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.oreInfo.containsKey( regKey ) && Requirements.oreInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            if( XP.getExtraChance( player, item.getRegistryName(), "log" ) > 0 )  //LOG EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraChance", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "log" ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.logInfo.containsKey( regKey ) && Requirements.logInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            if( XP.getExtraChance( player, item.getRegistryName(), "plant" ) > 0 )  //PLANT EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraChance", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "plant" ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.plantInfo.containsKey( regKey ) && Requirements.plantInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            if( Requirements.salvageInfo.containsKey( regKey ) && Requirements.salvageInfo.get( regKey ).containsKey( "salvageItem" ) )
            {
                Item salvageItem = ForgeRegistries.ITEMS.getValue( item.getRegistryName() );

                if( salvageItem != null )
                {
                    for( Map.Entry<String, Object> entry : Requirements.salvageInfo.get( regKey ).entrySet() )
                    {
//                        System.out.println( entry );
                    }
                }
            }

            if( Requirements.salvageInfo.containsKey( item.getRegistryName().toString() ) && !XP.getItem( (String) Requirements.salvageInfo.get( item.getRegistryName().toString() ).get( "salvageItem" ) ).equals( Items.AIR ) )
            {
                Map<String, Object> theMap = Requirements.salvageInfo.get( item.getRegistryName().toString() );

                level = XP.getLevel( "repairing", player );
                double baseChance = (double) theMap.get( "baseChance" );
                double chancePerLevel = (double) theMap.get( "chancePerLevel" );
                double maxSalvageMaterialChance = Config.config.maxSalvageMaterialChance.get();
                double chance = baseChance + ( chancePerLevel * level );

                if( chance > maxSalvageMaterialChance )
                    chance = maxSalvageMaterialChance;

                int salvageMax = (int) Math.floor( (double) theMap.get( "salvageMax" ) );
                double durabilityPercent = ( 1.00f - ( (double) itemStack.getDamage() / (double) itemStack.getMaxDamage() ) );
                int potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );
                Item salvageItem = XP.getItem( (String) theMap.get( "salvageItem" ) );


                if( potentialReturnAmount > 0 )
                    tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesInto", new TranslationTextComponent( item.getTranslationKey() ), potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                tooltip.add( new TranslationTextComponent( "pmmo.text.salvageChance", chance ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            }
        }
    }
}