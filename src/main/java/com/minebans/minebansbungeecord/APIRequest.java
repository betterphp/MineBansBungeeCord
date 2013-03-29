package com.minebans.minebansbungeecord;

public class APIRequest {
	
	private String url;
	private String motd;
	private String data;
	
	public APIRequest(String url, String motd, String data){
		this.url = url;
		this.motd = motd;
		this.data = data;
	}
	
	public String getURL(){
		return this.url;
	}
	
	public String getMOTD(){
		return this.motd;
	}
	
	public String getData(){
		return this.data;
	}
	
}
