package com.minebans.minebansbungeecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;

public class APIRequestHandler extends Thread {
	
	private MineBansBungeeCord plugin;
	
	private ArrayBlockingQueue<APIRequest> requestStack;
	private volatile APIRequest currentRequest;
	
	public APIRequestHandler(MineBansBungeeCord plugin){
		super("MineBans API Thread");
		
		this.plugin = plugin;
		this.requestStack = new ArrayBlockingQueue<APIRequest>(256);
	}
	
	public synchronized String processRequest(APIRequest request) throws Exception {
		this.currentRequest = request;
		
		String response;
		
		try{
			URLConnection conn = (new URL(request.getURL())).openConnection();
			
			conn.setUseCaches(false);
			conn.setConnectTimeout(8000);
			conn.setReadTimeout(8000);
			
			String requestData = request.getData();
			
			if (!requestData.isEmpty()){
				conn.setDoOutput(true);
				
				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
				
				out.write("request_data=" + URLEncoder.encode(requestData, "UTF-8"));
				
				out.flush();
				out.close();
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line;
			StringBuilder buffer = new StringBuilder();
			
			while ((line = in.readLine()) != null){
				buffer.append(line);
			}
			
			response = buffer.toString();
			
			in.close();
			
			if (MineBansBungeeCord.DEBUG_MODE){
				plugin.log.info("======================== REQUEST DUMP =========================");
				plugin.log.info(" URL: " + request.getURL().toString());
				plugin.log.info(" Request: " + requestData);
				plugin.log.info(" Response: " + response);
				plugin.log.info("===============================================================");
			}
			
			if (response == null || response.startsWith("E")){
				throw new APIException(response);
			}
		}catch (Exception exception){
			throw exception;
		}finally{
			this.currentRequest = null;
		}
		
		return response;
	}
	
	@Override
	public void run(){
		while (true){
			try{
				final APIRequest request = this.requestStack.take();
				
				try{
					request.onSuccess(this.processRequest(request));
				}catch (final Exception e){
					request.onFailure(e);
				}
			}catch (InterruptedException e1){
				return;
			}
		}
	}
	
	public void addRequest(APIRequest request){
		// This is only to prevent accidental exponential queue growth DOSing the API.
		if (this.requestStack.remainingCapacity() == 0){
			plugin.log.log(Level.WARNING, "API request queue overloaded, waiting for some to complete.");
		}
		
		try{
			this.requestStack.put(request);
		}catch (InterruptedException e){  }
	}
	
	public APIRequest getCurrentRequest(){
		return this.currentRequest;
	}
	
}
