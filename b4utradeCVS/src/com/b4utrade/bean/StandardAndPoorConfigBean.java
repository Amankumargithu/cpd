package com.b4utrade.bean;

import java.util.Properties;

public class StandardAndPoorConfigBean {
	private Properties userConfigurationProperties;
	private boolean configurationLoadedFlag = false;

	public Properties getUserConfigurationProperties() {
		return userConfigurationProperties;
	}

	public void setUserConfigurationProperties(Properties prop) {
		this.userConfigurationProperties = prop;
	}

	public boolean isConfigurationLoadedFlag() {
		return configurationLoadedFlag;
	}

	public void setConfigurationLoadedFlag(boolean configurationLoadedFlag) {
		this.configurationLoadedFlag = configurationLoadedFlag;
	}

	public String getParameter(String key) {
		return (userConfigurationProperties == null ? null : userConfigurationProperties.getProperty(key));
	}
}
