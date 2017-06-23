/**
 * Copyright (c) 2016-2016 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.common.EntityConfigurationProperty.ElementPropertyType.*;
import static net.esb.entity.common.ElementStandardIcon.*;
import static net.esb.entity.element.common.ElementNetworkConstants.*;

import org.springframework.stereotype.Component;

import net.esb.build.BuildInfo;
import net.esb.entity.common.Colors;
import net.esb.entity.common.EntityConfigurationProperty;
import net.esb.entity.element.connector.AbstractConnectorDefinition;
import net.esb.plugin.IPluginInfo;
import net.esb.plugin.PluginInfo;

@Component
public class TcpOutputConnectorDefinition extends AbstractConnectorDefinition<TcpOutputConnectorDefinition, TcpOutputConnector>{
	
	private static final long serialVersionUID = 1L;

	
	final String name = "Tcp Output Connector";
	final String description = "Tcp output connector is a tcp client";
	final String url = OUTPUT_CONNECTOR;
	
	IPluginInfo pluginInfo =  null;
	
	public TcpOutputConnectorDefinition() {
		super();
		registerProperties();
		resolveEntityBeanProperties();
	}
	
	void registerProperties(){
		configurationProperties.add(new EntityConfigurationProperty(PROP_HOST, PROP_HOST_DESC, STRING, NOTREADONLY, 50));
		configurationProperties.add(new EntityConfigurationProperty(PROP_PORT, PROP_PORT_DESC, INTEGER, NOTREADONLY, 51));
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public String getUrl(){
		return url;
	}
	
	@Override
	public String getDescription(){
		return description;
	}
	
	@Override
	public IPluginInfo getPluginInfo() {
		if(null==pluginInfo){
			pluginInfo = new PluginInfo(
					"Intamerge developers",
					getName(),
					BuildInfo.buildInfo().getVersion(),
					"Intamerge license",
					"Intamerge"
					);
			
			((PluginInfo)pluginInfo).setPluginIcon(getUrl());
			((PluginInfo)pluginInfo).setPluginColor(Colors.bg_outputmustard);

		}
		return pluginInfo;
	}
	

}
