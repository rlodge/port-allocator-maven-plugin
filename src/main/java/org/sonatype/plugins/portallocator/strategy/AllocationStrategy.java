package org.sonatype.plugins.portallocator.strategy;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.plugins.portallocator.PortAllocator;
import org.sonatype.plugins.portallocator.PortUnavailableException;

/**
 * @author Ross M. Lodge
 */
public interface AllocationStrategy {

	public int allocatePort(int defaultPort, PortAllocator allocator) throws PortUnavailableException, MojoExecutionException;

}
