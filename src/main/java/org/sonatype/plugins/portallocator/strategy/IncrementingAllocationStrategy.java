package org.sonatype.plugins.portallocator.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

/**
 * @author Ross M. Lodge
 */
public class IncrementingAllocationStrategy extends AbstractAllocationStrategy {
	@Override
	protected int allocateNonDefaultPort(
		final int defaultPort, final PortAllocator allocator
	) throws PortUnavailableException, MojoExecutionException {
		for (int i = 1; i <= PortAllocator.MAX_PORT; i++) {
			int portNumber = defaultPort + i;
			if (allocator.allocatePort(portNumber)) {
				return portNumber;
			}
		}
		throw new PortUnavailableException(String.format("Could not find port between %s and %s", defaultPort, PortAllocator.MAX_PORT));
	}
}
