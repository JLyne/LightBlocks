package uk.co.notnull.lightblocks;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class LightBlocks extends JavaPlugin implements Listener {
	private GriefPreventionHandler griefPreventionHandler;
	private WorldGuardHandler worldGuardHandler;
	public final NamespacedKey key = new NamespacedKey(this, "light_block");

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> new LightCommand(event.registrar()));

		initRecipe();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.getGameMode().equals(GameMode.CREATIVE) && holdingLightBlockTool(player)) {
					highlightLightBlocks(player);
				}
			}
		}, 0L, 20L);
	}

	public void onDisable() {
		if(Bukkit.getRecipe(key) != null) {
			Bukkit.removeRecipe(key);
		}
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		switch(event.getPlugin().getName()) {
			case "GriefPrevention":
				getLogger().info("Initialising GriefPrevention handler");
				griefPreventionHandler = new GriefPreventionHandler();
				break;

			case "WorldGuard":
				getLogger().info("Initialising WorldGuard handler");
				worldGuardHandler = new WorldGuardHandler();
				break;
		}
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		switch(event.getPlugin().getName()) {
			case "GriefPrevention":
				if(griefPreventionHandler != null) {
					getLogger().info("Disabling GriefPrevention handler");
					griefPreventionHandler = null;
				}
				break;

			case "WorldGuard":
				if(worldGuardHandler != null) {
					getLogger().info("Disabling WorldGuard handler");
					worldGuardHandler = null;
				}
				break;
		}
	}

	@EventHandler
	public void onServerResourcesReloaded(ServerResourcesReloadedEvent event) {
		initRecipe();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLeftClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}

		if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.LIGHT) {
			return;
		}

		if (!canBreak(block, event.getPlayer())) {
			return;
		}

		ItemStack item = event.getItem();
		Location location = block.getLocation();

		block.breakNaturally(item != null ? item : ItemStack.of(Material.LIGHT), true);
		location.getWorld().dropItemNaturally(location, ItemStack.of(Material.LIGHT));

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.LIGHT) {
			return;
		}

		if(worldGuardHandler != null && !worldGuardHandler.checkPermission(block.getLocation(), event.getPlayer())) {
			return;
		}

		if(griefPreventionHandler != null && !griefPreventionHandler.checkPermission(block.getLocation(), event.getPlayer(), event)) {
			return;
		}

		Light blockData = (Light) event.getClickedBlock().getBlockData();
		int level = blockData.getLevel();
		blockData.setLevel(level == blockData.getMaximumLevel() ? blockData.getMinimumLevel() : level + 1);
		event.getClickedBlock().setBlockData(blockData);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onBlockPlaced(BlockPlaceEvent event) {
		Block placed = event.getBlockPlaced();

		if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			return;
		}

		if (placed.getType() == Material.LIGHT) {
			event.getPlayer().spawnParticle(Particle.BLOCK_MARKER, placed.getLocation().add(0.5, 0.5, 0.5), 1, placed.getBlockData());
		} else if (event.getBlockReplacedState().getType() == Material.LIGHT) {
			Location location = event.getBlock().getLocation();
			location.getWorld().dropItemNaturally(location, ItemStack.of(Material.LIGHT));
		}
	}

	private void initRecipe() {
		if(Bukkit.getRecipe(key) != null) {
			Bukkit.removeRecipe(key);
		}

		ShapedRecipe recipe = new ShapedRecipe(key, ItemStack.of(Material.LIGHT, 8));
		recipe.setCategory(CraftingBookCategory.BUILDING);

		recipe.shape("###", "#I#", "###");
		recipe.setIngredient('#', ItemStack.of(Material.GLOWSTONE));

		ItemStack potion = ItemStack.of(Material.SPLASH_POTION);
		potion.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
				.potion(PotionType.INVISIBILITY).build());

		ItemStack potion2 = ItemStack.of(Material.SPLASH_POTION);
		potion2.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
				.potion(PotionType.LONG_INVISIBILITY).build());

		recipe.setIngredient('I', new RecipeChoice.ExactChoice(potion, potion2));

		Bukkit.addRecipe(recipe);
	}

	private List<Location> getLightBlocksAroundPlayer(Player player) {
		World world = player.getWorld();
		List<Location> locations = new ArrayList<>();

		Location playerLocation = player.getLocation();
		int baseX = playerLocation.getBlockX();
		int baseY = playerLocation.getBlockY();
		int baseZ = playerLocation.getBlockZ();

		for (int x = baseX - 16; x <= baseX + 16; x++) {
			for (int z = baseZ - 16; z <= baseZ + 16; z++) {
				int minY = Math.max(baseY - 16, world.getMinHeight());
				int maxY = Math.min(baseY + 16, world.getMaxHeight());

				for (int y = minY; y <= maxY; y++) {
					Block block = world.getBlockAt(x, y, z);

					if (block.getType() == Material.LIGHT) {
						locations.add(block.getLocation().add(0.5, 0.5, 0.5));
					}
				}
			}
		}

		return locations;
	}

	private void highlightLightBlocks(Player player) {
		for (Location location : getLightBlocksAroundPlayer(player)) {
			player.spawnParticle(Particle.BLOCK_MARKER, location, 1, location.getBlock().getBlockData());
		}
	}

	private boolean canBreak(Block block, Player player) {
		BlockBreakEvent event = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(event);

		return !event.isCancelled();
	}

	private boolean holdingLightBlockTool(Player player) {
		return player.getEquipment().getItemInMainHand().getType() == Material.LIGHT
				|| player.getEquipment().getItemInOffHand().getType() == Material.LIGHT;
	}
}
