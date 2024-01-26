package harmonised.pmmo.storage;

import com.mojang.serialization.Codec;
import harmonised.pmmo.config.codecs.CodecTypes;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class DataAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Reference.MOD_ID);

    /**Vein data stores the players current charge.  This is retained so that players leaving and rejoining
     * the world do not have to wait for charge to replenish which would be irritating with slow charge rates.*/
    public static final Supplier<AttachmentType<Double>> VEIN_CHARGE = ATTACHMENT_TYPES.register(
            "vein_data", () -> AttachmentType.builder(() -> 0d)
                    .serialize(Codec.DOUBLE).build());

    /**Placed Data stores who placed the block at a specific position within the chunk.  This is used to
     * track player breaks to prevent players from cycling through place and break actions to farm xp from
     * high-xp blocks*/
    public static final Supplier<AttachmentType<Map<BlockPos, UUID>>> PLACED_MAP = ATTACHMENT_TYPES.register(
            "placed_data", () -> AttachmentType.<Map<BlockPos, UUID>>builder(() -> new HashMap<>())
                    .serialize(Codec.unboundedMap(CodecTypes.BLOCKPOS_CODEC, CodecTypes.UUID_CODEC)
                            .xmap(HashMap::new, HashMap::new)).build());

    /**Break data stores in chunks during runtime to track blocks with cascading break behavior.  No
     * serialization is necessary since the effects are largely instantaneous and inconsequential to store*/
    public static final Supplier<AttachmentType<Map<BlockPos, UUID>>> BREAK_MAP = ATTACHMENT_TYPES.register(
            "break_data", () -> AttachmentType.<Map<BlockPos, UUID>>builder(() -> new HashMap<>()).build());
}
