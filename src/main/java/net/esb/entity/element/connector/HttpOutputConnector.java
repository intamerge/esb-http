/**
 * Copyright (c) 2016-2017 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.element.common.ElementCommonConstants.*;
import static net.esb.entity.element.common.ElementNetworkConstants.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.esb.context.IMapElementContext;
import net.esb.entity.common.EntityStartException;
import net.esb.entity.common.EntityStopException;
import net.esb.entity.element.common.ElementCommonConstants;
import net.esb.entity.element.common.ElementHttpConstants;
import net.esb.entity.element.common.ElementHttpUtils;
import net.esb.entity.element.common.ElementUtils;
import net.esb.message.IMapMessage;

/*
 * Grizzly: The Async Http Client library's purpose is to allow Java applications to easily execute HTTP requests 
 * and asynchronously process the HTTP responses. 
 * The library also supports the WebSocket Protocol. 
 * The Async HTTP Client library is simple to use.
 * It's built on top of Netty and currently requires JDK8.
 * https://github.com/AsyncHttpClient/async-http-client
 *
 */

/**
 * ref: https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html#d5e49
 *
 *
 */
@Component
@Scope(value = "prototype")
public class HttpOutputConnector extends AbstractMapOutputConnector<HttpOutputConnectorDefinition, HttpOutputConnector> {
    
	final static String REQUEST_PREFIX = ElementUtils.requestPrefix(HttpOutputConnector.class);
	final static String RESPONSE_PREFIX = ElementUtils.responsePrefix(HttpOutputConnector.class);
	
	
	@Override
	public boolean isSynchronous() {
		return true;
	}
	
	@net.esb.entity.common.ElementDefinitionProperty(PROP_URL) 
    public String url = "http://localhost:8888/esb";

	@net.esb.entity.common.ElementDefinitionProperty(PROP_OUTPUT_TYPE) 
    public ElementCommonConstants.OUTPUT_TYPE output = ElementCommonConstants.OUTPUT_TYPE.TEXTSTRING;

	@net.esb.entity.common.ElementDefinitionProperty(PROP_ENCODING) 
    public String encoding;
	
	public String getUrl() {
		return url;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static class InputStreamWrapper extends InputStream{
		
		InputStream stream;
		CloseableHttpResponse response;
		
		public InputStreamWrapper(InputStream stream, CloseableHttpResponse response) {
			this.stream = stream;
			this.response = response;
		}

		public int read() throws IOException {
			return stream.read();
		}

		public int hashCode() {
			return stream.hashCode();
		}

		public int read(byte[] b) throws IOException {
			return stream.read(b);
		}

		public boolean equals(Object obj) {
			return stream.equals(obj);
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return stream.read(b, off, len);
		}

		public long skip(long n) throws IOException {
			return stream.skip(n);
		}

		public String toString() {
			return stream.toString();
		}

		public int available() throws IOException {
			return stream.available();
		}

		public void close() throws IOException {
			stream.close();
			response.close();
		}

		public void mark(int readlimit) {
			stream.mark(readlimit);
		}

		public void reset() throws IOException {
			stream.reset();
		}

		public boolean markSupported() {
			return stream.markSupported();
		}


	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void sinkAsynchronousOutputMapRequest(IMapMessage message, IMapElementContext context) throws Exception{
		sinkMessageImpl(message, context);
	}

	PoolingHttpClientConnectionManager cm;
	CloseableHttpClient httpclient;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IMapMessage sinkSynchronousOutputMapRequest(IMapMessage message, IMapElementContext context) throws Exception{		
		return sinkMessageImpl(message, context);
	}

	IMapMessage sinkMessageImpl(IMapMessage requestMessage, IMapElementContext context) throws Exception{
		String _url = this.<String>parseTypedProperty(String.class, getUrl());
		
		HttpGet httpget = new HttpGet(_url);
		
		httpget.addHeader("content-type", ElementHttpConstants.DEFAULT_CONTENT_TYPE);
		boolean isRequest = true;
		
//		IHeaderSettable headerSettable = new IHeaderSettable(){
//			@Override
//			public void addHeader(String name, String value) {
//				httpget.addHeader(name, value);
//			}
//		};
		
		ElementHttpUtils.setHttpHeaders(
				isRequest, 
				(name, value)->{httpget.addHeader(name, value);}, 
				requestMessage, 
				context, 
				REQUEST_PREFIX, 
				RESPONSE_PREFIX);
		
		CloseableHttpResponse response = null;
		
		Object payload = null;
		
		try {
			response = httpclient.execute(httpget);
			
			HttpEntity entity = response.getEntity();
			
		    if (entity != null) {
		        InputStream instream = entity.getContent();

		        switch(output){
		        case INPUTSTREAM:
		        	payload = new InputStreamWrapper(instream, response);
		        	break;
		        case TEXTSTRING:
		        default:
		        	payload = IOUtils.toString(instream, encoding);
		        	instream.close();
		        	
		        }

		    }
		} catch(Exception e) {
		    if(null!=response){response.close();}
		    throw(e);
		}
		
		IMapMessage responseMessage = getMap().buildMessage(payload);
		getMap().transferProperties(requestMessage, responseMessage);
		
        HeaderIterator iterator = response.headerIterator();
        while(iterator.hasNext()){
        	Header header = (Header)iterator.next();
        	responseMessage.writeProperty(RESPONSE_PREFIX+header.getName(), header.getValue());
        }
		
		return responseMessage;
	}

	@Override
	public void start() throws EntityStartException {
    	if(getStarted()){return;}
    	
    	cm = new PoolingHttpClientConnectionManager();
    			 
    	cm.setMaxTotal(10);
    	
    	httpclient = HttpClients.custom().setConnectionManager(cm).build();

        
		super.start();
	}


	@Override
	public void stop() throws EntityStopException {
		if(!getStarted() ){return;}
		
		try{
			httpclient.close();
			httpclient = null;
		}catch(Exception e){
			throw new EntityStopException(e);
		}finally{
			cm.close();
		}
		
		super.stop();
	}

	public ElementCommonConstants.OUTPUT_TYPE getOutput() {
		return output;
	}
	
	@net.esb.entity.common.ElementPropertySetter
	public void setOutput(ElementCommonConstants.OUTPUT_TYPE output) {
		this.output = output;
	}
	
	


}

