package org.sonatype.plugins.portallocator.strategy;

import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

/**
 * @author Ross M. Lodge
 */
public class DefaultAllocationStrategy extends AbstractAllocationStrategy {

	@Override
	protected int allocateNonDefaultPort(final int defaultPort, final PortAllocator allocator) throws PortUnavailableException {
		throw new PortUnavailableException(String.format("The requested port %s is not available and the DEFAULT strategy does not include a fallback.", defaultPort));
	}

}
