package com.toiec.demo.policy;



import java.util.Map;

public class XpPolicy {
    private static final Map<Integer, Integer> QUALITY_XP = Map.of(
            4, 15,   // easy
            3, 10,   // good
            2, 5,    // hard
            1, 2,    // forget (still some XP)
            0, 0
    );

    public static int getXpForQuality(int quality) {
        return QUALITY_XP.getOrDefault(quality, 0);
    }
}