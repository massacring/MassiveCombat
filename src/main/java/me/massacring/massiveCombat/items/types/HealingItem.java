package me.massacring.massiveCombat.items.types;

import java.util.HashMap;
import java.util.List;

public record HealingItem(
        String tag,
        float heal_amount,
        boolean instant,
        int use_time,
        HashMap<String,HashMap<String, Integer>> used_effects,
        List<String> remove_effects,
        HashMap<String, Integer> during_effects
) {
}
