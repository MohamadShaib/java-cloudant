package com.cloudant.client.api.model;
/**
 * Represents optional configuration properties for connecting to CloudantDB.
 * @author Ganesh K Choudhary
 */
public class ConnectOptions {

	private int socketTimeout;
	private int connectionTimeout ;
	private int connectionRequestTimeout;

	private int maxConnections ;
	
	private String proxyHost ;
	private int proxyPort ;
	private boolean isSSLAuthenticationDisabled;
	
	public ConnectOptions(){
		// default constructor
	}

	public ConnectOptions setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		return this ;
	}

	public ConnectOptions setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this ;
	}

	public ConnectOptions setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
		return this ;
	}

	public ConnectOptions setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
		return this ;
	}

	public ConnectOptions setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
		return this ;
	}

	/** Sets whether hostname verification and certificate chain validation
	 * should be disabled.
	 * @param disabled set to true to disable or false to enable.
	 * @return the updated {@link ConnectOptions} object.
	 * @see #isSSLAuthenticationDisabled */
	public ConnectOptions setSSLAuthenticationDisabled(boolean disabled) {
		this.isSSLAuthenticationDisabled = disabled;
		return this;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	/** @return true if hostname verification and certificate chain validation are
	 *  set to disabled or false otherwise.
	 *  @see #setSSLAuthenticationDisabled(boolean) */
	public boolean isSSLAuthenticationDisabled() {
		return isSSLAuthenticationDisabled;
	}

	/** return connection request time out , how long request will wait to get connection from connection pool
	 * @see #setConnectionRequestTimeout */
	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	/** Sets connection request time out , how long request will wait to get connection from connection pool
	 * @param connectionRequestTimeout set the time to wait for connection.
	 * @return the updated {@link ConnectOptions} object.
	 * @see #getConnectionRequestTimeout */
	public ConnectOptions setConnectionRequestTimeout(int connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
		return this;
	}
}
