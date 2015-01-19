package org.sonatype.plugins.portallocator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
			Set<InetAddress> addresses = new HashSet<InetAddress>();
			Collections.addAll(addresses, InetAddress.getAllByName("localhost"));
			Collections.addAll(addresses, InetAddress.getLocalHost());
			Collections.addAll(addresses, InetAddress.getAllByName(java.net.InetAddress.getLocalHost().getHostName()));
			Collections.addAll(addresses, InetAddress.getLoopbackAddress());
			Collections.addAll(addresses, InetAddress.getByName("0.0.0.0"));
			Collections.addAll(addresses, InetAddress.getByName("localhost"));
			Collections.addAll(addresses, InetAddress.getByName("::1"));
			Collections.addAll(addresses, InetAddress.getByName("::"));
			Boolean preferIPv4 = Boolean.valueOf(System.getProperty("java.net.preferIPv4Stack", "false"));
			for (InetAddress address : addresses) {
				if (address instanceof Inet4Address || !preferIPv4) {
					tryOnHost(portNumber, address);
				}
			}
		} catch (IOException e) {
			getLog().debug("Failed to connect to port " + portNumber, e);
			return false;
		}
		return true;
	}

	private boolean isPortShutdown(int port, int connectTimeout, InetAddress host) {
		Socket s = new Socket();
		try {
			getLog().debug(
				"\tConnection attempt with socket " + s + ", current time is "
					+ System.currentTimeMillis()
			);

			s.bind(null);

			// If the remote port is closed, s.connect will throw an exception
			s.connect(new InetSocketAddress(host, port), connectTimeout);
			getLog().debug(
				"\tSocket " + s + " for port " + port + " managed to connect"
			);

			try {
				s.shutdownOutput();
			} catch (IOException e) {
				// ignored, irrelevant
				getLog().debug(
					"\tFailed to shutdown output for socket " + s + ": " + e
				);
			}
			try {
				s.shutdownInput();
			} catch (IOException e) {
				// ignored, irrelevant
				getLog().debug(
					"\tFailed to shutdown input for socket " + s + ": " + e
				);
			}

			getLog().debug(
				"\tSocket " + s + " for port " + port + " shutdown"
			);
		} catch (IOException ignored) {
			// If an IOException has occured, this means port is shut down
			getLog().debug("\tFailed to connect " + ignored);
			return true;
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				// ignored, irrelevant
				getLog().debug(
					"\tFailed to close socket " + s + ": " + e
				);
			} finally {
				getLog().debug(
					"\tSocket " + s + " for port " + port + " closed"
				);

				s = null;
				System.gc();
			}
		}

		return false;
	}

	private void tryOnHost(final int portNumber, final InetAddress host) throws IOException, MojoExecutionException {
		final ServerSocket server;
		try {
			server = new ServerSocket(portNumber, 50, host);
		} catch (BindException e) {
			if ("Can't assign requested address".equals(e.getMessage())) {
				//Suppress this one and return
				return;
			}
			throw e;
		}
		try {
			server.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to release port " + portNumber, e);
		}
		if (!isPortShutdown(portNumber, 0, host)) {
			throw new IOException("Port not shutdown");
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
