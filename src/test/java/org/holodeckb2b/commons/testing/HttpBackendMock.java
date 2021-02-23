/*******************************************************************************
 * Copyright (C) 2020 The Holodeck Team, Sander Fieten
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.holodeckb2b.commons.testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.holodeckb2b.commons.util.Utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simulates a back-end system that accepts HTTP request. It has three paths to simulate acceptance, rejection and 
 * timeouts. For both acceptance and rejection responses the HTTP status code to use can be set. Additionally it is
 * possible to set the number of requests that must be made before it will be accepted as successful.   
 * 
 * @author Sander Fieten (sander at holodeck-b2b.org)
 */
@SuppressWarnings("restriction")
public class HttpBackendMock {
	
	private HttpServer  server;
	private int			successCode = 202;
	private int			rejectionCode = 500;
	
	private int			acceptAfter = 0;
	private int			attempts = 0;
	
	private URI					requestURL;
	private Map<String, String> headers;	
	private byte[]				entityBody;
	
    public HttpBackendMock(final int timeout) throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/accept", new AcceptHandler());
        server.createContext("/reject", new RejectHandler());
        server.createContext("/timeout", new TimeoutHandler(timeout));
    }
    
    public void start() {
        server.start();
        System.out.println("[HttpBackendMock] Started at port: " + getPort());
    }
    
    public int getPort() {
        return server.getAddress().getPort();
    }
    
    public void setSuccessCode(int code) {
    	successCode = code;
    }
    
    public void setRejectionCode(int code) {
    	rejectionCode = code;
    }
    
    public void setMinRetries(int attempts) {
    	this.acceptAfter = attempts;
    }
    
    public int getAttempts() {
    	return attempts;
    }
    
    public void resetAttempts() {
    	this.attempts = 0;
    }
    
    public void stop() {
        server.stop(0);
    }
    
    public URI getRequestURL() {
    	return requestURL;
    }
    
    public Map<String, String> getRcvdHeaders() {
    	return headers;
    }
    
    public byte[] getRcvdData() {
    	return entityBody;
    }    

    class BaseHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange t) throws IOException {
			requestURL = t.getRequestURI();
			headers = null;
			entityBody = null;

			System.out.println("[HttpBackendMock] Handling request for URL: " + requestURL);
			attempts++;
			
			headers = new HashMap<>();
			for(Map.Entry<String, List<String>> h : t.getRequestHeaders().entrySet()) {
				String hv = "";
				for(int i = 0; i < h.getValue().size() - 1; i++)
					hv += h.getValue().get(i) + ",";
				hv += h.getValue().get(h.getValue().size() - 1);
				headers.put(h.getKey(), hv);
			}			
			
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				Utils.copyStream(t.getRequestBody(), bos);
				entityBody = bos.toByteArray();
			}
			
			if (entityBody.length == 0)
				entityBody = null;			
		}    	
    }
    
    class AcceptHandler extends BaseHandler {
    	@Override
        public void handle(HttpExchange t) throws IOException {
        	super.handle(t);
            
	        t.sendResponseHeaders(attempts > acceptAfter ? successCode : rejectionCode, 0);
        	t.close();
        }
    }

    class RejectHandler extends BaseHandler {
    	@Override
    	public void handle(HttpExchange t) throws IOException {
    		super.handle(t);    		    		
    		
    		t.sendResponseHeaders(rejectionCode, 0);
    		t.close();
    	}
    }
 
    class TimeoutHandler extends BaseHandler {
    	private int timeout;
    	
    	TimeoutHandler(int t) {
    		timeout = t;
    	}
    	
    	@Override
    	public void handle(HttpExchange t) throws IOException {
    		super.handle(t);
    		
    		try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
			}

    		t.sendResponseHeaders(successCode, 0);
    		t.close();
    	}
    }
    
}
