package com.minebans.minebansbungeecord;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class MineBansBungeeCord extends Plugin {
	
	public static final boolean DEBUG_MODE = true;
	
	protected ProxyServer proxy;
	protected Logger log;
	
	protected APIRequestHandler requestHandler;
	
	public void onEnable(){
		this.proxy = ProxyServer.getInstance();
		this.log = this.proxy.getLogger();
		
		if (DEBUG_MODE){
			this.log.log(Level.WARNING, "========================= WARNING ==========================");
			this.log.log(Level.WARNING, " Debug mode active, do not use this on a production server!");
			this.log.log(Level.WARNING, "============================================================");
		}
		
		if (!this.proxy.getConfigurationAdapter().getBoolean("online_mode", false)){
			this.log.log(Level.WARNING, "======================== WARNING ========================");
			this.log.log(Level.WARNING, " Your server must have online_mode=true to use MineBans!");
			this.log.log(Level.WARNING, "=========================================================");
			return;
		}
		
		this.proxy.registerChannel("MineBansBungee");
		this.proxy.getPluginManager().registerListener(new ConnectionListener(this));
		
		this.requestHandler = new APIRequestHandler(this);
		this.requestHandler.start();
	}
	
	public void onDisable(){
		this.requestHandler.interrupt();
	}
	
}
