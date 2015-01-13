package org.sonatype.plugins.portallocator;

import org.apache.maven.plugin.MojoFailureException;

public class PortUnavailableException
	extends MojoFailureException {

	private static final long serialVersionUID = -7410759462209491374L;

	public PortUnavailableException(final Object source, final String shortMessage, final String longMessage) {
		super(source, shortMessage, longMessage);
	}

	public PortUnavailableException(final String message) {
		super(message);
	}
}
