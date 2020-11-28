package com.ruthlessjailer.plugin.villagercollector;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class VillagerCollector extends JavaPlugin implements Listener {
	private static final List<Material>    buckets = Arrays.asList(Material.WATER_BUCKET,
																   Material.LAVA_BUCKET,
																   Material.MILK_BUCKET,
																   Material.BUCKET);
	private static final long           cooldownMs  = 6000;//every minute
	private long lastTrigger = 0;//last time the perm message was sent
	private static       VillagerCollector instance;



	@Override
	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		instance = null;
	}


	@EventHandler
	public void onClick(PlayerInteractAtEntityEvent event){

		if (!(event.getRightClicked() instanceof Villager)) {
			return;
		}

		final Villager villager = (Villager) event.getRightClicked();

		if (!(villager.getProfession() == Villager.Profession.NITWIT ||
			  villager.getProfession() == Villager.Profession.NONE)) {
			return;
		}

		final Player player = event.getPlayer();

		if (!buckets.contains(player.getInventory().getItemInMainHand().getType())) {
			return;
		}

		if (!player.hasPermission("villagercollector.use")) {
			if(System.currentTimeMillis() - this.lastTrigger > cooldownMs) {
				player.sendMessage(colorize("&cYou are not allowed to collect villagers!"));
			}
			lastTrigger = System.currentTimeMillis();
			return;
		}

		final ItemStack egg = new ItemStack(Material.VILLAGER_SPAWN_EGG);

		if (villager.getCustomName() != null) {
			final ItemMeta meta = egg.getItemMeta();
			meta.setDisplayName(colorize(villager.getCustomName()));
			egg.setItemMeta(meta);
		}

		final Collection<ItemStack> notAdded = player.getInventory().addItem(egg).values();

		if (!notAdded.isEmpty()) {
			for (final ItemStack stack : notAdded) {
				villager.getWorld().dropItem(villager.getLocation(), stack);
			}
		}

		villager.remove();

		event.setCancelled(true);
	}

	public static String colorize(String... strings){
		final StringBuilder result = new StringBuilder();
		for (String string : strings){
			result.append(ChatColor.translateAlternateColorCodes('&', string)).append('\n');
		}
		return result.toString();
	}

	public static VillagerCollector getInstance(){ return instance; }

}
