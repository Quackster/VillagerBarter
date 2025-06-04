package org.oldskooler.villagerbartering;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.oldskooler.villagerbartering.listeners.VillagerTradeListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class VillagerBarter extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 26080;

    private Logger logger;
    private BarterConfig barterConfig;
    private Metrics metrics;

    @Override
    public void onEnable() {
        logger = getLogger();
        logger.info("Starting plugin");

        metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        barterConfig = new BarterConfig(this);

        this.registerListeners();

        // Start scheduled barter check
        Bukkit.getScheduler().runTaskTimer(this, this::startVillagerBarterTask, 20L, 5 * 20L); // Initial delay 1s, repeat every 5s

        logger.info("Finished");
    }

    /**
     * Register the listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(this), this);
    }

    private void startVillagerBarterTask() {
        for (World world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                // Don't get unemployed villagers, nitwits, sleeping or child villagers trade
                if (villager.getProfession() == Villager.Profession.NONE ||
                        villager.getProfession() == Villager.Profession.NITWIT ||
                        !villager.isAdult() ||
                        villager.isSleeping()) continue;

                var settings = barterConfig.getSettings(world, villager.getProfession());
                if (!settings.isEnabled()) continue;

                if (villager.getVillagerLevel() < settings.getMinLevel() || villager.getVillagerLevel() > settings.getMaxLevel())
                    continue;

                List<Item> itemsToConsume = canTradeWithVillager(villager, settings.getSearchRadius());
                if (!itemsToConsume.isEmpty()) {
                    for (var droppedItem : itemsToConsume) {
                        droppedItem.getPersistentDataContainer().set(
                                new NamespacedKey(this, "unpickable"),
                                PersistentDataType.BYTE,
                                (byte) 1
                        );

                        droppedItem.setPickupDelay(Integer.MAX_VALUE);
                    }

                    tradeWithVillager(villager, itemsToConsume);
                }
            }
        }
    }

    public List<Item> canTradeWithVillager(Villager villager, int searchRadius) {
        List<Item> nearbyItems = new ArrayList<>();
        for (Entity entity : villager.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
            if (entity instanceof Item item) nearbyItems.add(item);
        }

        for (MerchantRecipe recipe : villager.getRecipes()) {
            if (recipe.getUses() >= recipe.getMaxUses()) continue;

            List<ItemStack> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty() || ingredients.size() > 2) continue;

            List<Item> availableItems = new ArrayList<>(nearbyItems);
            List<Item> itemsToConsume = new ArrayList<>();

            boolean matches = true;
            for (ItemStack required : ingredients) {
                Item match = findAndExtract(availableItems, required);
                if (match == null) {
                    matches = false;
                    break;
                }
                itemsToConsume.add(match);
            }

            if (matches) {
                return itemsToConsume;
            }
        }

        return List.of(); // empty list = no trade
    }

    private boolean canMatchIngredients(List<Item> items, List<ItemStack> ingredients) {
        List<Item> copy = new ArrayList<>(items);

        for (ItemStack required : ingredients) {
            boolean found = false;
            for (Item item : copy) {
                ItemStack stack = item.getItemStack();
                if (matches(stack, required) && stack.getAmount() >= required.getAmount()) {
                    found = true;
                    copy.remove(item); // Pretend we "used" it, like findAndExtract
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }

    public void tradeWithVillager(Villager villager, List<Item> itemsToConsume) {
        var settings = barterConfig.getSettings(villager.getWorld(), villager.getProfession());

        if (!settings.isEnabled()) return;

        List<MerchantRecipe> updatedRecipes = new ArrayList<>(villager.getRecipes());

        for (int i = 0; i < updatedRecipes.size(); i++) {
            AtomicInteger index = new AtomicInteger(i);
            MerchantRecipe recipe = updatedRecipes.get(i);

            if (recipe.getUses() >= recipe.getMaxUses()) continue;

            List<ItemStack> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty() || ingredients.size() > 2) continue;

            List<Item> availableItems = new ArrayList<>(itemsToConsume);

            List<Item> matches = new ArrayList<>();
            boolean allMatched = true;

            for (ItemStack required : ingredients) {
                Item match = findAndExtract(availableItems, required);
                if (match == null) {
                    allMatched = false;
                    break;
                }
                matches.add(match);
            }

            if (allMatched) {
                AtomicInteger remaining = new AtomicInteger(matches.size());
                for (Item item : matches) {
                    simulatePickup(item, villager, () -> {
                        if (remaining.decrementAndGet() == 0) {
                            villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    ItemStack result = recipe.getResult();
                                    // villager.getWorld().dropItemNaturally(villager.getLocation(), result)
                                    //        .setVelocity(new Vector(0, 0.3, 0));

                                    villager.getWorld().getNearbyEntities(villager.getLocation(), settings.getSearchRadius(), settings.getSearchRadius(), settings.getSearchRadius()).stream()
                                            .filter(e -> e.getType() == EntityType.PLAYER)
                                            .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(villager.getLocation())))
                                            .ifPresent(nearestPlayer -> facePlayer(villager, nearestPlayer));

                                    villager.getWorld().dropItemNaturally(villager.getLocation(), result)
                                            .setVelocity(new Vector(0, 0.3, 0));

                                    recipe.setUses(recipe.getUses() + 1);
                                    updatedRecipes.set(index.get(), recipe);
                                    villager.setRecipes(updatedRecipes);

                                    villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                                }
                            }.runTaskLater(this, 20);
                        }
                    });
                }
                return;
            }
        }
    }

    private void facePlayer(Villager villager, Entity player) {
        if (villager == null || player == null) return;

        Location villagerLoc = villager.getLocation();
        Location playerLoc = player.getLocation();

        // Calculate direction vector
        double dx = playerLoc.getX() - villagerLoc.getX();
        double dz = playerLoc.getZ() - villagerLoc.getZ();
        double dy = playerLoc.getY() - villagerLoc.getY();

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        Location newLoc = villagerLoc.clone();
        newLoc.setYaw(yaw);
        newLoc.setPitch(pitch);

        villager.teleport(newLoc);
    }


    private void simulatePickup(Item item, Villager villager, Runnable onFinish) {
        item.getWorld().playSound(item.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.3f, 2f);

        Vector direction = villager.getEyeLocation().toVector().subtract(item.getLocation().toVector()).normalize();
        item.setVelocity(direction.multiply(0.3));

        new BukkitRunnable() {
            @Override
            public void run() {
                item.remove();
                if (onFinish != null) onFinish.run();
            }
        }.runTaskLater(this, 5);
    }

    private Item findAndExtract(List<Item> items, ItemStack required) {
        for (Item item : items) {
            ItemStack stack = item.getItemStack();
            if (matches(stack, required) && stack.getAmount() >= required.getAmount()) {
                if (stack.getAmount() == required.getAmount()) {
                    items.remove(item);
                    return item;
                } else {
                    stack.setAmount(stack.getAmount() - required.getAmount());
                    ItemStack used = required.clone();
                    Item fake = item.getWorld().dropItem(item.getLocation(), used);
                    fake.setPickupDelay(100000);
                    fake.remove();
                    return fake;
                }
            }
        }
        return null;
    }

    private boolean matches(ItemStack a, ItemStack b) {
        if (a.getType() != b.getType()) return false;
        if (a.hasItemMeta() != b.hasItemMeta()) return false;
        if (a.hasItemMeta()) return a.getItemMeta().equals(b.getItemMeta());
        return true;
    }

    @Override
    public void onDisable() { }
}