package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.ScreenshotHandler;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.PlacedBlocks;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockBrokenHandler
{
    public static void handleBroken( BlockEvent.BreakEvent event )
    {
        if( event.getPlayer() instanceof PlayerEntity && !(event.getPlayer() instanceof FakePlayer) )
        {
            PlayerEntity player = event.getPlayer();

            if( XP.isPlayerSurvival( player ) )
            {
                BlockState state = event.getState();
                Block block = state.getBlock();
                World world = event.getWorld().getWorld();
                Material material = event.getState().getMaterial();

                // DEBUG

//				if( debugInt-- > 0 )
//				{
//					World debugWorld = event.getWorld().getWorld();
//					BlockPos debugPos = event.getPos().south();
//					BlockState debugBlockState = debugWorld.getBlockState( debugPos );
//					BreakEvent debugEvent = new BreakEvent( debugWorld, debugPos, debugBlockState, player );
//					MinecraftForge.EVENT_BUS.post( debugEvent );
//					if( !debugEvent.isCanceled() )
//						debugWorld.removeBlock( debugPos, false );
//
//					System.out.println( debugPos );
//				}

                // DEBUG

                Block blockAbove = world.getBlockState( event.getPos().up() ).getBlock();
                boolean passedBreakReq;

                if( JsonConfig.data.get( "plantInfo" ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable);
                passedBreakReq = XP.checkReq( player, blockAbove.getRegistryName(), "break" );

                if( !passedBreakReq )
                    block = blockAbove;
                else
                    passedBreakReq = XP.checkReq( player, block.getRegistryName(), "break" );

                if( passedBreakReq )
                {
                    double blockHardnessLimit = Config.forgeConfig.blockHardnessLimit.get();
                    boolean wasPlaced = PlacedBlocks.isPlayerPlaced( event.getWorld().getWorld(), event.getPos() );
                    ItemStack toolUsed = player.getHeldItemMainhand();
                    String skill = XP.getSkill( material ).name().toLowerCase();
//					String regKey = block.getRegistryName().toString();
                    double hardness = block.getBlockHardness( block.getDefaultState(), event.getWorld(), event.getPos() );
                    if( hardness > blockHardnessLimit )
                        hardness = blockHardnessLimit;

                    String awardMsg = "";
                    Map<String, Double> award = new HashMap<>();
                    award.put( skill, hardness );

                    Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments( player.getHeldItemMainhand() );
                    int fortune = 0;
                    if( enchants.get( Enchantments.FORTUNE ) != null )
                        fortune = enchants.get( Enchantments.FORTUNE );

                    List<ItemStack> drops;

                    if( world instanceof ServerWorld)
                    {
                        LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                                .withRandom(world.rand)
                                .withParameter( LootParameters.POSITION, event.getPos() )
                                .withParameter( LootParameters.TOOL, toolUsed )
                                .withParameter( LootParameters.THIS_ENTITY, player )
                                .withNullableParameter( LootParameters.BLOCK_ENTITY, world.getTileEntity( event.getPos() ) );
                        if (fortune > 0)
                        {
                            builder.withLuck(fortune);
                        }
                        drops = block.getDrops( event.getState(), builder );
                    }
                    else
                        drops = new ArrayList<>();

                    Block sugarCane = Blocks.SUGAR_CANE;
                    Block cactus = Blocks.CACTUS;
                    Block kelp = Blocks.KELP_PLANT;
                    Block bamboo = Blocks.BAMBOO;

                    if( block.equals( sugarCane ) || block.equals( cactus ) || block.equals( kelp ) || block.equals( bamboo ) ) //Handle Sugar Cane / Cactus
                    {
                        Block baseBlock = event.getState().getBlock();
                        BlockPos baseBlockPos = event.getPos();

                        double extraChance = XP.getExtraChance( player, block.getRegistryName(), "plant" ) / 100;
                        int rewardable, guaranteedDrop, extraDrop, totalDrops, guaranteedDropEach;
                        rewardable = extraDrop = guaranteedDrop = totalDrops = 0;

                        guaranteedDropEach = (int) Math.floor( extraChance );
                        extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;

                        int height = 0;
                        BlockPos currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
                        block =  world.getBlockState( currBlockPos ).getBlock();
                        for( ; ( block.equals( baseBlock ) ); )
                        {
                            wasPlaced = PlacedBlocks.isPlayerPlaced( world, currBlockPos );
                            if( !wasPlaced )
                            {
                                rewardable++;
                                guaranteedDrop += guaranteedDropEach;

                                if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
                                    extraDrop++;
                            }
                            height++;
                            currBlockPos = new BlockPos( baseBlockPos.getX(), baseBlockPos.getY() + height, baseBlockPos.getZ() );
                            block =  world.getBlockState( currBlockPos ).getBlock();
                        }

                        int dropsLeft = guaranteedDrop + extraDrop;

                        if( dropsLeft > 0 )
                        {
                            XP.dropItems( dropsLeft, block.asItem(), world, event.getPos() );
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + dropsLeft, drops.get( 0 ).getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                        }

                        totalDrops = rewardable + dropsLeft;
                        award = XP.addMaps( award, XP.multiplyMap( XP.getXp( baseBlock.getRegistryName() ), totalDrops ) );

                        awardMsg = "removing " + height + " + " + ( guaranteedDrop + extraDrop ) + " extra";
                    }
                    else if( ( material.equals( Material.PLANTS ) || material.equals( Material.OCEAN_PLANT ) || material.equals( Material.TALL_PLANTS ) ) && drops.size() > 0 ) //IS PLANT
                    {
                        ItemStack theDropItem = drops.get( 0 );

                        int age = -1;
                        int maxAge = -1;

                        if( !wasPlaced )
                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), theDropItem.getCount() ) );

                        if( state.has( BlockStateProperties.AGE_0_1 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_1 );
                            maxAge = 1;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_2 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_2 );
                            maxAge = 2;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_3 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_3 );
                            maxAge = 3;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_5 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_5 );
                            maxAge = 5;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_7 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_7 );
                            maxAge = 7;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_15) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_15 );
                            maxAge = 15;
                        }
                        else if( state.has( BlockStateProperties.AGE_0_25 ) )
                        {
                            age = state.get( BlockStateProperties.AGE_0_25 );
                            maxAge = 25;
                        }
                        else if( state.has( BlockStateProperties.PICKLES_1_4 ) )
                        {
                            age = state.get( BlockStateProperties.PICKLES_1_4 );
                            maxAge = 4;
                            if( wasPlaced )
                                return;
                        }

                        if( age == maxAge && age >= 0 || block instanceof SeaPickleBlock)
                        {
                            award = XP.addMaps( award, XP.getXp( block.getRegistryName() ) );

                            double extraChance = XP.getExtraChance( player, block.getRegistryName(), "plant" ) / 100;
                            int guaranteedDrop = 0;
                            int extraDrop = 0;

                            guaranteedDrop = (int) Math.floor( extraChance );
                            extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;

                            if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
                                extraDrop = 1;

                            if( guaranteedDrop + extraDrop > 0 )
                            {
                                XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                            }

                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), guaranteedDrop + extraDrop ) );
                            awardMsg = "harvesting " + ( theDropItem.getCount() ) + " + " + ( guaranteedDrop + extraDrop ) + " crops";
                        }
                        else if( !wasPlaced )
                            awardMsg = "breaking a plant";
                    }
                    else if( XP.getExtraChance( player, block.getRegistryName(), "ore" ) > 0 )		//IS ORE
                    {
                        boolean isSilk = enchants.get( Enchantments.SILK_TOUCH ) != null;
                        boolean noDropOre = false;
                        if( drops.size() > 0 )
                            noDropOre = block.asItem().equals( drops.get(0).getItem() );

                        if( !wasPlaced && !isSilk )
                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), drops.get( 0 ).getCount() ) );

                        if( noDropOre && !wasPlaced || !noDropOre && !isSilk )			//EXTRA DROPS
                        {
                            double extraChance = XP.getExtraChance( player, block.getRegistryName(), "ore" ) / 100;

                            int guaranteedDrop = 0;
                            int extraDrop = 0;

                            guaranteedDrop = (int)Math.floor( extraChance );
                            extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;


                            if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
                                extraDrop = 1;

                            if( !noDropOre && wasPlaced )
                                award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), ( drops.get( 0 ).getCount() ) ) );

                            awardMsg = "mining a block";

                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), ( guaranteedDrop + extraDrop ) ) );
                            if( guaranteedDrop + extraDrop > 0 )
                            {
                                XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                            }
                        }
                        else
                            awardMsg = "mining a block";
                    }
                    else if( XP.getExtraChance( player, block.getRegistryName(), "log" ) > 0 )
                    {
                        if( !wasPlaced )			//EXTRA DROPS
                        {
                            double extraChance = XP.getExtraChance( player, block.getRegistryName(), "log" ) / 100;

                            int guaranteedDrop = 0;
                            int extraDrop = 0;

                            if( ( extraChance ) > 1 )
                            {
                                guaranteedDrop = (int)Math.floor( extraChance );
                                extraChance = ( ( extraChance ) - Math.floor( extraChance ) ) * 100;
                            }

                            extraChance *= 100;

                            if( Math.ceil( Math.random() * 1000 ) <= extraChance * 10 )
                                extraDrop = 1;

                            if( guaranteedDrop + extraDrop > 0 )
                            {
                                XP.dropItems( guaranteedDrop + extraDrop, drops.get( 0 ).getItem(), world, event.getPos() );
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.extraDrop", "" + (guaranteedDrop + extraDrop), drops.get( 0 ).getItem().getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
                            }

                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), ( drops.get( 0 ).getCount() + guaranteedDrop + extraDrop ) ) );

                            awardMsg = "cutting a block";
                        }
                        else
                            awardMsg = "cutting a block";
                    }
                    else
                    {
                        if( !wasPlaced )
                        {
                            award = XP.addMaps( award, XP.multiplyMap( XP.getXp( block.getRegistryName() ), drops.size() ) );
                            PlacedBlocks.removeOre( event.getWorld().getWorld(), event.getPos() );
                        }

                        switch( XP.getSkill( material ) )
                        {
                            case MINING:
                                awardMsg = "mining a block";
                                break;

                            case WOODCUTTING:
                                awardMsg = "cutting a block";
                                break;

                            case EXCAVATION:
                                awardMsg = "digging a block";
                                break;

                            case FARMING:
                                awardMsg = "harvesting";
                                break;

                            default:
//								System.out.println( "INVALID SKILL ON BREAK" );
                                break;
                        }
                    }

                    int gap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), "tool" );

                    if( gap > 0 )
                        player.getHeldItemMainhand().damageItem( gap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );

                    for( String skillName : award.keySet() )
                    {
                        XP.awardXp( player, Skill.getSkill( skillName ), awardMsg, award.get( skillName ) / (gap + 1), false );
                    }
                }
                else
                {
                    int startLevel;

                    if( XP.correctHarvestTool( material ).equals( "axe" ) )
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toChop", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toChop", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
                    }
                    else if( JsonConfig.data.get( "plantInfo" ).containsKey( blockAbove.getRegistryName().toString() ) || block instanceof IPlantable )
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toHarvest", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toHarvest", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
                    }
                    else
                    {
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toBreak", block.getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                        NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.toBreak", block.getTranslationKey(), "", false, 2 ), (ServerPlayerEntity) player );
                    }

                    for( Map.Entry<String, Object> entry : JsonConfig.data.get( "breakReq" ).get( block.getRegistryName().toString() ).entrySet() )
                    {
                        startLevel = XP.getLevel( Skill.getSkill( entry.getKey() ), player );

                        double entryValue = 1;
                        if( entry.getValue() instanceof Double )
                            entryValue = (double) entry.getValue();

                        if( startLevel < entryValue )
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 2 ), (ServerPlayerEntity) player );
                        else
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.levelDisplay", "pmmo." + entry.getKey(), "" + (int) Math.floor( entryValue ), false, 1 ), (ServerPlayerEntity) player );
                    }

                    event.setCanceled( true );
                }
            }
        }
    }
}
