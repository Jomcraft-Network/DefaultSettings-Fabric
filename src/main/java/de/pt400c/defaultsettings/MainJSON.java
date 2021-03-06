package de.pt400c.defaultsettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.Level;

public class MainJSON {
	
	public static transient final long serialVersionUID = 32371L;
	private String version;
	private String prevVersion;
	protected boolean initPopup = false;
	private boolean exportMode = false;
	public String generatedBy = "<default>";
	//public List<String> activeConfigs = new ArrayList<String>();
	public HashMap<String, String> hashes = new HashMap<String, String>();
	public String mainProfile = "!NEW!";
	
	@SuppressWarnings("unused")
	private String initially_created;
	
	public MainJSON setVersion(String version) {
		this.version = version;
		return this;
	}
	
	public void setExportMode(boolean exportMode) {
		this.exportMode = exportMode;
	}
	
	public MainJSON setCreated(String initially_created) {
		this.initially_created = initially_created;
		return this;
	}
	
	public MainJSON setPrevVersion(String prevVersion) {
		this.prevVersion = prevVersion;
		return this;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public boolean getExportMode() {
		return this.exportMode;
	}

	public String getPrevVersion() {
		return this.prevVersion;
	}
	
	public void save() {
		try (FileWriter writer = new FileWriter(new File(FileUtil.mcDataDir, "config/defaultsettings.json"))) {
            FileUtil.gson.toJson(this, writer);
        } catch (IOException e) {
        	DefaultSettings.log.log(Level.ERROR, "Exception at processing configs: ", e);
        }
	}
}