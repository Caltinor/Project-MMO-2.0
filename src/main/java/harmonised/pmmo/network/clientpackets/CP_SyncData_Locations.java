package harmonised.pmmo.network.clientpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.CodecMapLocation;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_Locations {
	private final Map<ResourceLocation, CodecMapLocation.LocationMapContainer> data;
	
	private static final Codec<Map<ResourceLocation, CodecMapLocation.LocationMapContainer>> MAPPER = 
			Codec.unboundedMap(ResourceLocation.CODEC, CodecMapLocation.LocationMapContainer.CODEC);
	
	public CP_SyncData_Locations(Map<ResourceLocation, CodecMapLocation.LocationMapContainer> data) {this.data = data;}
	public static CP_SyncData_Locations decode(FriendlyByteBuf buf) {
		return new CP_SyncData_Locations(MAPPER.parse(NbtOps.INSTANCE, buf.readNbt(NbtAccounter.UNLIMITED)).result().orElse(new HashMap<>()));
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeNbt((CompoundTag)(MAPPER.encodeStart(NbtOps.INSTANCE, data).result().orElse(new CompoundTag())));
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			finalizeLocationMaps(data);
		});
		ctx.get().setPacketHandled(true);
	}
	
	private static void finalizeLocationMaps(Map<ResourceLocation, CodecMapLocation.LocationMapContainer> data) {
		data.forEach((rl, lmc) -> {
			List<ResourceLocation> tagValues = List.of(rl);
			if (lmc.tagValues().size() > 0) tagValues = lmc.tagValues();
			for (ResourceLocation tag : tagValues) {
				for (Map.Entry<ModifierDataType, Map<String, Double>> modifiers : lmc.bonusMap().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "BONUSES: "+tag.toString()+modifiers.getKey().toString()+MsLoggy.mapToString(modifiers.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getXpUtils().setObjectXpModifierMap(modifiers.getKey(), tag, modifiers.getValue());
				}
				for (Map.Entry<ResourceLocation, Map<String, Double>> mobMods : lmc.mobModifiers().entrySet()) {
					MsLoggy.INFO.log(LOG_CODE.DATA, "MOB MODIFIERS: "+tag.toString()+mobMods.getKey().toString()+MsLoggy.mapToString(mobMods.getValue())+" loaded from config");
					Core.get(LogicalSide.CLIENT).getDataConfig().setMobModifierData(tag, mobMods.getKey(), mobMods.getValue());
				}
				MsLoggy.INFO.log(LOG_CODE.DATA, "POSITIVE EFFECTS: "+MsLoggy.mapToString(lmc.positive()));
				Core.get(LogicalSide.CLIENT).getDataConfig().setLocationEffectData(true, tag, lmc.positive());
				MsLoggy.INFO.log(LOG_CODE.DATA, "NEGATIVE EFFECTS: "+MsLoggy.mapToString(lmc.negative()));
				Core.get(LogicalSide.CLIENT).getDataConfig().setLocationEffectData(false, tag, lmc.negative());
				MsLoggy.INFO.log(LOG_CODE.DATA, "VEIN BLACKLIST: "+MsLoggy.listToString(lmc.veinBlacklist()));
				Core.get(LogicalSide.CLIENT).getDataConfig().setArrayData(tag, lmc.veinBlacklist());
				MsLoggy.INFO.log(LOG_CODE.DATA, "TRAVEl REQ: "+MsLoggy.mapToString(lmc.travelReq()));
				Core.get(LogicalSide.CLIENT).getSkillGates().setObjectSkillMap(ReqType.TRAVEL, tag, lmc.travelReq());
			}
		});
	}
}
