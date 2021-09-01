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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Map;

public class DamageHandler
{
    public static double getEnduranceMultiplier( Player player )
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

    public static double getFallSaveChance(Player player )
    {
        int agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
        double maxFallSaveChance = Config.forgeConfig.maxFallSaveChance.get();
        double saveChancePerLevel = Math.min( maxFallSaveChance, Config.forgeConfig.saveChancePerLevel.get() / 100 );

        return agilityLevel * saveChancePerLevel;
    }

    public static void handleDamage( LivingHurtEvent event )
    {
        if( !(event.getEntity() instanceof FakePlayer) )
        {
            float damage = event.getAmount();
            //Anti ghast crazy dmg
            if( event.getSource() instanceof IndirectEntityDamageSource )
            {
                IndirectEntityDamageSource indirectSource = (IndirectEntityDamageSource) event.getSource();
                if( indirectSource.getImmediateSource() instanceof FireballEntity )
                    damage = Math.min( 69, damage );
            }
            float startDmg = damage;
            LivingEntity target = event.getEntityLiving();
            Entity source = event.getSource().getEntity();
            if( target instanceof ServerPlayer )		//player hurt
            {
                boolean isFallDamage = event.getSource().getMsgId().equals( "fall" );
                ServerPlayer player = (ServerPlayer) target;
                ServerLevel world = player.getLevel();
//                int agilityLevel = Skill.getLevel( Skill.AGILITY.toString(), player );
//                damage -= agilityLevel / 50;
                double agilityXp = 0;
                double enduranceXp;
                boolean hideEndurance = false;
///////////////////////////////////////////////////////////////////////PARTY//////////////////////////////////////////////////////////////////////////////////////////
                if( source instanceof ServerPlayer && !(source instanceof FakePlayer) )
                {
                    ServerPlayer sourcePlayer = (ServerPlayer) source;
                    Party party = PmmoSavedData.get().getParty( player.getUUID() );
                    if( party != null )
                    {
                        PartyMemberInfo sourceMemberInfo = party.getMemberInfo( sourcePlayer.getUUID() );
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
                        player.displayClientMessage( new TranslatableComponent( "pmmo.savedFall", saved ), true );

                    award = saved * 5;

                    agilityXp = award;
                }

                if( player.getHealth() > damage )
                {
                    if( agilityXp > 0 )
                        hideEndurance = true;

                    Vec3 pos = player.position();

                    if( event.getSource().getEntity() != null )
                        XP.awardXp( player, Skill.ENDURANCE.toString(), event.getSource().getEntity().getDisplayName().getString(), enduranceXp, hideEndurance, false, false );
                    else
                        XP.awardXp( player, Skill.ENDURANCE.toString(), event.getSource().getMsgId(), enduranceXp, hideEndurance, false, false );
                    if( enduranceXp > 0 )
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), pos.x(), pos.y() + player.getEyeHeight() + 0.523, pos.z(), 1.523, enduranceXp, Skill.ENDURANCE.toString() );
                        XP.addWorldXpDrop( xpDrop, player );
                    }

                    if( agilityXp > 0 )
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), pos.x(), pos.y() + player.getEyeHeight() + 0.523, pos.z(), 1.523, agilityXp, Skill.AGILITY.toString() );
                        xpDrop.setSize( 1.523f );
                        XP.addWorldXpDrop( xpDrop, player );
                        XP.awardXp( player, Skill.AGILITY.toString(), "surviving " + startDmg + " fall damage", agilityXp, false, false, false );
                    }
                }
            }

