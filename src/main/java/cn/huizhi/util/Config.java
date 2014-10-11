package cn.huizhi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

private static final Properties prop = new Properties();
	
	static {
		InputStream inStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			prop.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getValue(String key) {
		return prop.getProperty(key);
	}
	
	public static int getIntValue(String key) {
		Object obj = prop.get(key);
		if(obj != null) {
			return Integer.parseInt((String)obj);
		}
		return 0;
	}
	
}
