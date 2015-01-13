package org.sonatype.plugins.portallocator;

import org.sonatype.plugins.portallocator.strategy.AllocationStrategy;
import org.sonatype.plugins.portallocator.strategy.ConsistentRandomAllocationStrategy;
import org.sonatype.plugins.portallocator.strategy.DefaultAllocationStrategy;
import org.sonatype.plugins.portallocator.strategy.IncrementingAllocationStrategy;
import org.sonatype.plugins.portallocator.strategy.RandomAllocationStrategy;

/**
 * @author Ross M. Lodge
 */
public enum PortType {

	RANDOM(new RandomAllocationStrategy()),
	CONSISTENT(new ConsistentRandomAllocationStrategy()),
	INCREMENTING(new IncrementingAllocationStrategy()),
	DEFAULT(new DefaultAllocationStrategy());

	public final AllocationStrategy allocationStrategy;

	PortType(final AllocationStrategy allocationStrategy) {
		this.allocationStrategy = allocationStrategy;
	}

}
