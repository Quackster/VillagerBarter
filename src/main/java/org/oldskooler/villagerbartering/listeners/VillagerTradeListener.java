package org.oldskooler.villagerbartering.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.persistence.PersistentDataType;
import org.oldskooler.villagerbartering.VillagerBarter;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VillagerTradeListener implements Listener {
    private final Random random;
    private final VillagerBarter plugin;

    public VillagerTradeListener(VillagerBarter plugin) {
        this.random = new Random();
        this.plugin = plugin;
    }

    /*
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();

        // Prevent pickup by any entity
        droppedItem.setPickupDelay(Integer.MAX_VALUE);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity nearby : droppedItem.getNearbyEntities(VillagerBarter.SEARCH_RADIUS, VillagerBarter.SEARCH_RADIUS, VillagerBarter.SEARCH_RADIUS)) {
                    if (nearby instanceof Villager villager) {
                        if (!VillagerBarter.ALLOWED_PROFESSIONS.contains(villager.getProfession())) continue;
                        int level = villager.getVillagerLevel();
                        if (level < MIN_LEVEL || level > MAX_LEVEL) continue;

                        if (canTradeWithVillager(villager)) {
                            droppedItem.getPersistentDataContainer().set(
                                    new NamespacedKey(plugin, "unpickable"),
                                    PersistentDataType.BYTE,
                                    (byte) 1
                            );

                            tradeWithVillager(villager);
                        }
                    }
                }
            }
        }.runTaskLater(this.plugin, DELAY_TICKS);
    }*/



    @EventHandler
    public void onVillagerPickupFood(EntityPickupItemEvent event) {
        if (isItemUnpickable(event.getItem())) {
            Bukkit.getServer().getPlayer("Cleopatra_Vll").sendMessage(event.getEntity().getType().name() + " tried picking up " + event.getItem().getItemStack().getType().name());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerAttemptPickup(PlayerAttemptPickupItemEvent event) {
        if (isItemUnpickable(event.getItem())) {
            Bukkit.getServer().getPlayer("Cleopatra_Vll").sendMessage("Player tried picking up " + event.getItem().getItemStack().getType().name());

            event.setCancelled(true);
        }
    }

    private boolean isItemUnpickable(Item item) {
        Byte tag = item.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "unpickable"),
                PersistentDataType.BYTE
        );
        return tag != null && tag == 1;
    }
}
