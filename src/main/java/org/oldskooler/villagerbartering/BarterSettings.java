package org.oldskooler.villagerbartering;

public class BarterSettings {
    public static final BarterSettings DEFAULT = new BarterSettings(3, 60, 1, 5, false);

    private final int searchRadius;
    private final int delayTicks;
    private final int minLevel;
    private final int maxLevel;
    private final boolean enabled;

    public BarterSettings(int searchRadius, int delayTicks, int minLevel, int maxLevel, boolean enabled) {
        this.searchRadius = searchRadius;
        this.delayTicks = delayTicks;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.enabled = enabled;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean isEnabled() {
        return enabled;
    }
}