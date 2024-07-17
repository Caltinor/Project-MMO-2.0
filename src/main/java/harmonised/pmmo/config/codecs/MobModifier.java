package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MobModifier(
	String attribute,
	double amount,
	String operation
){

	public static final Codec<MobModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.STRING.fieldOf( "attribute" ).forGetter( MobModifier::attribute),
			Codec.DOUBLE.fieldOf( "amount" ).forGetter( MobModifier::amount ),
			Codec.STRING.fieldOf( "operation" ).forGetter( MobModifier::operation )
		).apply( instance, MobModifier::new )
	);
}
