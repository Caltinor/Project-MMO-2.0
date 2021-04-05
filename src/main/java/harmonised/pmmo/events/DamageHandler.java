package harmonised.pmmo.events;

import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import java.util.Map;

public class DamageHandler
{
    public static double getEnduranceMultiplier( PlayerEntity player )
    {
        int enduranceLevel = Skill.getLevel( Skill.ENDURANCE.toString(), player );
        double endurancePerLevel = Config.forgeConfig.endurancePerLevel.get();
        double maxEndurance = Config.forgeConfig.maxEndurance.get();
        double endurePercent = (enduranceLevel * endurancePerLevel);
        if( endurePercent > maxEndurance )
            endurePercent = maxEndurance;
        endurePercent /= 100;
        return endurePercent;
    }

    public static double getFallSaveChance(PlayerEntity player )
    {
        int agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
        double maxFallSaveChance = Config.forgeConfig.maxFallSaveChance.get();
        double saveChancePerLevel = Math.min( maxFallSaveChance, Config.forgeConfig.saveChancePerLevel.get() / 100 );

        return agilityLevel * saveChancePerLevel;
    }

    public static void handleDamage( LivingDamageEvent event )
    {
        if( !(event.getEntity() instanceof FakePlayer) )
        {
            float damage = event.getAmount();
            float startDmg = damage;
            LivingEntity target = event.getEntityLiving();
            Entity source = event.getSource().getTrueSource();
            if( target instanceof ServerPlayerEntity )		//player hurt
            {
                boolean isFallDamage = event.getSource().getDamageType().equals( "fall" );
                ServerPlayerEntity player = (ServerPlayerEntity) target;
                ServerWorld world = player.getServerWorld();
//                int agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
//                damage -= agilityLevel / 50;
                double agilityXp = 0;
                double enduranceXp;
                boolean hideEndurance = false;
///////////////////////////////////////////////////////////////////////PARTY//////////////////////////////////////////////////////////////////////////////////////////
                if( source instanceof ServerPlayerEntity && !(source instanceof FakePlayer) )
                {
                    ServerPlayerEntity sourcePlayer = (ServerPlayerEntity) source;
                    Party party = PmmoSavedData.get().getParty( player.getUniqueID() );
                    if( party != null )
                    {
                        PartyMemberInfo sourceMemberInfo = party.getMemberInfo( sourcePlayer.getUniqueID() );
                        double friendlyFireMultiplier = Config.forgeConfig.partyFriendlyFireAmount.get() / 100D;

                        if( sourceMemberInfo != null )
                            damage *= friendlyFireMultiplier;
                    }
                }
///////////////////////////////////////////////////////////////////////ENDURANCE//////////////////////////////////////////////////////////////////////////////////////////
                double endured = Math.max( 0, damage * getEnduranceMultiplier( player ) );

                damage -= endured;

                enduranceXp = ( damage * 5 ) + ( endured * 7.5 );
///////////////////////////////////////////////////////////////////////FALL//////////////////////////////////////////////////////////////////////////////////////////////
                if( isFallDamage )
                {
                    double award;
                    int saved = 0;
                    double chance = getFallSaveChance( player );
                    for( int i = 0; i < damage; i++ )
                    {
                        if( Math.ceil( Math.random() * 100 ) <= chance )
                        {
                            saved++;
                        }
                    }
                    damage -= saved;

                    if( saved != 0 && player.getHealth() > damage )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.savedFall", saved ), true );

                    award = saved * 5;

                    agilityXp = award;
                }

                if( player.getHealth() > damage )
                {
                    if( agilityXp > 0 )
                        hideEndurance = true;

                    Vector3d pos = player.getPositionVec();

                    if( event.getSource().getTrueSource() != null )
                        XP.awardXp( player, Skill.ENDURANCE.toString(), event.getSource().getTrueSource().getDisplayName().getString(), enduranceXp, hideEndurance, false, false );
                    else
                        XP.awardXp( player, Skill.ENDURANCE.toString(), event.getSource().getDamageType(), enduranceXp, hideEndurance, false, false );
                    if( enduranceXp > 0 )
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), pos.getX(), pos.getY() + player.getEyeHeight() + 0.523, pos.getZ(), 1.523, enduranceXp, Skill.ENDURANCE.toString() );
                        XP.addWorldXpDrop( xpDrop, player );
                    }

                    if( agilityXp > 0 )
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), pos.getX(), pos.getY() + player.getEyeHeight() + 0.523, pos.getZ(), 1.523, agilityXp, Skill.AGILITY.toString() );
                        xpDrop.setSize( 1.523f );
                        XP.addWorldXpDrop( xpDrop, player );
                        XP.awardXp( player, Skill.AGILITY.toString(), "surviving " + startDmg + " fall damage", agilityXp, false, false, false );
                    }
                }
            }

