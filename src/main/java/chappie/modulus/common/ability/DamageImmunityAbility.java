package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DamageImmunityAbility extends Ability {
    public final List<String> damageSources = new ArrayList<>();
    public final List<TagKey<DamageType>> damageSourceTags = new ArrayList<>();

    public DamageImmunityAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    public static AbilityBuilder of(String id, Collection<TagKey<DamageType>> tagSources, String... damageSources) {
        return AbilityBuilder.of(id, AbilityType.DAMAGE_IMMUNITY).hide().additionalData(a -> {
            if (a instanceof DamageImmunityAbility ability) {
                ability.damageSources.addAll(List.of(damageSources));
                ability.damageSourceTags.addAll(tagSources);
            }
        });
    }
}