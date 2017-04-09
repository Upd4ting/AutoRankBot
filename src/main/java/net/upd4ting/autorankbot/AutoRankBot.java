package net.upd4ting.autorankbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.resource.Buyer;
import be.maximvdw.spigotsite.api.resource.PremiumResource;
import be.maximvdw.spigotsite.api.resource.Resource;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class AutoRankBot {
	
	private static JDA jda;
	private static List<User> users = new ArrayList<>();
	private static Set<Buyer> buyers = new HashSet<>();
	private static Set<String> usedBuyers = new HashSet<>();
	private static Set<String> discordUser = new HashSet<>();
	private static boolean running = true;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		Console.info("Initializing AutoRankBot v0.1 ...");
		
		new Configuration(2); // Version 2
		
		String key = Configuration.getString("key");
		
		try {
			jda = new JDABuilder(AccountType.BOT).addListener(new BotListener()).setToken(key).buildBlocking();
			jda.getPresence().setGame(Game.of("I'm coding my AI 2.0"));
			jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
		
		Console.info("Initializing SpigotAPI ...");
		
        new SpigotSiteCore();
        
        Console.info("Loading data...");
        load();
        Console.info("Loading finished!");
        
        Thread th = new Thread(new Runnable() {
        	
        	@Override
        	public void run() {
        		while (running) {
					try {
						for (User u : new ArrayList<>(users)) {
							List<Resource> resources = SpigotSite.getAPI().getResourceManager()
							        .getResourcesByUser(u);
							
		        	        for (Resource res : resources) {
		        	            if (res instanceof PremiumResource) {
		        	                try {
		        	                    List<Buyer> resourceBuyers = SpigotSite
		        	                            .getAPI()
		        	                            .getResourceManager()
		        	                            .getPremiumResourceBuyers(
		        	                                    (PremiumResource) res, u);
		        	                    buyers.addAll(resourceBuyers);
		        	                } catch (ConnectionFailedException e) {
		        	                    e.printStackTrace();
		        	                    return;
		        	                }
		        	            }
		        	        }
						}
					} catch (ConnectionFailedException e1) {
						e1.printStackTrace();
					}
        		}
        		
        		try {
        			Thread.sleep(30000); // 30 seconds
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        th.start();
        Console.info("Press ENTER to quit.");
        System.console().readLine();
        th.stop();
        running = false;
        
        
        // Saving...
        Console.info("Saving data ...");
        save();
        Console.info("Saving finished!");
        
        System.exit(0);
	}
	
	private static void save() {
		File f = new File("data.json");
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		JsonObject root = new JsonObject();
		
		JsonArray array = new JsonArray();
		
		for (String s : usedBuyers)
			array.add(s);
		
		root.add("data", array);
		
		JsonArray array2 = new JsonArray();
		
		for (String s : discordUser)
			array2.add(s);
		
		root.add("data2", array);
		
		try {
			Files.write(f.toPath(), Collections.singleton(root.toString()), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private static void load() {
		File f = new File("data.json");
		
		if (!f.exists()) return;
		
		JsonParser p = new JsonParser();
		JsonObject root;
		
		try {
			root = p.parse(new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"))).getAsJsonObject();
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		JsonArray array = root.get("data").getAsJsonArray();
		
		for (JsonElement s : array)
			usedBuyers.add(s.getAsString());
		
		JsonArray array2 = root.get("data2").getAsJsonArray();
		
		for (JsonElement s : array2)
			discordUser.add(s.getAsString());
	}
	
	public static void addAccount(String username, String password, String totpSecret) throws InvalidCredentialsException, TwoFactorAuthenticationException, ConnectionFailedException {
        Console.info("Logging in " + username + " ...");
    	User u = SpigotSite.getAPI().getUserManager()
                .authenticate(username, password, totpSecret);
    	
    	if (u != null)
    		users.add(u);
	}
	
	public static JDA getJDA() { return jda; }
	public static List<User> getUsers() { return users; }
	public static Set<Buyer> getBuyers() { return buyers; }
	public static Set<String> getUsedBuyers() { return usedBuyers; }
	public static Set<String> getDiscordUser() { return discordUser; }
}
