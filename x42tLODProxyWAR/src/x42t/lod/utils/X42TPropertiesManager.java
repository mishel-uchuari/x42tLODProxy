package x42t.lod.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author grozadilla
 *
 */
public class X42TPropertiesManager {

	final static Logger logger = Logger.getLogger(X42TPropertiesManager.class);
	private static X42TPropertiesManager INSTANCE = null;
	private Properties prop = null;

	/**
	 *  Get a PropertiesManager instance
	 * @return PropertiesManager instance
	 * @throws IOException
	 */
	public synchronized static X42TPropertiesManager getInstance() throws IOException {
		if (INSTANCE == null) {
			INSTANCE = new X42TPropertiesManager();
			INSTANCE.loadProperties();

		}
		return INSTANCE;
	}

	/**
	 * Loads properties from file
	 * @throws IOException
	 */
	public void loadProperties () throws IOException{
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream input = classLoader.getResourceAsStream("proxy.properties");
		prop = new Properties();
		prop.load(input);
		logger.info("Properties file loaded");
	}

	/**
	 * Gets a property value
	 * @param property the property key
	 * @return the property value
	 */
	public String getProperty(final String property){
		return prop.getProperty(property);
	}
}
