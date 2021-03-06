package de.pt400c.defaultsettings;

import org.apache.logging.log4j.Level;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunch implements PreLaunchEntrypoint {

	@Override
	public void onPreLaunch() {
		try {
			
			FileUtil.restoreContents();
			
		} catch (Exception e) {
			DefaultSettings.log.log(Level.ERROR, "An exception occurred while starting up the game:", e);
		}
		
	}

	
	
}