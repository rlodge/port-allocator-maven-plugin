package org.sonatype.plugins.portallocator.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

import java.util.Random;

/**
 * @author Ross M. Lodge
 */
public class ConsistentRandomAllocationStrategy extends RandomAllocationStrategy {
	@Override
	protected int allocateNonDefaultPort(
		final int defaultPort, final PortAllocator allocator
	) throws PortUnavailableException, MojoExecutionException {
		int hash = allocator.getProject().getArtifactId().hashCode();
		int portNumber = defaultPort + new Random(hash).nextInt(100);
		if (allocator.allocatePort(portNumber)) {
			return portNumber;
		}
		return super.allocateNonDefaultPort(defaultPort, allocator);
	}
}
