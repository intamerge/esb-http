/**
 * Copyright (c) 2016-2017 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package sandpit;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;

import net.esb.json.JsonUtils;

public class HttpClient {

	public static void main(String[] args) throws Exception {

		String url = "http://localhost:9999/mycontext";
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(url);
		HttpClientContext context = HttpClientContext.create();
		
		{
		BasicCookieStore cookieStore = new BasicCookieStore();
		BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", "1234");
		cookie.setDomain("localhost");
		cookie.setPath("/");
		cookieStore.addCookie(cookie);
		
		context.setCookieStore(cookieStore);
		}

		CloseableHttpResponse response = httpclient.execute(httpget, context);
		Object payload = null;
		
		try {
			HttpEntity entity = response.getEntity();
		    if (entity != null) {
		    	
		    	System.err.println("Success");
		    	
		        InputStream instream = entity.getContent();

	            if(true){
	            	payload = IOUtils.toString(instream, "UTF-8");
	            	instream.close();
	            	System.err.println(payload);
	            }
	            
	            {
		            CookieStore cookieStore = context.getCookieStore();
		            List<Cookie> cookies = cookieStore.getCookies();
		            if(cookies.size()>0){
		            	for(Cookie cookie: cookies){
		            		System.err.println(JsonUtils.toJsonString(cookie));
		            	}
		            }
	            }
		    }
		} catch(Exception e) {
		    response.close();
		    throw(e);
		}

	}

}
