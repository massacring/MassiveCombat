package me.massacring.massiveCombat.items.types;

import java.util.HashMap;
import java.util.List;

public record HealingItem(
        String tag,
        float healAmount,
        boolean instant,
        int useTime,
        HashMap<String,HashMap<String, Integer>> usedEffects,
        List<String> removeEffects,
        HashMap<String, Integer> duringEffects
) {
}
