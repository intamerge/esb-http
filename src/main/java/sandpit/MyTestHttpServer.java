/**
 * Copyright (c) 2016-2017 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package sandpit;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.TimeoutHandler;

public class MyTestHttpServer {

	public static void main(String[] args) {
		String CONTEXT = "/time";
		HttpServer server = HttpServer.createSimpleServer("./", "0.0.0.0", 7777);
		
		server.getServerConfiguration().addHttpHandler(
		    new HttpHandler() {
		        public void service(Request request, Response response) throws Exception {
		            final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		            final String date = format.format(new Date(System.currentTimeMillis()));
		            
		            @SuppressWarnings("unused")
					CompletionHandler<Response> completionHandler = new CompletionHandler<Response>(){

						@Override
						public void cancelled() {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void failed(Throwable throwable) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void completed(Response result) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void updated(Response result) {
							// TODO Auto-generated method stub
							
						}
		            	
		            };
		            @SuppressWarnings("unused")
					TimeoutHandler timeoutHandler = new TimeoutHandler(){

						@Override
						public boolean onTimeout(Response response) {
							return true;
						}
		            	
		            };
		            

		            response.setContentType("text/plain");
		            response.setContentLength(date.length());
		            response.getWriter().write(date);
		        }
		    },
		    CONTEXT);

		try {
		    server.start();
		    System.out.println("Press any key to stop the server...");
		    System.in.read();
		} catch (Exception e) {
		    System.err.println(e);
		}

	}

}
