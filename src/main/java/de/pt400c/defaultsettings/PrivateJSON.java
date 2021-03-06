package de.pt400c.defaultsettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.Level;

public class PrivateJSON {
	
	public static transient final long serialVersionUID = 498123L;
	public HashMap<String, String> currentHash = new HashMap<String, String>();
	public String targetProfile = "!NEW!";
	public String currentProfile = "!NEW!";
	public String privateIdentifier = null;
	public boolean compatibilityMode = false;
	
	public void save() {
		try (FileWriter writer = new FileWriter(new File(FileUtil.mcDataDir, "ds_private_storage.json"))) {
            FileUtil.gson.toJson(this, writer);
        } catch (IOException e) {
        	DefaultSettings.log.log(Level.ERROR, "Exception at processing startup: ", e);
        }
	}
}