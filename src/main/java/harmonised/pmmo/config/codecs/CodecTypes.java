package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;

public class CodecTypes {
	public static final Codec<Map<String, Double>> DOUBLE_CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);
	public static final Codec<Map<String, Long>> LONG_CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG);
	public static final Codec<Map<String, Integer>> INTEGER_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);	
	
	public static record ModifierData(Map<ModifierDataType, Map<String, Double>> obj) {}
	public static final Codec<ModifierData> MODIFIER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ModifierDataType.CODEC, DOUBLE_CODEC, StringRepresentable.keys(ModifierDataType.values())).forGetter(ModifierData::obj)
			).apply(instance, ModifierData::new));
	
	public static record EventData(Map<EventType, Map<String, Long>> obj) {}
	public static final Codec<EventData> EVENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(EventType.CODEC, LONG_CODEC, StringRepresentable.keys(EventType.values())).forGetter(EventData::obj)
			).apply(instance, EventData::new));
	
	public static record ReqData(Map<ReqType, Map<String, Integer>> obj) {}
	public static final Codec<ReqData> REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ReqType.CODEC, INTEGER_CODEC, StringRepresentable.keys(ReqType.values())).forGetter(ReqData::obj)
			).apply(instance, ReqData::new));
	
	public static record NBTReqData(Map<ReqType, List<LogicEntry>> logic) {
		public NBTReqData() {this(new HashMap<>());}
	}
	public static final Codec<NBTReqData> NBT_REQ_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ReqType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ReqType.values())).forGetter(NBTReqData::logic) 
			).apply(instance, NBTReqData::new));
	
	public static record NBTXpGainData(Map<EventType, List<LogicEntry>> logic) {
		public NBTXpGainData() {this(new HashMap<>());}
	}
	public static final Codec<NBTXpGainData> NBT_XPGAIN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(EventType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(EventType.values())).forGetter(NBTXpGainData::logic) 
			).apply(instance, NBTXpGainData::new));
	
	public static record NBTBonusData(Map<ModifierDataType, List<LogicEntry>> logic) {
		public NBTBonusData() {this(new HashMap<>());}
	}
	public static final Codec<NBTBonusData> NBT_BONUS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.simpleMap(ModifierDataType.CODEC, Codec.list(LogicEntry.CODEC), StringRepresentable.keys(ModifierDataType.values())).forGetter(NBTBonusData::logic) 
			).apply(instance, NBTBonusData::new));
	
	public record SalvageData (
			Map<String, Double> chancePerLevel,
			Map<String, Integer> levelReq,
			Map<String, Long> xpAward,
			int salvageMax,
			double baseChance, 
			double maxChance){
		
		public static SalvageData combine(SalvageData one, SalvageData two, boolean oneOverride, boolean twoOverride) {
			Map<String, Double> chancePerLevel = new HashMap<>();
			Map<String, Integer> levelReq = new HashMap<>();
			Map<String, Long> xpAward = new HashMap<>();
			AtomicInteger salvageMax = new AtomicInteger(0);
			AtomicDouble baseChance = new AtomicDouble(0); 
			AtomicDouble maxChance = new AtomicDouble(0);
			
			BiConsumer<SalvageData, SalvageData> bothOrNeither = (o, t) -> {
				chancePerLevel.putAll(o.chancePerLevel());
				t.chancePerLevel().forEach((skill, mod) -> {
					chancePerLevel.merge(skill, mod, (o1, n1) -> o1 > n1 ? o1 : n1);
				});
				levelReq.putAll(o.levelReq());
				t.levelReq().forEach((skill, level) -> {
					levelReq.merge(skill, level, (o1, n1) -> o1 > n1 ? o1 : n1);
				});
				xpAward.putAll(o.xpAward());
				t.xpAward().forEach((skill, xp) -> {
					xpAward.merge(skill, xp, (o1, n1) -> o1 > n1 ? o1 : n1);
				});
				salvageMax.set(o.salvageMax() > t.salvageMax() ? o.salvageMax() : t.salvageMax());
				baseChance.set(o.baseChance() > t.baseChance() ? o.baseChance() : t.baseChance());
				maxChance.set(o.maxChance() > t.maxChance() ? o.maxChance() : t.maxChance());
			};
			Functions.biPermutation(one, two, oneOverride, twoOverride, (o, t) -> {
				chancePerLevel.putAll(o.chancePerLevel().isEmpty() ? t.chancePerLevel() : o.chancePerLevel());
				levelReq.putAll(o.levelReq().isEmpty() ? t.levelReq() : o.levelReq());
				xpAward.putAll(o.xpAward().isEmpty() ? t.xpAward() : o.xpAward());
				salvageMax.set(t.salvageMax());
				baseChance.set(t.baseChance());
				maxChance.set(t.maxChance());
			}, 
			bothOrNeither, 
			bothOrNeither);
			
			return new SalvageData(chancePerLevel, levelReq, xpAward, salvageMax.get(), baseChance.get(), maxChance.get());
		}
	}
	public static final Codec<SalvageData> SALVAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("chancePerLevel").forGetter(SalvageData::chancePerLevel),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("levelReq").forGetter(SalvageData::levelReq),
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("xpPerItem").forGetter(SalvageData::xpAward),
			Codec.INT.fieldOf("salvageMax").forGetter(SalvageData::salvageMax),			
			Codec.DOUBLE.fieldOf("baseChance").forGetter(SalvageData::baseChance),			
			Codec.DOUBLE.fieldOf("maxChance").forGetter(SalvageData::maxChance)			
			).apply(instance, SalvageData::new));
	
	public static final PrimitiveCodec<UUID> UUID_CODEC = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<UUID> read(DynamicOps<T> ops, T input) {
			return DataResult.success(UUID.fromString(ops.getStringValue(input).getOrThrow(false, null)));
		}
		@Override
		public <T> T write(DynamicOps<T> ops, UUID value) {
			return ops.createString(value.toString());
		}
		@Override
		public String toString() { return "uuid";}
	};
	
	public static final PrimitiveCodec<BlockPos> BLOCKPOS_CODEC = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<BlockPos> read(DynamicOps<T> ops, T input) {
			return DataResult.success(BlockPos.of(ops.getStringValue(input).map(Long::valueOf).getOrThrow(false, null)));
		}
		@Override
		public <T> T write(DynamicOps<T> ops, BlockPos value) {
			return ops.createString(String.valueOf(value.asLong()));
		}
		@Override
		public String toString() { return "blockpos";}
	};
	
	public static final PrimitiveCodec<ChunkPos> CHUNKPOS_CODEC = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<ChunkPos> read(DynamicOps<T> ops, T input) {
			return DataResult.success(new ChunkPos(ops.getNumberValue(input).map(Number::longValue).getOrThrow(false, null)));
		}
		@Override
		public <T> T write(DynamicOps<T> ops, ChunkPos value) {
			return ops.createLong(value.toLong());
		}
		@Override
		public String toString() { return "chunkpos";}
	};
}
