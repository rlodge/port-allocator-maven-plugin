package org.sonatype.plugins.portallocator.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

/**
 * @author Ross M. Lodge
 */
public abstract class AbstractAllocationStrategy implements AllocationStrategy {

	public final int allocatePort(final int defaultPort, final PortAllocator allocator) throws PortUnavailableException, MojoExecutionException {
		if (allocator.allocatePort(defaultPort)) {
			return defaultPort;
		}
		return allocateNonDefaultPort(defaultPort, allocator);
	}

	protected abstract int allocateNonDefaultPort(final int defaultPort, final PortAllocator allocator) throws PortUnavailableException, MojoExecutionException;

}
