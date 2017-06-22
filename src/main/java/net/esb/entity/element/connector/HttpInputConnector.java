/**
 * Copyright (c) 2016-2016 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.element.common.ElementNetworkConstants.PROP_CONTEXT;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_HOST;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_PORT;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_RESPONSETIMEOUT;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.http.Cookie;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.TimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.esb.context.IMapElementContext;
import net.esb.entity.common.ElementStartException;
import net.esb.entity.common.EntityStartException;
import net.esb.entity.common.EntityStopException;
import net.esb.entity.element.common.ElementFileConstants;
import net.esb.entity.element.common.ElementHttpConstants;
import net.esb.entity.element.common.ElementHttpUtils;
import net.esb.entity.element.common.ElementUtils;
import net.esb.message.AbstractMessageProperty;
import net.esb.message.IMapMessage;

@Component
@Scope(value = "prototype")
public class HttpInputConnector extends AbstractMapInputConnector<HttpInputConnectorDefinition, HttpInputConnector> {
	
	final static String REQUEST_PREFIX = ElementUtils.requestPrefix(HttpInputConnector.class);
	final static String RESPONSE_PREFIX = ElementUtils.responsePrefix(HttpInputConnector.class);
	final static String _source = HttpInputConnector.class.getSimpleName();
	
	transient Logger logger = LoggerFactory.getLogger(HttpInputConnector.class);
	
	HttpServer httpServer = null;
	HttpHandler myHandler = new MyHandler(this);
	NetworkListener networkListener;

	@net.esb.entity.common.ElementDefinitionProperty(PROP_HOST) 
    public String host = "0.0.0.0";
	
	@net.esb.entity.common.ElementDefinitionProperty(PROP_PORT) 
    public String port = "" + 9999;

	@net.esb.entity.common.ElementDefinitionProperty(PROP_CONTEXT) 
    public String context = "mycontext";
	
	@net.esb.entity.common.ElementDefinitionProperty(PROP_RESPONSETIMEOUT) 
	public String responseTimeout = "" + 10000;

	
	@Override
	public boolean isSynchronous(){
		return true;
	}
	
	public HttpInputConnector(){
		super();
	}
    
	@Override
    public void start() throws EntityStartException{
    	
    	if(getStarted()){return;}
    	
		httpServer = new HttpServer();

		String _host = this.<String>parseStartProperty(String.class, getHost());
		String _context = this.<String>parseStartProperty(String.class, getContext());
		Integer _port = this.<Integer>parseStartProperty(Integer.class, getPort());
		
		NetworkListener networkListener = new NetworkListener(getName(), _host, _port);
		httpServer.addListener(networkListener);
		String myContext = _context.startsWith("/")?_context:"/"+_context;
		httpServer.getServerConfiguration().addHttpHandler(myHandler, myContext);
		    

		try {
			httpServer.start();
		    super.start();
		} catch (Exception e) {
		    throw new ElementStartException(e);
		}
        
 
    }
	
    @Override
    public void stop() throws EntityStopException{

    	if(!getStarted()){return;}
    	
        // stop the transport
        
        // shutdownNow Forcibly stops the transport and closes all connections.
    	// options are 
    	// shutdown() Gracefully stops the transport accepting new connections and allows existing work to complete before finalizing the shutdown.
    	// shutdown(long gracePeriod, TimeUnit timeUnit) Gracefully stops the transport accepting new connections and allows existing work to complete before finalizing the shutdown.
		if(null!=networkListener){
			try {
				networkListener.shutdownNow();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	if(null!=httpServer){httpServer.shutdownNow();}
		
        
        super.stop();

    }

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}


	public String getContext() {
		return context;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setContext(String context) {
		this.context = context;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setHost(String host) {
		this.host = host;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setPort(String port) {
		this.port = port;
	}
	
	
	public class MyHandler extends HttpHandler{
		
		IMapInputConnector<?,?> inputConnector;
		
		public MyHandler(IMapInputConnector<?,?> inputConnector){
			super();
			this.inputConnector = inputConnector;
		}

        public void service(Request request, Response response) throws Exception {
            
            CompletionHandler<Response> completionHandler = new CompletionHandler<Response>(){

            	// TODO implement the following methods
            	
				@Override
				public void cancelled() {
					//TODO
					@SuppressWarnings("unused")
					int x=1;
				}

				@Override
				public void failed(Throwable throwable) {
					//TODO
					@SuppressWarnings("unused")
					int x=1;
				}

				@Override
				public void completed(Response result) {
					//TODO
					@SuppressWarnings("unused")
					int x=1;
				}

				@Override
				public void updated(Response result) {
					//TODO
					@SuppressWarnings("unused")
					int x=1;
				}
            	
            };
            
            TimeoutHandler timeoutHandler = new TimeoutHandler(){

				@Override
				public boolean onTimeout(Response response) {
					return true;
				}
            	
            };
            
            Integer _responseTimeout = getMap().<Integer>parseProperty(Integer.class, responseTimeout);
            response.suspend(_responseTimeout, TimeUnit.MILLISECONDS, completionHandler, timeoutHandler);
            
            String requestPayload = null;
            if(request.getMethod() == Method.GET){
            	requestPayload = request.getQueryString();
            }else if(request.getMethod() == Method.POST){
            	requestPayload = IOUtils.toString(request.getInputStream(), ElementFileConstants.DEFAULT_ENCODING);
            }
            IMapMessage requestMessage = getMap().buildMessage(requestPayload);
            
            putPropertiesOnMessage(requestMessage, request);

            Cookie[] cookies = request.getCookies();
            putCookiesOnMessage(requestMessage, cookies);
            
            
            // propagate the message to the map            
            IMapElementContext context = getMap().buildContext(inputConnector);
            IMapMessage responseMessage = postRequestToMap(context, requestMessage);
            
            // TODO support streams
            String responsePayload = (null!=responseMessage.getPayload())?responseMessage.getPayload().toString() : "";
            int responseLength = responsePayload.length();
            
    		response.addHeader("content-type", ElementHttpConstants.DEFAULT_CONTENT_TYPE);
    		
    		putCookiesOnResponse(response, responseMessage);
    		
    		boolean isRequest = false;
    		
    		ElementHttpUtils.setHttpHeaders(
    				isRequest, 
    				(name, value)->{response.addHeader(name, value);}, 
    				requestMessage, 
    				context, 
    				REQUEST_PREFIX, 
    				RESPONSE_PREFIX);
            
            response.setContentLength(responseLength);
            response.getWriter().write(responsePayload);
            response.resume();
            
        }


	}
	
	@SuppressWarnings("unchecked")
	void putCookiesOnResponse(Response response, IMapMessage responseMessage) {
		AbstractMessageProperty<?> responseCookies =  responseMessage.readProperty("cookies");
		
		if(null!=responseCookies){
			Object propVal = responseCookies.getValue();
			if(null!=propVal){
				if(propVal instanceof Collection<?>
				&& ((Collection<?>)propVal).iterator().next() instanceof CookieBean
				){
					for(CookieBean cookieBean: (Collection<CookieBean>)propVal){
						Cookie grizzlyCookie = cookieBean.toGrizzlyCookie();
						response.addCookie(grizzlyCookie);
					}
				}
				else if(propVal instanceof Object[]
						&& ((Object[])propVal).length>0
						&& ((Object[])propVal)[0] instanceof CookieBean){
					
					for(Object cookieBean: (Object[])propVal){
						org.glassfish.grizzly.http.Cookie grizzlyCookie = ((CookieBean)cookieBean).toGrizzlyCookie();
						response.addCookie(grizzlyCookie);
					}
				}
			}
		}
		
	}
	
	void putPropertiesOnMessage(IMapMessage requestMessage, Request request) throws CharConversionException {
		Iterable<String> iter = request.getHeaderNames();
		for(String key: iter){
			String value = request.getHeader(key);
			requestMessage.writeProperty(key, value, _source);
			if(logger.isDebugEnabled()){logger.debug("http request parameter "+key+"="+value);}
		}
		
		requestMessage.writeProperty("http.method", request.getMethod().getMethodString(), _source);
		requestMessage.writeProperty("remote.addr", request.getRemoteAddr(), _source);
		requestMessage.writeProperty("remote.host", request.getRemoteHost(), _source);
		requestMessage.writeProperty("remote.port", request.getRemotePort(), _source);
		requestMessage.writeProperty("request.uri", request.getRequestURI(), _source);
		requestMessage.writeProperty("request.url", request.getRequestURL(), _source);
		requestMessage.writeProperty("decoded.request.uri", request.getDecodedRequestURI(), _source);
		requestMessage.writeProperty("query.string", request.getQueryString(), _source);
		
		// for http://localhost:9999/mycontext?name=fred
		request.getRemoteAddr(); // 127.0.0.1
		request.getRemoteHost(); // localhost
		request.getRemotePort(); // 35548
		request.getRequestURI(); // /mycontext or /mycontext/apples/12/pears/13 for http://localhost:9999/mycontext/apples/12/pears/13
		request.getRequestURL(); // http://localhost:9999/mycontext or http://localhost:9999/mycontext/apples/12/pears/13
		request.getDecodedRequestURI(); // "" or /mycontext/apples/12/pears/13
		request.getQueryString(); // name=fred or null
		
        if(null!=request.getParameterMap()){
        	for(String key: request.getParameterMap().keySet()){
        		String value = request.getParameter(key);
        		requestMessage.writeProperty(key, value, _source);
        		if(logger.isDebugEnabled()){logger.debug("http request parameter "+key+"="+value);}
        	}
        }
	}
	
	void putCookiesOnMessage(IMapMessage requestMessage, Cookie[] cookies) {
		if(cookies.length!=0){
			List<CookieBean> messageCookies = new ArrayList<CookieBean>();
			for(Cookie cookie: cookies){
				CookieBean cookieBean = new CookieBean(cookie);
				messageCookies.add(cookieBean);
			}
			requestMessage.writeProperty("cookies", messageCookies);
		}
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setResponseTimeout(String responseTimeout) {
		this.responseTimeout = responseTimeout;
	}
	
	

	
}

