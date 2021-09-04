package net.jomcraft.defaultsettings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

public class MainJSON {

	public static transient final long serialVersionUID = 32371L;
	private String version;
	private String prevVersion;
	protected boolean initPopup = false;
	public String generatedBy = "<default>";
	public HashMap<String, String> hashes = new HashMap<String, String>();
	public String mainProfile = "!NEW!";

	@SuppressWarnings("unused")
	private String initially_created;

	public MainJSON setVersion(String version) {
		this.version = version;
		return this;
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