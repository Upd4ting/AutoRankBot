package net.upd4ting.autorankbot;

import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

public class BotListener extends ListenerAdapter {
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
		String message = e.getMessage().getRawContent();
		MessageChannel channel = e.getChannel();
		
		String name = e.getAuthor().getName();
				
		if (!name.equals("Upd4ting") && !name.equals("MartinEkstrom"))
			return;
				
		if (message.startsWith("!addaccount ")) {
			String params = message.replaceAll("!addaccount ", "");
			String[] args = params.split(" ");
			
			if (args.length != 3 && args.length != 2) {
				channel.sendMessage(e.getAuthor().getAsMention() + " Bad command...").queue();
				return;
			}
			
			String username = args[0];
			String password = args[1];
			
			boolean exist = false;
			
			for (User u : AutoRankBot.getUsers()) {
				if (u.getUsername().equals(username))
					exist = true;
			}
			
			if (exist) {
				channel.sendMessage(e.getAuthor().getAsMention() + " Your account has been already added!").queue();
				return;
			}
			
			try {
				AutoRankBot.addAccount(username, password, args.length == 2 ? "" : args[2]);
				channel.sendMessage(e.getAuthor().getAsMention() + " Your account has succesfully been added!").queue();
			} catch (InvalidCredentialsException | TwoFactorAuthenticationException | ConnectionFailedException e1) {
				channel.sendMessage(e.getAuthor().getAsMention() + " Failed to add your account...").queue();
			}
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String message = e.getMessage().getRawContent();
		MessageChannel channel = e.getChannel();
		
		if (!channel.getId().equals("300618169767428099") && !channel.getId().equals("297722160309338112"))
			return;
		
        if (message.startsWith("!register ")) {
        	String buyerName = message.replaceAll("!register ", "");
        	String discordName = e.getAuthor().getName();
        	
        	boolean registred = false;
        	boolean used = false;
        	boolean exist = false;
        	
        	for (String u : AutoRankBot.getDiscordUser()) {
        		if (u.equalsIgnoreCase(discordName))
        			registred = true;
        	}
        	
        	for (String u : AutoRankBot.getUsedBuyers()) {
        		if (u.equalsIgnoreCase(buyerName)) {
        			used = true;
        		}
        	}
        	
        	for (User u : AutoRankBot.getBuyers()) {
        		if (u.getUsername().equalsIgnoreCase(buyerName)) {
        			exist = true;
        		}
        	}
        	
        	if (registred) {
        		channel.sendMessage(e.getAuthor().getAsMention() + " You are already registred!").queue();
        	}
        	else if (!exist) {
        		channel.sendMessage(e.getAuthor().getAsMention() + " This spigot username doesn't exist or haven't buy our premium ressource!").queue();
        	} else if (used) {
        		channel.sendMessage(e.getAuthor().getAsMention() + " This spigot username is already used!").queue();
        	} else if (exist && !used) {
        		GuildController controller = e.getGuild().getController();
        		controller.addRolesToMember(e.getMember(), e.getGuild().getRolesByName("Clients", true)).queue();;
        		
        		AutoRankBot.getUsedBuyers().add(buyerName);
        		AutoRankBot.getDiscordUser().add(discordName);
        		
        		channel.sendMessage(e.getAuthor().getAsMention() + " You got ranked to 'Client'. Thanks for supporting us!").queue();
        		
        		AutoRankBot.save();
        	}
        } else if (!e.getAuthor().getName().equals(AutoRankBot.getJDA().getSelfUser().getName()) && !channel.getId().equals("297722160309338112")) {
        	e.getMessage().delete().queue();
        	e.getAuthor().getPrivateChannel().sendMessage(e.getAuthor().getAsMention() + " You should not talk in this channel!").queue();
        }
	}
}
