package me.massacring.massiveCombat.items.types;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public record HealingItem(
        String tag,
        double healAmount,
        List<PotionEffect> addEffects,
        List<PotionEffectType> removeEffects
) {
}