///////////////////////////////////////Attacking////////////////////////////////////////////////////////////

            if ( target instanceof LivingEntity && event.getSource().getTrueSource() instanceof ServerPlayerEntity )
            {
//				IAttributeInstance test = target.getAttribute( Attributes.GENERIC_MOVEMENT_SPEED );
//				if( !(target instanceof AnimalEntity) )
//					System.out.println( test.getValue() + " " + test.getBaseValue() );

                ServerPlayerEntity player = (ServerPlayerEntity) event.getSource().getTrueSource();
                ServerWorld world = player.getServerWorld();

                if( XP.isHoldingDebugItemInOffhand( player ) )
                {
                    player.sendStatusMessage( new StringTextComponent( "regName:" + target.getEntityString() ), false );
                    player.sendStatusMessage( new StringTextComponent( "dmgType:" + event.getSource().damageType ), false );
                }

                if( XP.isPlayerSurvival( player ) )
                {
                    ItemStack mainItemStack = player.getHeldItemMainhand();
                    ResourceLocation mainResLoc = player.getHeldItemMainhand().getItem().getRegistryName();
                    ResourceLocation offResLoc = player.getHeldItemOffhand().getItem().getRegistryName();
                    Map<String, Double> weaponReq = XP.getJsonMap( mainResLoc, JType.REQ_WEAPON );
                    NBTHelper.maxDoubleMaps( weaponReq, XP.getJsonMap( offResLoc, JType.REQ_WEAPON ) );
                    String skill;
                    String itemSpecificSkill = AutoValues.getItemSpecificSkill( mainItemStack.getItem().getRegistryName().toString() );
                    boolean longDistanceCombatDamage = false;
                    boolean swordInMainHand = mainItemStack.getItem() instanceof SwordItem;

                    if( itemSpecificSkill != null )
                        skill = itemSpecificSkill;
                    else
                    {
                        if( event.getSource().damageType.equals( "arrow" ) )
                            skill = Skill.ARCHERY.toString();
                        else
                        {
                            skill = Skill.COMBAT.toString();
                            if( Util.getDistance( player.getPositionVec(), target.getPositionVec() ) > 4.20 + target.getWidth() + ( swordInMainHand ? 1.523 : 0 ) )
                                longDistanceCombatDamage = true;
                        }
                    }

                    if( Config.getConfig( "wearReqEnabled" ) != 0 && Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateWeaponReqDynamicallyEnabled" ) != 0 )
                        weaponReq.put( skill, weaponReq.getOrDefault( skill, AutoValues.getWeaponReqFromStack( mainItemStack ) ) );
                    int weaponGap = XP.getSkillReqGap( player, weaponReq );
                    int enchantGap = XP.getSkillReqGap( player, XP.getEnchantsUseReq( player.getHeldItemMainhand() ) );
                    int gap = Math.max( weaponGap, enchantGap );
                    if( gap > 0 )
                    {
                        if( enchantGap < gap )
                            NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToUseAsWeapon", player.getHeldItemMainhand().getTranslationKey(), "", true, 2 ), (ServerPlayerEntity) player );
                        if( Config.forgeConfig.strictReqWeapon.get() )
                        {
                            event.setCanceled( true );
                            return;
                        }
                    }

                    if( longDistanceCombatDamage )
                        skill = Skill.MAGIC.toString();

                    //Apply damage bonuses
//                    System.out.println( damage );
                    //Combat is taken care of in AttributeHandler
//                    if( skill.equals( Skill.COMBAT.toString() ) )
//                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelMelee.get();
                    if( skill.equals( Skill.ARCHERY.toString() ) )
                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelArchery.get();
                    else if( skill.equals( Skill.MAGIC.toString() ) )
                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelMagic.get();
//                    System.out.println( damage );

                    int killGap = 0;

                    if( target.getEntityString() != null )
                    {
                        killGap = XP.getSkillReqGap( player, XP.getResLoc( target.getEntityString() ), JType.REQ_KILL );
                        if( killGap > 0 )
                        {
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToDamage", new TranslationTextComponent( target.getType().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToDamage", new TranslationTextComponent( target.getType().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

                            for( Map.Entry<String, Double> entry : JsonConfig.data.get( JType.REQ_KILL ).get( target.getEntityString() ).entrySet() )
                            {
                                int level = Skill.getLevel( entry.getKey(), player );

                                if( level < entry.getValue() )
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                                else
                                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                            }
                        }
                        if( Config.forgeConfig.strictReqKill.get() )
                        {
                            event.setCanceled( true );
                            return;
                        }
                    }
                    float amount = 0;
                    float playerHealth = player.getHealth();
                    float targetHealth = target.getHealth();
                    float targetMaxHealth = target.getMaxHealth();
                    float lowHpBonus = 1.0f;

                    //apply damage penalties
                    damage /= weaponGap + 1;
                    damage /= killGap + 1;

                    //no overkill xp
                    if( damage > targetHealth )
                        damage = targetHealth;

                    amount += damage * 3;

                    //kill reduce xp
                    if ( startDmg >= targetHealth )
                        amount /= 2;

                    //max hp kill reduce xp
                    if( startDmg >= targetMaxHealth )
                        amount /= 1.5;

//					player.setHealth( 1f );

                    //reduce xp if passive mob
                    if( target instanceof AnimalEntity)
                        amount /= 2;
                    else if( playerHealth <= 10 )   //increase xp if aggresive mob and player low on hp
                    {
                        lowHpBonus += ( 11 - playerHealth ) / 5;
                        if( playerHealth <= 2 )
                            lowHpBonus += 1;
                    }

                    if( skill.equals( Skill.ARCHERY.toString() ) || skill.equals( Skill.MAGIC.toString() ) )
                    {
                        double distance = event.getEntity().getDistance( player );
                        if( distance > 16 )
                            distance -= 16;
                        else
                            distance = 0;

                        amount += ( Math.pow( distance, 1.25 ) * ( damage / target.getMaxHealth() ) * ( damage >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp
                        amount *= lowHpBonus;
                    }
                    else
                        amount *= lowHpBonus;

                    Vector3d xpDropPos = target.getPositionVec();
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), xpDropPos.getX(), xpDropPos.getY() + target.getHeight(), xpDropPos.getZ(), target.getHeight(), amount, skill );
                    XP.addWorldXpDrop( xpDrop, player );
                    XP.awardXp( player, skill, player.getHeldItemMainhand().getDisplayName().toString(), amount, false, false, false );

                    if( weaponGap > 0 )
                        player.getHeldItemMainhand().damageItem( weaponGap - 1, player, (a) -> a.sendBreakAnimation(Hand.MAIN_HAND ) );
                }
            }
            event.setAmount( damage );
            if( event.getAmount() <= 0 )
                event.setCanceled( true );
        }
    }
}