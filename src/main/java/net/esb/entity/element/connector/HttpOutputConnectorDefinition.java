/**
 * Copyright (c) 2016-2016 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import static net.esb.entity.common.ElementStandardIcon.OUTPUT_CONNECTOR;
import static net.esb.entity.common.EntityConfigurationProperty.ElementPropertyType.CHOICE;
import static net.esb.entity.common.EntityConfigurationProperty.ElementPropertyType.STRING;
import static net.esb.entity.element.common.ElementCommonConstants.PROP_ENCODING;
import static net.esb.entity.element.common.ElementCommonConstants.PROP_ENCODING_DESC;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_URL;
import static net.esb.entity.element.common.ElementNetworkConstants.PROP_URL_DESC;

import org.springframework.stereotype.Component;

import net.esb.build.BuildInfo;
import net.esb.entity.common.Colors;
import net.esb.entity.common.EntityConfigurationProperty;
import net.esb.entity.element.common.ElementCommonConstants;
import net.esb.entity.element.common.ElementHttpConstants;
import net.esb.plugin.IPluginInfo;
import net.esb.plugin.PluginInfo;

@Component
public class HttpOutputConnectorDefinition extends AbstractConnectorDefinition<HttpOutputConnectorDefinition, HttpOutputConnector>{
	
	private static final long serialVersionUID = 1L;
	

	
	final String name = "Http Output Connector";
	final String description = "Http output connector is an http client";
	final String url = OUTPUT_CONNECTOR;
	
	IPluginInfo pluginInfo =  null;
	
	public HttpOutputConnectorDefinition() {
		super();
		registerProperties();
		resolveEntityBeanProperties();
	}
	
	void registerProperties(){
		configurationProperties.add(new EntityConfigurationProperty(PROP_URL, PROP_URL_DESC, STRING, NOTREADONLY, 50));
		EntityConfigurationProperty operation = new EntityConfigurationProperty(ElementHttpConstants.PROP_OUTPUT_TYPE, ElementHttpConstants.PROP_OUTPUT_TYPE_DESC, CHOICE, NOTREADONLY, 51);
		operation.setChoices(choicesMapFromEnum(ElementCommonConstants.OUTPUT_TYPE.class, null));
		configurationProperties.add(operation); 

		configurationProperties.add(new EntityConfigurationProperty(PROP_ENCODING, PROP_ENCODING_DESC, STRING, NOTREADONLY, 52));

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