///////////////////////////////////////Attacking////////////////////////////////////////////////////////////

            if ( target instanceof LivingEntity && event.getSource().getEntity() instanceof ServerPlayer )
            {
                ServerPlayer player = (ServerPlayer) event.getSource().getEntity();
                ServerLevel world = player.getLevel();

                if( XP.isHoldingDebugItemInOffhand( player ) )
                {
                    player.displayClientMessage( new TextComponent( "regName:" + target.getEncodeId() ), false );
                    player.displayClientMessage( new TextComponent( "dmgType:" + event.getSource().msgId ), false );
                }

                if( XP.isPlayerSurvival( player ) )
                {
                    ItemStack mainItemStack = player.getMainHandItem();
                    ResourceLocation mainResLoc = player.getMainHandItem().getItem().getRegistryName();
                    ResourceLocation offResLoc = player.getOffhandItem().getItem().getRegistryName();
                    Map<String, Double> weaponReq = XP.getXpBypass( mainResLoc, JType.REQ_WEAPON );
                    NBTHelper.maxDoubleMaps( weaponReq, XP.getXpBypass( offResLoc, JType.REQ_WEAPON ) );
                    String skill;
                    String itemSpecificSkill = AutoValues.getItemSpecificSkill( mainItemStack.getItem().getRegistryName().toString() );
                    boolean swordInMainHand = mainItemStack.getItem() instanceof SwordItem;

                    if( itemSpecificSkill != null )
                        skill = itemSpecificSkill;
                    else
                    {
                        if( event.getSource().msgId.equals( "arrow" ) )
                            skill = Skill.ARCHERY.toString();
                        else
                        {
                            skill = Skill.COMBAT.toString();
                            if( Util.getDistance( player.position(), target.position() ) > 4.20 + target.getBbWidth() + ( swordInMainHand ? 1.523 : 0 ) )
                                skill = Skill.MAGIC.toString(); //Magically far melee damage
                        }
                    }

                    int killGap = 0;
                    int weaponGap = 0;
                    if( Config.getConfig( "weaponReqEnabled" ) != 0 )
                    {
                        if( Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateWeaponReqDynamicallyEnabled" ) != 0 )
                            weaponReq.put( skill, weaponReq.getOrDefault( skill, AutoValues.getWeaponReqFromStack( mainItemStack ) ) );
                        weaponGap = XP.getSkillReqGap( player, weaponReq );
                        int enchantGap = XP.getSkillReqGap( player, XP.getEnchantsUseReq( player.getMainHandItem() ) );
                        int gap = Math.max( weaponGap, enchantGap );
                        if( gap > 0 )
                        {
                            if( enchantGap < gap )
                                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.notSkilledEnoughToUseAsWeapon", player.getMainHandItem().getDescriptionId(), "", true, 2 ), (ServerPlayer) player );
                            if( Config.forgeConfig.strictReqWeapon.get() )
                            {
                                event.setCanceled( true );
                                return;
                            }
                        }
                    }

                    //Apply damage bonuses
                    //Combat is taken care of in AttributeHandler
//                    if( skill.equals( Skill.COMBAT.toString() ) )
//                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelMelee.get();
                    if( skill.equals( Skill.ARCHERY.toString() ) )
                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelArchery.get();
                    else if( skill.equals( Skill.MAGIC.toString() ) )
                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelMagic.get();
                    else if( skill.equals( Skill.GUNSLINGING.toString() ) )
                        damage *= 1 + Skill.getLevel( skill, player ) * Config.forgeConfig.damageBonusPercentPerLevelGunslinging.get();

                    if( target.getEncodeId() != null )
                    {
                        killGap = Math.max( killGap, XP.getSkillReqGap( player, XP.getResLoc( target.getEncodeId() ), JType.REQ_KILL ) );
                        if( killGap > 0 )
                        {
                            player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToDamage", new TranslatableComponent( target.getType().getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
                            player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToDamage", new TranslatableComponent( target.getType().getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), false );

                            XP.sendPlayerSkillList( player, JsonConfig.data.get( JType.REQ_KILL ).get( target.getEncodeId() ) );

                            if( Config.forgeConfig.strictReqKill.get() )
                            {
                                event.setCanceled( true );
                                return;
                            }
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
                    amount += Math.min( damage, targetHealth ) * 3;

                    //kill reduce xp
                    if ( startDmg >= targetHealth )
                        amount /= 2;

                    //max hp kill reduce xp
                    if( startDmg >= targetMaxHealth )
                        amount /= 1.5;

//					player.setHealth( 1f );

                    //reduce xp if passive mob
                    if( target instanceof Animal)
                        amount /= 2;
                    else if( playerHealth <= 10 )   //increase xp if aggresive mob and player low on hp
                    {
                        lowHpBonus += ( 11 - playerHealth ) / 5;
                        if( playerHealth <= 2 )
                            lowHpBonus += 1;
                    }
                    double distance = Util.getHorizontalDistance( event.getEntity().position(), player.position() );

                    if( skill.equals( Skill.COMBAT.toString() ) )
                        amount *= lowHpBonus;
                    else if( skill.equals( Skill.ARCHERY.toString() ) )
                    {
                        if( distance > 16 )
                            distance -= 16;
                        else
                            distance = 0;

                        amount += ( Math.pow( distance, 1.3251 ) * damage * 0.5f * ( damage / target.getMaxHealth() ) * ( damage >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp
                        amount *= lowHpBonus;
                    }
                    else
                    {
                        if( distance > 32 )
                            distance -= 32;
                        else
                            distance = 0;

                        amount += ( Math.pow( distance, 1.1523 ) * damage * 0.05f * ( damage / target.getMaxHealth() ) * ( damage >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp
//                        amount += ( Math.pow( distance, 1.1523 ) * damage * 0.05f * ( damage >= targetMaxHealth ? 1.5 : 1 ) );	//add distance xp
                        amount *= lowHpBonus;
                    }

                    Vec3 xpDropPos = target.position();
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ( XP.getDimResLoc( world ), xpDropPos.x(), xpDropPos.y() + target.getBbHeight(), xpDropPos.z(), target.getBbHeight(), amount, skill );
                    XP.addWorldXpDrop( xpDrop, player );
                    Map<String, Double> entityMap = XP.getXp( target, JType.XP_MULTIPLIER_ENTITY );
                    if( entityMap.containsKey( skill ) )
                        amount *= entityMap.get( skill );
                    XP.awardXp( player, skill, player.getMainHandItem().getHoverName().toString(), amount, false, false, false );

                    if( weaponGap > 0 )
                        player.getMainHandItem().hurtAndBreak( weaponGap - 1, player, (a) -> a.broadcastBreakEvent(InteractionHand.MAIN_HAND ) );
                }
            }
            event.setAmount( damage );
            if( event.getAmount() <= 0 )
                event.setCanceled( true );
        }
    }
}