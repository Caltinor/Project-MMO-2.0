package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import static org.w3c.dom.events.MutationEvent.ADDITION;

public record MobModifier(
    ResourceLocation attribute,
    double amount,
    AttributeModifier.Operation operation
){
    public static final Codec<MobModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf( "attribute" ).forGetter( MobModifier::attribute),
                    Codec.DOUBLE.fieldOf( "amount" ).forGetter( MobModifier::amount ),
                    AttributeModifier.Operation.CODEC.fieldOf( "operation" ).forGetter( MobModifier::operation )
            ).apply( instance, MobModifier::new )
    );

    public String display() {
        return switch (operation) {
            case ADD_VALUE -> "+";
            case ADD_MULTIPLIED_BASE -> "*";
            case ADD_MULTIPLIED_TOTAL -> "**";
        };
    }
}
