package uk.co.notnull.lightblocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LightCommand implements CommandExecutor {
	private final LightBlocks plugin;

	public LightCommand(LightBlocks plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage("You must be a player!");
			return true;
		}

		if(args.length > 1) {
			player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
			return false;
		}

		int lightLevel = 15;

		if(args.length == 1) {
			try {
				lightLevel = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
				return false;
			}
		}

		if(lightLevel < 0 || lightLevel > 15) {
			player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
			return false;
		}

		plugin.getLogger().info("/give " + player.getName()
						+ " minecraft:light{BlockStateTag: {level:\"" + lightLevel + "\"}} ");

		plugin.getServer().dispatchCommand(
				plugin.getServer().getConsoleSender(), "give " + player.getName()
						+ " minecraft:light{BlockStateTag: {level:\"" + lightLevel + "\"}} ");

		return true;
	}
}
