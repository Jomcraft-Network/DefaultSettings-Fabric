package de.pt400c.defaultsettings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class DefaultSettings implements ModInitializer {

	public static final String MODID = "defaultsettings";
	public static final Logger log = LogManager.getLogger(DefaultSettings.MODID);
	//public static final String VERSION = getModVersion();
	public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
	public static Map<String, KeyContainer> keyRebinds = new HashMap<String, KeyContainer>();
	public static String BUILD_ID = "Unknown";
	public static String BUILD_TIME = "Unknown";
	public static DefaultSettings instance;
	public static final boolean debug = false;
	public static boolean init = false;
	public static Class<?> alphaTest;
	public static int targetMS = 9;
	public static boolean compatibilityMode = false;
	
	@Override
    public void onInitialize()
    {
		instance = this;
		
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated) {
                CommandDefaultSettings.register(dispatcher);
            }
        });
        
        ClientLifecycleEvents.CLIENT_STARTED.register((test) -> {try {
			FileUtil.restoreKeys(true);
		} catch (IOException e) {
			DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
		} catch (NullPointerException e) {
			DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
		}}
        		
        		
        		
        		);
        /*
        
		//ClientStarted
		try {
			FileUtil.restoreKeys(true);
		} catch (IOException e) {
			DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
		} catch (NullPointerException e) {
			DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game (Post):", e);
		}*/
		
		(new Thread() {

			@Override
			public void run() {
				try {
					sendCount();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}).start();
    }

	public static DefaultSettings getInstance() {
		return instance;
	}
	
	public static void sendCount() throws Exception {
		String url = "https://apiv1.jomcraft.net/count";
		String jsonString = "{\"id\":\"Defaultsettings\", \"code\":" + RandomStringUtils.random(32, true, true) + "}"; 
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(jsonString);

		wr.flush();
		wr.close();
		con.getResponseCode();
		con.disconnect();

	}
}