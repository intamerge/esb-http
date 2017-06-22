/**
 * Copyright (c) 2016-2016 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.common.EntityConfigurationProperty.ElementPropertyType.INTEGER;
import static net.esb.entity.common.EntityConfigurationProperty.ElementPropertyType.STRING;
import static net.esb.entity.common.ElementStandardIcon.INPUT_CONNECTOR;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_CONTEXT;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_CONTEXT_DESC;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_HOST;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_HOST_DESC;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_PORT;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_PORT_DESC;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_RESPONSETIMEOUT;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_RESPONSETIMEOUT_DESC;

import org.springframework.stereotype.Component;

import net.esb.build.BuildInfo;
import net.esb.entity.common.Colors;
import net.esb.entity.common.EntityConfigurationProperty;
import net.esb.entity.element.connector.AbstractConnectorDefinition;
import net.esb.plugin.IPluginInfo;
import net.esb.plugin.PluginInfo;

@Component
public class HttpInputConnectorDefinition extends AbstractConnectorDefinition <HttpInputConnectorDefinition, HttpInputConnector>{
	
	private static final long serialVersionUID = 1L;
	
	final String name = "Http Input Connector";
	final String description = "Http input connector - acts like an HTTP server";
	final String url = INPUT_CONNECTOR;
	
	
	IPluginInfo pluginInfo = null;
	
	public HttpInputConnectorDefinition() {
		super();
		registerProperties();
		resolveEntityBeanProperties();
	}
	
	void registerProperties(){
		configurationProperties.add(new EntityConfigurationProperty(PROP_HOST, PROP_HOST_DESC, STRING, NOTREADONLY, 50));
		configurationProperties.add(new EntityConfigurationProperty(PROP_PORT, PROP_PORT_DESC, INTEGER, NOTREADONLY, 51));
		configurationProperties.add(new EntityConfigurationProperty(PROP_CONTEXT, PROP_CONTEXT_DESC, STRING, NOTREADONLY, 52));
		configurationProperties.add(new EntityConfigurationProperty(PROP_RESPONSETIMEOUT, PROP_RESPONSETIMEOUT_DESC, INTEGER, NOTREADONLY, 53));
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
			((PluginInfo)pluginInfo).setPluginColor(Colors.bg_inputgreen);

		}
		return pluginInfo;
	}
	

}
