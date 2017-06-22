/**
 * Copyright (c) 2016-2016 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.element.common.ElementNetworkConstants.PROP_HOST;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_PORT;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.esb.context.IMapElementContext;
import net.esb.dispatcher.IDispatcherLatch;
import net.esb.entity.common.EntityStartException;
import net.esb.entity.common.EntityStopException;
import net.esb.message.IMapMessage;


@Component
@Scope(value = "prototype")
public class TcpOutputConnector extends AbstractMapOutputConnector<TcpOutputConnectorDefinition, TcpOutputConnector> {
	
	
	@Override
	public boolean isSynchronous() {
		return true;
	}
	
	public class ResponseFilter extends BaseFilter {
	    /**
	     * Handle just read operation, when some message has come and ready to be
	     * processed.
	     *
	     * @param ctx Context of {@link FilterChainContext} processing
	     * @return the next action
	     * @throws java.io.IOException
	     */
	    @Override
	    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
	        // We get String message from the context, because we rely prev. Filter in chain is StringFilter
	        final String serverResponse = ctx.getMessage();
	        IMapMessage responseMessage = getMap().buildMessage(serverResponse);
	        
	        if(null!=latch){
	        	latch.setMessage(responseMessage);
        		latch.countDown();
	        	
	        };
	        return ctx.getStopAction();
	    }
	}

	@net.esb.entity.common.ElementDefinitionProperty(PROP_HOST) 
    public String host = "localhost";
	
	@net.esb.entity.common.ElementDefinitionProperty(PROP_PORT) 
    public String port = "" + -1;


	public String getHost() {
		return host;
	}


	public String getPort() {
		return port;
	}
		
	IDispatcherLatch latch;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void sinkAsynchronousOutputMapRequest(IMapMessage message, IMapElementContext context){}

	/**
	 * {@inheritDoc}
	 * @throws Exception 
	 */
	@Override
	protected IMapMessage sinkSynchronousOutputMapRequest(IMapMessage requestMessage, IMapElementContext context) throws Exception{
		// send the message
		// the response should be put into the message payload
		
		latch = getMap().newDispatcherLatch();
		
		if(requestMessage.getPayload()==null){
			throw new Exception("Cannot send null message");
		}
		connection.write(requestMessage.getPayload().toString());
		
		IMapMessage responseMessage = null;
		
		try {
			boolean result = getMap().awaitLatch(latch);
			if(false==result){
				Exception latchException = new DispatcherLatchException(this.getClass().getSimpleName()+" Dispatcher latch timed out");
				responseMessage = getMap().handleException(this, requestMessage, latchException);
			}else{
		        responseMessage = latch.getMessage();
		        getMap().transferProperties(requestMessage, responseMessage);
			}
		} catch (InterruptedException e) {
			Exception latchException = new DispatcherLatchException(this.getClass().getSimpleName()+" Dispatcher latch was interrupted, "+e.getMessage(), e);
			responseMessage = getMap().handleException(this, requestMessage, latchException);
		}
		
		return responseMessage;
		
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setHost(String host) {
		this.host = host;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setPort(String port) {
		this.port = port;
	}

	Connection<?> connection = null;
	
    // Create TCP transport
    final TCPNIOTransport transport = TCPNIOTransportBuilder.newInstance().build();

	@Override
	public void start() throws EntityStartException {
    	if(getStarted()){return;}
    	

        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        // Add TransportFilter, which is responsible
        // for reading and writing data to the connection
        filterChainBuilder.add(new TransportFilter());
        // StringFilter is responsible for Buffer <-> String conversion
        filterChainBuilder.add(new StringFilter(Charset.forName("UTF-8")));
        // ClientFilter is responsible for redirecting server responses to the standard output
        filterChainBuilder.add(new ResponseFilter());

        transport.setProcessor(filterChainBuilder.build());

        try{
	        // start the transport
	        transport.start();
	
			String _host = this.<String>parseStartProperty(String.class, getHost());
			Integer _port = this.<Integer>parseStartProperty(Integer.class, getPort());
	        
	        // perform async. connect to the server
			@SuppressWarnings("rawtypes")
			Future<Connection> future = transport.connect(_host, _port);
	        
	        // wait for connect operation to complete
	        connection = future.get(10, TimeUnit.SECONDS);
	
	        assert connection != null;
        }catch(Exception e){
        	throw new EntityStartException(e);
        }

        
		super.start();
	}


	@Override
	public void stop() throws EntityStopException {
		if(!getStarted() ){return;}
		
        // close the client connection
        if (connection != null) {
            connection.close();
        }

        // stop the transport
        try {
			transport.shutdownNow();
		} catch (IOException e) {
			throw new EntityStopException(e);
		}
        
		super.stop();
	}
	
	


}

