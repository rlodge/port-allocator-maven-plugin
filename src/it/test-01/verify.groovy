File propertiesFile = new File(new File(basedir, "target"), "app.properties");

Properties properties = new Properties();
properties.load(new FileInputStream(propertiesFile));

def ports = ["build.port",
             "build.apjPort",
             "build.stopPort",
             "build.cargoDebugPort",
             "karaf.rmi.server.port",
             "karaf.rmi.registry.port"];

for (String portKey: ports) {
	if (properties.getProperty(portKey, "0").equals("0")) {
		throw new RuntimeException("Could not find property " + portKey);
	}
}
