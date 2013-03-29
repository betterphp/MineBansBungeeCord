package com.minebans.minebansbungeecord;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;

public class APIRequestProxy extends Thread {
	
	private MineBansBungeeCord plugin;
	private String authStr;
	
	private ServerSocket socket;
	private volatile APIRequest currentRequest;
	
	public APIRequestProxy(MineBansBungeeCord plugin, String address, int port) throws IOException {
		super("MineBans API Server Thread");
		
		this.plugin = plugin;
		this.authStr = plugin.config.getProperty("auth-str", "CHANGE_ME");
		
		this.socket = new ServerSocket();
		this.socket.bind(new InetSocketAddress(address, port));
	}
	
	public void stopThread(){
		try{
			this.socket.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		while (true){
			Socket socket = null;
			
			try{
				socket = this.socket.accept();
				
				socket.setSoTimeout(4000);
				
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());
				
				String authStr = input.readLine();
				String url = input.readLine();
				String data = input.readLine();
				String motd = input.readLine();
				
				if (authStr != null && url != null && data != null && motd != null && authStr.equals(this.authStr)){
					this.currentRequest = new APIRequest(url, motd, data);
					
					URLConnection conn = (new URL(url)).openConnection();
					
					conn.setUseCaches(false);
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
					
					if (!data.isEmpty()){
						conn.setDoOutput(true);
						
						OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
						
						out.write("request_data=" + URLEncoder.encode(data, "UTF-8"));
						
						out.flush();
						out.close();
					}
					
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					
					String line;
					StringBuilder buffer = new StringBuilder();
					
					while ((line = in.readLine()) != null){
						buffer.append(line);
					}
					
					String response = buffer.toString();
					
					in.close();
					
					if (MineBansBungeeCord.DEBUG_MODE){
						plugin.log.info("======================== REQUEST DUMP =========================");
						plugin.log.info(" URL: " + url);
						plugin.log.info(" Request: " + data);
						plugin.log.info(" Response: " + response);
						plugin.log.info("===============================================================");
					}
					
					if (response == null || response.startsWith("E")){
						throw new APIException(response);
					}
					
					output.writeBytes(response);
				}
			}catch (SocketException e){
				return;
			}catch (SocketTimeoutException e){
				plugin.log.log(Level.SEVERE, "The API failed to respond in time.");
			}catch (IOException e){
				plugin.log.log(Level.SEVERE, "Failed to contact the API (you should report this).");
				e.printStackTrace();
			}catch (APIException e){
				plugin.log.log(Level.SEVERE, "API Request Failed: " + e.getResponse());
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				if (socket != null){
					try{
						socket.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public APIRequest getCurrentRequest(){
		return this.currentRequest;
	}
	
}
