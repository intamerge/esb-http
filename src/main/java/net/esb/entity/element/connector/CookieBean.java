/**
 * Copyright (c) 2016-2017 Intamerge http://www.intamerge.com
 * All Rights Reserved.
 *
 * This source code is licensed under AGPLv3 and allows you to freely download and use this source:  Try it free !
 * This license does not extend to source code in other Intamerge source code projects, please refer to those projects for their specific licensing.
 */

package net.esb.entity.element.connector;

import java.time.Duration;
import java.time.Instant;

public class CookieBean {
	

    protected String name;
    protected String value; 
    
    protected String comment; 
    protected String domain;    
//    an integer specifying the maximum age of the
//    cookie in seconds; if negative, means
//    the cookie persists until browser shutdown
    protected int maxAge = -1;    
    protected String path;   
    protected boolean secure;   
    protected int version = 0;    

    protected boolean isHttpOnly;
    
    /**
     * Convert expiryDate to maxAge
     * NB: this has not been tested.
     * 
     * @param tokenExpires
     * @return
     */
    private static int createCookieMaxAge(java.util.Date tokenExpires) {  
  	  return (int) Duration.between(
  	    Instant.now(),
  	    Instant.ofEpochSecond(tokenExpires.getTime()*1000)
  	    ).getSeconds();
  	}
    
    public CookieBean(){}

    public org.glassfish.grizzly.http.Cookie toGrizzlyCookie(){
    	
    	org.glassfish.grizzly.http.Cookie clone = new org.glassfish.grizzly.http.Cookie(name, value);
    	
    	clone.setComment(comment);
    	clone.setDomain(domain);
    	clone.setMaxAge(maxAge);
    	clone.setPath(path);
    	clone.setVersion(version);
    	clone.setSecure(secure);
    	clone.setHttpOnly(isHttpOnly);
    	
    	return clone;
    }
    
    public CookieBean(String name, String value){
    	this.name = name;
    	this.value = value;
    }
    
    public CookieBean(org.glassfish.grizzly.http.Cookie toClone){
    	name = toClone.getName();
    	value = toClone.getValue();
    	comment = toClone.getComment();
    	domain = toClone.getDomain();
    	maxAge = toClone.getMaxAge();
    	path = toClone.getPath();
    	version = toClone.getVersion();
    	secure = toClone.isSecure();
    	isHttpOnly = toClone.isHttpOnly();
    }
    
    public CookieBean(org.apache.http.cookie.Cookie toClone){    	
    	name = toClone.getName();
		value = toClone.getValue();
		comment = toClone.getComment();
		domain = toClone.getDomain();
		maxAge = createCookieMaxAge(toClone.getExpiryDate());
		path = toClone.getPath();
		version = toClone.getVersion();
		secure = toClone.isSecure();

    }
    
    public CookieBean(javax.servlet.http.Cookie toClone){
    	name = toClone.getName();
    	value = toClone.getValue();
    	comment = toClone.getComment();
    	domain = toClone.getDomain();
    	maxAge = toClone.getMaxAge();
    	path = toClone.getPath();
    	version = toClone.getVersion();
    	secure = toClone.getSecure();
    	isHttpOnly = toClone.isHttpOnly();
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isHttpOnly() {
		return isHttpOnly;
	}

	public void setHttpOnly(boolean isHttpOnly) {
		this.isHttpOnly = isHttpOnly;
	}

    
}
