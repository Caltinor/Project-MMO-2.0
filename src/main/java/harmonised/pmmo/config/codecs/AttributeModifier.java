package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AttributeModifier(
	String attribute,
	double amount,
	String operation
){

	public static final Codec<AttributeModifier> CODEC = RecordCodecBuilder.create( instance ->
		instance.group(
			Codec.STRING.fieldOf( "attribute" ).forGetter( AttributeModifier::attribute),
			Codec.DOUBLE.fieldOf( "amount" ).forGetter( AttributeModifier::amount ),
			Codec.STRING.fieldOf( "operation" ).forGetter( AttributeModifier::operation )
		).apply( instance, AttributeModifier::new )
	);
}
