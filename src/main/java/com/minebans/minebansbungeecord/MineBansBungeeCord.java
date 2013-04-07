package com.minebans.minebansbungeecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class MineBansBungeeCord extends Plugin {
	
	public static final boolean DEBUG_MODE = true;
	
	protected ProxyServer proxy;
	protected Logger log;
	protected File dataDir;
	protected Properties config;
	
	protected APIRequestProxy requestProxy;
	
	@Override
	public void onEnable(){
		this.proxy = ProxyServer.getInstance();
		this.log = this.proxy.getLogger();
		this.dataDir = new File("plugins" + File.separator + this.getDescription().getName());
		this.config = new Properties();
		
		if (!this.dataDir.exists()){
			this.dataDir.mkdirs();
		}
		
		File configFile = new File(this.dataDir, "config.txt");
		
		if (!configFile.exists()){
			this.config.put("auth-str", "CHANGE_THIS");
			this.config.put("listen-address", "127.0.0.1");
			this.config.put("listen-port", "8000");
			
			try{
				this.config.store(new FileOutputStream(configFile), null);
			}catch (Exception e){
				this.log.warning("Failed to save default config");
				e.printStackTrace();
			}
		}else{
			try{
				this.config.load(new FileInputStream(configFile));
			}catch (Exception e){
				this.log.warning("Failed to load config");
				e.printStackTrace();
			}
		}
		
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
		
		try{
			String address = this.config.getProperty("listen-address", "127.0.0.1");
			int port = Integer.parseInt(this.config.getProperty("listen-port", "8000"));
			
			this.requestProxy = new APIRequestProxy(this, address, port);
			this.requestProxy.start();
		}catch (Exception e){
			this.log.severe("Failed to start request proxy on");
			e.printStackTrace();
		}
		
		this.proxy.getPluginManager().registerListener(this, new ConnectionListener(this));
	}
	
	@Override
	public void onDisable(){
		this.requestProxy.stopThread();
	}
	
}
