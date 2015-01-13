package org.sonatype.plugins.portallocator.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

import java.util.Random;

/**
 * @author Ross M. Lodge
 */
public class RandomAllocationStrategy extends AbstractAllocationStrategy {

	private static final int MAX_TRIES = 10;

	private Random random = new Random();

	@Override
	protected int allocateNonDefaultPort(
		final int defaultPort, final PortAllocator allocator
	) throws PortUnavailableException, MojoExecutionException {
		for (int i = 0; i < MAX_TRIES; i++) {
			int portNumber = random.nextInt((PortAllocator.MAX_PORT - defaultPort)) + defaultPort;
			if (allocator.allocatePort(portNumber)) {
				return portNumber;
			}
		}
		throw new PortUnavailableException(String.format("Unable to randomly allocate port between %s and %s after %s tries.", defaultPort, PortAllocator.MAX_PORT, MAX_TRIES));
	}
}
