package org.sonatype.plugins.portallocator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Allocate ports to be used during build process
 *
 * @author velo
 * @goal allocate-ports
 */
public class PortAllocatorMojo
	extends AbstractMojo
	implements PortAllocator {

	/**
	 * Define which ports should be allocated by the plugin
	 *
	 * @parameter
	 * @required
	 */
	private Port[] ports;

	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	public void execute()
		throws MojoExecutionException, MojoFailureException {
		for (Port port : ports) {
			String name = port.getName();
			if (name == null) {
				getLog().warn("Port name not defined.  Skipping.");
				continue;
			}

			if (!project.getProperties().containsKey(name)) {
				int portNumber = port.allocatePort(this);
				getLog().info("Assigning port '" + portNumber + "' to property '" + name + "'");
				project.getProperties().put(name, String.valueOf(portNumber));
			} else {
				getLog().warn("Property '"  + name + "' already has value '" + project.getProperties().get(name) + "'");
			}
		}
	}

	public boolean allocatePort(final int portNumber) throws MojoExecutionException {
		try {
			tryOnHost(portNumber, InetAddress.getLocalHost());
			tryOnHost(portNumber, InetAddress.getLoopbackAddress());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private void tryOnHost(final int portNumber, final InetAddress host) throws IOException, MojoExecutionException {
		final ServerSocket server;
		server = new ServerSocket(portNumber, 50, host);
		try {
			server.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to release port " + portNumber, e);
		}
	}

	public MavenProject getProject() {
		return project;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setPorts(Port[] ports) {
		this.ports = ports;
	}

}
