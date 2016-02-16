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
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

	Set<InetAddress> addresses = new HashSet<InetAddress>();

	public void execute()
		throws MojoExecutionException, MojoFailureException {

		resolveAddresses();

		for (Port port : ports) {
			String name = port.getName();
			if (name == null) {
				getLog().warn("Port name not defined.  Skipping.");
				continue;
			}

			if (!project.getProperties().containsKey(name)) {
				Stopwatch stopwatch = new Stopwatch();
				int portNumber = port.allocatePort(this);
				getLog().info("Assigning port '" + portNumber + "' to property '" + name + "'; duration: " + stopwatch);
				project.getProperties().put(name, String.valueOf(portNumber));
			} else {
				getLog().warn("Property '" + name + "' already has value '" + project.getProperties().get(name) + "'");
			}
		}
	}

	private void resolveAddresses() {
		Stopwatch stopwatch = new Stopwatch();
		safelyAddHost("InetAddress.getLoopbackAddress()", addresses, new Callable<InetAddress[]>() {
			public InetAddress[] call() throws Exception {
				return new InetAddress[]{InetAddress.getLoopbackAddress()};
			}
		});
		safelyAddHost("InetAddress.getByName(\"::1\")", addresses, new Callable<InetAddress[]>() {
			public InetAddress[] call() throws Exception {
				return new InetAddress[]{InetAddress.getByName("::1")};
			}
		});
		safelyAddHost("InetAddress.getByName(\"127.0.0.1\")", addresses, new Callable<InetAddress[]>() {
			public InetAddress[] call() throws Exception {
				return new InetAddress[]{InetAddress.getByName("127.0.0.1")};
			}
		});

		safelyAddHost("NetworkInterface.getNetworkInterfaces()", addresses, new Callable<InetAddress[]>() {
			public InetAddress[] call() throws Exception {
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
				Set<InetAddress> addresses = new HashSet<InetAddress>();
				for (NetworkInterface netint : Collections.list(nets)) {
					addresses.addAll(Collections.list(netint.getInetAddresses()));
				}
				return addresses.toArray(new InetAddress[addresses.size()]);
			}
		});

		getLog().info("Resolved possible hosts in " + stopwatch + ": " + addresses);
	}

	public boolean allocatePort(final int portNumber) throws MojoExecutionException {
		try {

			Boolean preferIPv4 = Boolean.valueOf(System.getProperty("java.net.preferIPv4Stack", "false"));
			for (InetAddress address : addresses) {
				if (address instanceof Inet4Address || !preferIPv4) {
					Stopwatch stopwatch = new Stopwatch();
					getLog().debug("Trying " + portNumber + " on " + address);
					tryOnHost(portNumber, address);
					getLog().debug("Success for " + portNumber + " on " + address + " in " + stopwatch);
				} else {
					getLog().debug("Skipping testing " + portNumber + " on " + address + " because it's not an IPV4 address and we're preferring IPV4");
				}
			}
		} catch (IOException e) {
			getLog().debug("Failed to connect to port " + portNumber, e);
			return false;
		}
		return true;
	}

	private void safelyAddHost(
		final String name,
		final Set<InetAddress> addresses,
		final Callable<InetAddress[]> fn
	) {
		Stopwatch stopwatch = new Stopwatch();
		try {
			final InetAddress[] result = fn.call();
			Collections.addAll(addresses, result);
			getLog().debug("Resolved " + name + " to " + Arrays.toString(result) + " in " + stopwatch);
		} catch (Exception e) {
			//Makes no sense to do this.
			getLog().debug("Failed to resolve putative local address.  Ignoring. Duration: " + stopwatch, e);
		}
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
			getLog().debug("\tFailed to connect " + ignored + " so this port is open on this interface");
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
		if (!isPortShutdown(portNumber, 100, host)) {
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


	static class Stopwatch {
		long start = System.nanoTime();

		@Override
		public String toString() {
			return NumberFormat.getNumberInstance().format((double)(System.nanoTime() - start) / TimeUnit.SECONDS.toNanos(1)) + "s";
		}
	}
}
