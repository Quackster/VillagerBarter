package org.oldskooler.villagerbartering;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Villager;

import java.util.*;

public class BarterConfig {
    private final VillagerBarter plugin;
    private final Map<String, Map<Villager.Profession, BarterSettings>> settings = new HashMap<>();

    public BarterConfig(VillagerBarter plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        settings.clear();
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        for (String worldName : config.getConfigurationSection("worlds").getKeys(false)) {
            Map<Villager.Profession, BarterSettings> professionMap = new HashMap<>();
            for (String profName : config.getConfigurationSection("worlds." + worldName).getKeys(false)) {
                try {
                    Villager.Profession profession = getProfession(profName.toUpperCase());
                    int radius = config.getInt("worlds." + worldName + "." + profName + ".search_radius", 3);
                    int delay = config.getInt("worlds." + worldName + "." + profName + ".delay_ticks", 60);
                    int min = config.getInt("worlds." + worldName + "." + profName + ".min_level", 1);
                    int max = config.getInt("worlds." + worldName + "." + profName + ".max_level", 5);
                    boolean enabled = config.getBoolean("worlds." + worldName + "." + profName + ".enabled", true);

                    professionMap.put(profession, new BarterSettings(radius, delay, min, max, enabled));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Unknown profession in config: " + profName);
                }
            }
            settings.put(worldName, professionMap);
        }
    }

    public BarterSettings getSettings(World world, Villager.Profession profession) {
        return settings
                .getOrDefault(world.getName(), Map.of())
                .getOrDefault(profession, BarterSettings.DEFAULT);
    }

    public static final Map<String, Villager.Profession> PROFESSION_MAP = Map.ofEntries(
            Map.entry("armorer", Villager.Profession.ARMORER),
            Map.entry("butcher", Villager.Profession.BUTCHER),
            Map.entry("cartographer", Villager.Profession.CARTOGRAPHER),
            Map.entry("cleric", Villager.Profession.CLERIC),
            Map.entry("farmer", Villager.Profession.FARMER),
            Map.entry("fisherman", Villager.Profession.FISHERMAN),
            Map.entry("fletcher", Villager.Profession.FLETCHER),
            Map.entry("leatherworker", Villager.Profession.LEATHERWORKER),
            Map.entry("librarian", Villager.Profession.LIBRARIAN),
            Map.entry("mason", Villager.Profession.MASON),
            Map.entry("shepherd", Villager.Profession.SHEPHERD),
            Map.entry("toolsmith", Villager.Profession.TOOLSMITH),
            Map.entry("weaponsmith", Villager.Profession.WEAPONSMITH)
    );

    public static Villager.Profession getProfession(String key) {
        if (key == null) return null;
        return PROFESSION_MAP.get(key.toLowerCase());
    }
}