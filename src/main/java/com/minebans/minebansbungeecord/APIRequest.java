package com.minebans.minebansbungeecord;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import net.md_5.bungee.api.connection.Server;

public class APIRequest {
	
	private MineBansBungeeCord plugin;
	
	private Server server;
	private String url;
	private String data;
	
	public APIRequest(MineBansBungeeCord plugin, Server server, String url, String data){
		this.plugin = plugin;
		
		this.server = server;
		this.url = url;
		this.data = data;
	}
	
	public String getURL(){
		return this.url;
	}
	
	public String getData(){
		return this.data;
	}
	
	public void onSuccess(String response){
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(data);
		
		try{
			output.writeUTF("RequestComplete");
			output.writeUTF(response);
			
			server.sendData("MineBansbungee", data.toByteArray());
		}catch (Exception e){
			this.onFailure(e);
		}
	}
	
	public void onFailure(Exception exception){
		if (exception instanceof SocketTimeoutException){
			plugin.log.log(Level.SEVERE, "The API failed to respond in time.");
		}else if (exception instanceof UnsupportedEncodingException || exception instanceof IOException){
			plugin.log.log(Level.SEVERE, "Failed to contact the API (you should report this).");
			exception.printStackTrace();
		}else if (exception instanceof APIException){
			plugin.log.log(Level.SEVERE, "API Request Failed: " + ((APIException) exception).getResponse());
		}else{
			exception.printStackTrace();
		}
	}
	
}
