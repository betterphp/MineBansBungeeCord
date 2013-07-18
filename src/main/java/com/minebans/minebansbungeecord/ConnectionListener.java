package com.minebans.minebansbungeecord;

import java.net.InetAddress;
import java.security.MessageDigest;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ConnectionListener implements Listener {
	
	private MineBansBungeeCord plugin;
	
	public ConnectionListener(MineBansBungeeCord plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onProxyPing(ProxyPingEvent event){
		try{
			InetAddress address = event.getConnection().getAddress().getAddress();
			ServerPing ping = event.getResponse();
			
			if ((!address.isAnyLocalAddress() && address.getHostAddress().equals(InetAddress.getByName("minebans.com").getHostAddress())) || MineBansBungeeCord.DEBUG_MODE){
				String motd = "";
				
				if (plugin.requestProxy.currentRequest != null){
					motd = plugin.requestProxy.currentRequest.getMOTD();
					plugin.requestProxy.currentRequest = null;
				}else{
					StringBuilder message = new StringBuilder(32);
					
					for (byte b : MessageDigest.getInstance("MD5").digest(("NONE" + plugin.requestProxy.apiKey).getBytes("UTF-8"))){
						String hex = Integer.toHexString(0x000000FF & b);
						
						if (hex.length() % 2 != 0){
							message.append("0");
						}
						
						message.append(hex);
					}
					
					motd = message.toString();
				}
				
				event.setResponse(new ServerPing(ping.getProtocolVersion(), ping.getGameVersion(), motd, ping.getCurrentPlayers(), ping.getMaxPlayers()));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
