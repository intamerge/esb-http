/**
 * Copyright (c) 2016-2017 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.element.common.ElementNetworkConstants.*;

import java.io.IOException;
import java.nio.charset.Charset;

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
import net.esb.entity.common.EntityStartException;
import net.esb.entity.common.EntityStopException;
import net.esb.entity.element.common.ElementFileConstants;
import net.esb.message.IMapMessage;

/*
 * TODO: enable/disable of chunking at the filter level to prevent the chunked transfer-encoding from being used.
 * TODO: SSL filter
 */

/**
 * Class initializes and starts the tcp server, based on Grizzly 2.3
 * 
 * Grizzly Transport implementation can operate over TCP, UDP or other custom protocols, using blocking, NIO or NIO.2 Java API.
 * 
 * 
 * <p>
 * <pre>
 * Notes:
 * 
 * Configuration items to be considered:
 * 
 * https://grizzly.java.net/coreconfig.html
 * 
 * keepAlive
 * tcpNoDelay
 * serverConnectionBackLog
 * linger
 * asyncQueueIO
 * DEFAULT_KEEP_ALIVE 
 * DEFAULT_LINGER 
 * DEFAULT_SERVER_CONNECTION_BACKLOG
 * DEFAULT_TCP_NO_DELAY 
 * MAX_RECEIVE_BUFFER_SIZE 
 * 
 * From AbstractTransport:
 * standalone
 * blocking
 * kernelPoolConfig
 * name
 * readBufferSize
 * readTimeout
 * state - Transport state controller
 * strategy
 * threadPoolMonitoringConfig - thread pool probes
 * transportMonitoringConfig - transport probes
 * writeBufferSize - Transport default buffer size for write operations
 * writeTimeout
 * 
 * From NIOTransport
 * DEFAULT_CLIENT_SOCKET_SO_TIMEOUT 
 * DEFAULT_CONNECTION_TIMEOUT 
 * DEFAULT_OPTIMIZED_FOR_MULTIPLEXING 
 * DEFAULT_REUSE_ADDRESS 
 * DEFAULT_SELECTOR_RUNNER_COUNT 
 * DEFAULT_SERVER_SOCKET_SO_TIMEOUT 
 * 
 * From Transport
 * DEFAULT_READ_BUFFER_SIZE, DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_BUFFER_SIZE, DEFAULT_WRITE_TIMEOUT
 * IOStrategy Set the IOStrategy implementation, which will be used by Transport to process IOEvent.
 * threadPoolConfig
 * standalone
 * blocking
 * KernelThreadPoolConfig Set the ThreadPoolConfig to be used by the Transport internal thread pool.
 * MonitoringConfig
 * name - Sets the Transport name.
 * readBufferSize - Set the default size of Buffers, which will be allocated for reading data from Transport's Connections.
 * readTimeout - Specifies the timeout for the blocking reads.
 * state
 * threadpoolmonitoringconfig
 * workertheadpoolconfig - Set the ThreadPoolConfig to be used by the worker thread pool.
 * writeBufferSize -Set the default size of Buffers, which will be allocated for writing data to Transport's Connections.
 * writetimeout - Specifies the timeout for the blocking writes. Returns the current value for the blocking write timeout converted to the provided TimeUnit specification.
 * pause - Pauses the transport
 * resume - Resumes the transport after a pause
 * stopped - Returns true, if this Transport is in stopped state, false otherwise.
 * 
 */

@Component
@Scope(value = "prototype")
public class TcpInputConnector extends AbstractMapInputConnector<TcpInputConnectorDefinition, TcpInputConnector> {
	
	TCPNIOTransport transport;

	@net.esb.entity.common.ElementDefinitionProperty(PROP_HOST) 
    public String host = "0.0.0.0";
	
	// this is a string here to allow variable interpolation
	@net.esb.entity.common.ElementDefinitionProperty(PROP_PORT) 
    public String port = "" + -1;
	
	
	@Override
	public boolean isSynchronous(){
		return true;
	}
    
	@Override
    public void start() throws EntityStartException{
    	
    	if(getStarted()){return;}
    	
        // Create a FilterChain using FilterChainBuilder
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();

        // Add TransportFilter, which is responsible
        // for reading and writing data to the connection
        filterChainBuilder.add(new TransportFilter());

        // StringFilter is responsible for Buffer <-> String conversion
        // TODO this should be removed. any conversion must be a property or a component on the map
        filterChainBuilder.add(new StringFilter(Charset.forName(ElementFileConstants.DEFAULT_ENCODING)));

        // EchoFilter is responsible for echoing received messages
        filterChainBuilder.add((BaseFilter) new ResponseFilter(this));

        // Create TCP transport
        transport =  TCPNIOTransportBuilder.newInstance().build();

        transport.setProcessor(filterChainBuilder.build());

        try {
        	
        	String _host = this.<String>parseTypedProperty(String.class, getHost());
        	Integer _port = this.<Integer>parseTypedProperty(Integer.class, getPort());
        	// binding transport to start listen on certain host and port
        	transport.bind(_host, _port);
        	// start the transport
			transport.start();
		} catch (Exception e) {
			throw new EntityStartException(e);
		}
        
        super.start();
 
    }

	@Override
    public void stop() throws EntityStopException{

    	if(!getStarted() ){return;}
    	
        // stop the transport
        try {
        	// shutdownNow Forcibly stops the transport and closes all connections.
        	// options are 
        	// shutdown() Gracefully stops the transport accepting new connections and allows existing work to complete before finalizing the shutdown.
        	// shutdown(long gracePeriod, TimeUnit timeUnit) Gracefully stops the transport accepting new connections and allows existing work to complete before finalizing the shutdown.
			if(null!=transport){transport.shutdownNow();}
			super.stop();
		} catch (IOException e) {
			throw new EntityStopException(e);
		}
        

    }
    
    class ResponseFilter extends BaseFilter {
    	
		IMapInputConnector<?,?> inputConnector;
		
		public ResponseFilter(IMapInputConnector<?,?> inputConnector){
			super();
			this.inputConnector = inputConnector;
		}

        /**
         * Handle just read operation, when some message has come and ready to be
         * processed.
         *
         * @param ctx Context of {@link FilterChainContext} processing
         * @return the next action
         * @throws java.io.IOException
         */
        @Override
        public NextAction handleRead(FilterChainContext ctx)
                throws IOException {
            // Peer address is used for non-connected UDP Connection :)
            final Object peerAddress = ctx.getAddress();
            
            IMapMessage requestMessage = getMap().buildMessage(ctx.getMessage());
            IMapMessage responseMessage;
			try {
				IMapElementContext context = getMap().buildContext(inputConnector);
				responseMessage = postRequestToMap(context, requestMessage);
			} catch (Exception e) {
				responseMessage = requestMessage;
				responseMessage.setPayload(e.toString());
			}

            ctx.write(peerAddress, responseMessage.getPayload(), null);

            return ctx.getStopAction();
        }
    }
    

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setHost(String host) {
		this.host = host;
	}

	@net.esb.entity.common.ElementPropertySetter
	public void setPort(String port) {
		this.port = port;
	}
	
	


	
}

