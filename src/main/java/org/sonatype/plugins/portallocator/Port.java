package org.sonatype.plugins.portallocator;

import org.apache.maven.plugin.MojoExecutionException;

public class Port {

	public static final String DEFAULT = "default";

	/**
	 * When true will break the build if the preferred port number is not available. If false will allocate another
	 * port.
	 */
	private boolean failIfOccupied;

	/**
	 * Port name used to add to properties. Required.
	 */
	private String name;

	/**
	 * A preferred port
	 */
	private int portNumber;

	/**
	 * The port type.
	 *
	 * @see org.sonatype.plugins.portallocator.PortType
	 */
	private PortType type = PortType.RANDOM;

	public Port() {
		super();
	}

	public Port(String name) {
		super();
		this.name = name;
	}

	public boolean getFailIfOccupied() {
		return failIfOccupied;
	}

	public String getName() {
		return name;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public PortType getType() {
		return type;
	}

	public void setFailIfOccupied(boolean failIfOccupied) {
		this.failIfOccupied = failIfOccupied;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public void setType(PortType type) {
		this.type = type;
	}

	public int allocatePort(PortAllocator allocator) throws PortUnavailableException, MojoExecutionException {
		return type.allocationStrategy.allocatePort(getPortNumber(), allocator);
	}

}
