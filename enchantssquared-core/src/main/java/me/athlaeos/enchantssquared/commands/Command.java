package me.athlaeos.enchantssquared.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Command {
	boolean execute(CommandSender sender, String[] args);
	String[] getRequiredPermission();
	String getFailureMessage();
	String[] getHelpEntry();
	List<String> getSubcommandArgs(CommandSender sender, String[] args);
}
