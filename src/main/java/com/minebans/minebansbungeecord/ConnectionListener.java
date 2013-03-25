package com.minebans.minebansbungeecord;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;

import com.google.common.eventbus.Subscribe;

public class ConnectionListener implements Listener {
	
	private MineBansBungeeCord plugin;
	
	public ConnectionListener(MineBansBungeeCord plugin){
		this.plugin = plugin;
	}
	
	@Subscribe
	public void onPluginMessage(PluginMessageEvent event){
		Connection sender = event.getSender();
		
		if (sender instanceof Server && event.getTag().equals("MineBansBungee")){
			Server server = (Server) sender;
			
			DataInputStream input = new DataInputStream(new ByteArrayInputStream(event.getData()));
			
			try{
				String command = input.readUTF();
				
				if (command.equals("PerformRequest")){
					String requestURL = input.readUTF();
					String requestData = input.readUTF();
					
					plugin.requestHandler.addRequest(new APIRequest(plugin, server, requestURL, requestData));
					
			//		server.sendData("MineBansBungee", new byte[]{0, 1, 2});
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Subscribe
	public void onLogin(LoginEvent event){
		
	}
	
	@Subscribe
	public void onProxyPing(ProxyPingEvent event){
		ServerPing ping = event.getResponse();
		
		try{
			InetAddress address = event.getConnection().getAddress().getAddress();
			
			if ((!address.isAnyLocalAddress() && address.getHostAddress().equals(InetAddress.getByName("minebans.com").getHostAddress())) || MineBansBungeeCord.DEBUG_MODE){
				event.setResponse(new ServerPing(ping.getProtocolVersion(), ping.getGameVersion(), "TEST", ping.getCurrentPlayers(), ping.getMaxPlayers()));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
