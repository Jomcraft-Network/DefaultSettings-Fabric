package net.jomcraft.defaultsettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.Level;
import org.quiltmc.loader.api.QuiltLoader;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.InputUtil;
import net.jomcraft.defaultsettings.mixin.DefaultSettingsMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBind;

public class FileUtil {

	public static final File mcDataDir = QuiltLoader.getGameDir().toFile();
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static MainJSON mainJson;
	public static PrivateJSON privateJson;
	public static IgnoreJSON ignoreJson;
	public volatile static Thread registryChecker;
	public volatile static boolean options_exists = false;
	public volatile static boolean keys_exists = false;
	public static ArrayList<String> deleted = new ArrayList<String>();
	public volatile static boolean servers_exists = false;
	public static String activeProfile = "Default";
	public static boolean otherCreator = false;
	public static boolean firstBootUp;
	public static final FileFilter fileFilterModular = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("sharedConfigs") && !file.getName().equals("ignore.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("servers.dat") /*																																																																																																										 *//* ? true : getMainJSON().activeConfigs.contains(file.getName()) *//* ) */)
				return true;

			return false;
		}
	};

	public static final FileFilter fileFilterAnti = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("sharedConfigs") && !file.getName().equals("ignore.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("servers.dat")/* && !getMainJSON().activeConfigs.contains(file.getName()) */)
				return true;

			return false;
		}
	};

	public static final FileFilter fileFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {

			if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("servers.dat") && !new File(FileUtil.getMainFolder(), "sharedConfigs/" + file.getName()).exists())
				return true;

			return false;
		}
	};

	public static File getMainFolder() {
		final File storeFolder = new File(mcDataDir, "config/defaultsettings");
		storeFolder.mkdir();
		return storeFolder;
	}

	public static void switchState(Byte state, String query) {

		@SuppressWarnings("unused")
		FileFilter ff = null;
		if (!query.isEmpty()) {
			ff = new FileFilter() {

				@Override
				public boolean accept(File file) {

					if (!file.getName().equals("defaultsettings") && !file.getName().equals("defaultsettings.json") && !file.getName().equals("ds_dont_export.json") && !file.getName().equals("keys.txt") && !file.getName().equals("options.txt") && !file.getName().equals("optionsof.txt") && !file.getName().equals("optionsshaders.txt") && !file.getName().equals("servers.dat") && !new File(FileUtil.getMainFolder(), "sharedConfigs/" + file.getName()).exists() && file.getName().toLowerCase().startsWith(query.toLowerCase()))
						return true;

					return false;
				}
			};
		} else {
			ff = FileUtil.fileFilter;
		}

		if (state == 1 || state == 2) {
			// List<String> list = new ArrayList<String>(mainJson.activeConfigs);
			// mainJson.activeConfigs.stream().filter(file ->
			// file.toLowerCase().startsWith(query.toLowerCase()) && new File(mcDataDir +
			// "/config", file).exists()).forEach(file -> removeFromLists(list, file));
			// mainJson.activeConfigs = list;

		} else if (state == 0) {
			// File fileDir = new File(mcDataDir, "config");
			// for (File file : fileDir.listFiles(ff))
			// getMainJSON().activeConfigs.add(file.getName());

		}

		mainJson.save();
	}

	public static void setActive(String name, boolean active) {
		// if (!active)
		// mainJson.activeConfigs.remove(name);
		// else if (!mainJson.activeConfigs.contains(name))
		// mainJson.activeConfigs.add(name);

		mainJson.save();
	}

	public static void switchActive(String name) {
		// if (mainJson.activeConfigs.contains(name))
		// mainJson.activeConfigs.remove(name);
		// else
		// mainJson.activeConfigs.add(name);

		mainJson.save();
	}

	public static void initialSetupJSON() throws UnknownHostException, SocketException, NoSuchAlgorithmException {

		getPrivateJSON();

		final String version = getMainJSON().getVersion();

		if (!DefaultSettings.VERSION.equals(version))
			mainJson.setVersion(DefaultSettings.VERSION).setPrevVersion(version);

		if (!privateJson.privateIdentifier.equals(mainJson.generatedBy) && !mainJson.generatedBy.equals("<default>")) {
			otherCreator = true;
		}

		mainJson.save();
	}

	public static IgnoreJSON getSharedIgnore(File location) {

		if (ignoreJson != null)
			return ignoreJson;

		if (location.exists()) {
			try (Reader reader = new FileReader(location)) {
				ignoreJson = gson.fromJson(reader, IgnoreJSON.class);
				ignoreJson.location = location;

			} catch (Exception e) {
				DefaultSettings.log.log(Level.ERROR, "Exception at processing startup: ", e);
			}

		} else {

			ignoreJson = new IgnoreJSON(location);
			ignoreJson.save();
		}
		return ignoreJson;
	}

	public static PrivateJSON getPrivateJSON() {

		if (privateJson != null)
			return privateJson;
		final File privateFile = new File(mcDataDir, "ds_private_storage.json");
		if (privateFile.exists()) {
			try (Reader reader = new FileReader(privateFile)) {
				privateJson = gson.fromJson(reader, PrivateJSON.class);

				if (privateJson.privateIdentifier == null || privateJson.privateIdentifier.isEmpty())
					privateJson.privateIdentifier = UUID.randomUUID().toString();

				privateJson.save();

			} catch (Exception e) {
				DefaultSettings.log.log(Level.ERROR, "Exception at processing startup: ", e);
			}

		} else {

			privateJson = new PrivateJSON();
			privateJson.privateIdentifier = UUID.randomUUID().toString();
			privateJson.save();
		}
		return privateJson;
	}

	public static MainJSON getMainJSON() {

		if (mainJson != null)
			return mainJson;

		File mainFile = new File(mcDataDir, "config/defaultsettings.json");

		if (mainFile.exists()) {
			try (Reader reader = new FileReader(mainFile)) {
				mainJson = gson.fromJson(reader, MainJSON.class);

			} catch (Exception e) {
				DefaultSettings.log.log(Level.ERROR, "Exception at processing configs: ", e);
				if (e instanceof JsonSyntaxException) {
					mainFile.renameTo(new File(mcDataDir, "config/defaultsettings_malformed.json"));
					getMainJSON();
				}

			}

		} else {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

			mainJson = new MainJSON().setVersion("3.0.2").setCreated(formatter.format(date));

			mainJson.initPopup = true;
			// File fileDir = new File(mcDataDir, "config");
			// for (File file : fileDir.listFiles(fileFilter))
			// mainJson.activeConfigs.add(file.getName());

			mainJson.save();
		}
		return mainJson;
	}

	public static void setPopup(boolean active) {
		mainJson.initPopup = active;
		mainJson.save();
	}

	public static void restoreContents() throws NullPointerException, IOException, NoSuchAlgorithmException {

		initialSetupJSON();

		initialToDefaultProfile();

		String firstFolder = "<ERROR>";

		for (File file : getMainFolder().listFiles()) {
			if (file.isDirectory() && !file.getName().equals("sharedConfigs")) {
				firstFolder = file.getName();
				break;
			}
		}

		if (!new File(getMainFolder(), mainJson.mainProfile).exists())
			mainJson.mainProfile = firstFolder;

		if (privateJson.targetProfile.equals("!NEW!"))
			privateJson.targetProfile = mainJson.mainProfile;

		if (privateJson.currentProfile.equals("!NEW!"))
			privateJson.currentProfile = mainJson.mainProfile;

		if (!new File(getMainFolder(), privateJson.targetProfile).exists())
			privateJson.targetProfile = firstFolder;

		if (!new File(getMainFolder(), privateJson.currentProfile).exists())
			privateJson.currentProfile = firstFolder;

		privateJson.save();

		mainJson.save();

		boolean switchProf = switchProfile();

		activeProfile = privateJson.currentProfile;

		final File options = new File(mcDataDir, "options.txt");
		firstBootUp = !options.exists();
		if (firstBootUp) {
			restoreOptions();

			restoreConfigs();
		} else if (switchProf) {
			restoreConfigs();
			mainJson.save();
		} else {

			copyAndHashPrivate(true, true);
			mainJson.save();

		}

		final File optionsOF = new File(mcDataDir, "optionsof.txt");
		if (!optionsOF.exists())
			restoreOptionsOF();

		final File optionsShaders = new File(mcDataDir, "optionsshaders.txt");
		if (!optionsShaders.exists())
			restoreOptionsShaders();

		final File serversFile = new File(mcDataDir, "servers.dat");
		if (!serversFile.exists())
			restoreServers();

		if (!options.exists())
			options.createNewFile();

	}

	private static boolean switchProfile() throws IOException {
		if (!privateJson.currentProfile.equals(privateJson.targetProfile)) {

			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

			String profileName = formatter.format(date);

			File fileDir = new File(FileUtil.getMainFolder(), profileName);
			fileDir.mkdir();

			activeProfile = profileName;

			FileUtil.moveAllConfigs();
			FileUtil.checkMD5(true, false, null); // TODO: This second "false" is a place holder

			String[] extensions = new String[] { "zip" };
			List<Path> oldestFiles = Collections.emptyList();

			Collection<File> files = FileUtils.listFiles(getMainFolder(), extensions, false);

			if (files.size() >= 10) {

				final List<Path> list2 = files.stream().map(File::toPath).collect(Collectors.toList());
				Comparator<? super Path> lastModifiedComparator = (p1, p2) -> Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified());
				try (Stream<Path> paths = list2.stream()) {
					oldestFiles = paths.filter(Files::isRegularFile).sorted(lastModifiedComparator).limit(files.size() - 8).collect(Collectors.toList());
					oldestFiles.stream().forEach(t -> {
						try {
							Files.delete(t);
						} catch (IOException e) {
							DefaultSettings.log.log(Level.ERROR, "Exception while processing profiles: ", e);
						}
					});
				}

			}

			Path pf = new File(FileUtil.getMainFolder(), profileName + ".zip").toPath();
			Files.createFile(pf);
			try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(pf))) {
				Path pt = Paths.get(fileDir.getPath());
				Files.walk(pt).filter(path -> !Files.isDirectory(path)).forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pt.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
						DefaultSettings.log.log(Level.ERROR, "Exception while processing profiles: ", e);
					}
				});
			}

			try {
				deleted.add(fileDir.getName());

				FileUtils.deleteDirectory(fileDir);

			} catch (IOException e) {
				Thread thread = new Thread("File deletion thread") {
					public void run() {
						try {
							Thread.sleep(10000);
							FileUtils.deleteDirectory(fileDir);
						} catch (InterruptedException | IOException e) {

						}

					}
				};
				thread.start();
				try {
					FileUtils.forceDeleteOnExit(fileDir);
				} catch (IOException e1) {

				}

			}

			activeProfile = privateJson.targetProfile;
			privateJson.currentProfile = activeProfile;
			privateJson.save();

			return true;

		}
		return false;

	}

	private static void initialToDefaultProfile() {
		if (getMainJSON().mainProfile.equals("!NEW!")) {

			new File(getMainFolder(), "Default").mkdir();

			FileFilter ffm = new FileFilter() {

				@Override
				public boolean accept(File file) {
					if (!file.getName().equals("Default"))
						return true;

					return false;
				}
			};

			try {
				FileUtils.copyDirectory(getMainFolder(), new File(getMainFolder(), "Default"), ffm);
			} catch (IOException e) {
				DefaultSettings.log.log(Level.ERROR, "Couldn't move config files: ", e);
			}

			for (File f : getMainFolder().listFiles(ffm)) {
				try {
					if (f.isDirectory())
						FileUtils.deleteDirectory(f);
					else
						Files.delete(f.toPath());
				} catch (IOException e) {
					DefaultSettings.log.log(Level.ERROR, "Couldn't move config files: ", e);
				}
			}

			privateJson.targetProfile = "Default";

			privateJson.save();

			getMainJSON().mainProfile = "Default";

			mainJson.save();

		}
		String firstFolder = "<ERROR>";
		for (File file : getMainFolder().listFiles()) {
			if (file.isDirectory() && !file.getName().equals("sharedConfigs")) {
				firstFolder = file.getName();
				break;
			}
		}

		if (firstFolder.equals("<ERROR>")) {
			new File(getMainFolder(), "Default").mkdir();

			privateJson.targetProfile = "Default";

			privateJson.save();

			getMainJSON().mainProfile = "Default";

			mainJson.save();
		}

		File shared = new File(getMainFolder(), "sharedConfigs");
		shared.mkdir();
		getSharedIgnore(new File(shared, "ignore.json"));
	}

	public static void copyAndHashPrivate(boolean options, boolean configs) throws NullPointerException, IOException {
		ArrayList<String> toRemove = new ArrayList<String>();

		if (options) {

			for (String opt : optUse) {
				File optFile = new File(getMainFolder(), activeProfile + "/" + opt);
				if (optFile.exists()) {
					if (!privateJson.currentHash.containsKey(activeProfile + "/" + opt) || (mainJson.hashes.containsKey(activeProfile + "/" + opt) && !privateJson.currentHash.get(activeProfile + "/" + opt).equals(mainJson.hashes.get(activeProfile + "/" + opt)))) {

						if (opt.equals("options.txt")) {
							restoreOptions();
						} else if (opt.equals("keys.txt")) {
							restoreKeys(false, false);
						} else if (opt.equals("optionsof.txt")) {
							restoreOptionsOF();
						} else if (opt.equals("optionsshaders.txt")) {
							restoreOptionsShaders();
						} else if (opt.equals("servers.dat")) {
							restoreServers();
						}
						privateJson.currentHash.put(activeProfile + "/" + opt, mainJson.hashes.get(activeProfile + "/" + opt));

					}
				}
			}
		}

		if (configs) {

			File filec = new File(mcDataDir, "config");
			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!configFile.isDirectory() && !configFile.getName().contains("defaultsettings") && !configFile.getName().equals("ignore.json") && !optUse.contains(configFile.getName())) {
					String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
					String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);

					if (!privateJson.currentHash.containsKey(pathString) || (mainJson.hashes.containsKey(pathString) && !privateJson.currentHash.get(pathString).equals(mainJson.hashes.get(pathString)))) {

						if (mainJson.hashes.containsKey(pathString)) {
							FileUtils.copyFile(configFile, new File(filec, relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1)));
							privateJson.currentHash.put(pathString, mainJson.hashes.get(pathString));
						}

					}

				}
			}
		}
		/*
		 * Collection<File> shared = FileUtils.listFilesAndDirs(new
		 * File(getMainFolder(), "sharedConfigs/"), TrueFileFilter.INSTANCE,
		 * TrueFileFilter.INSTANCE); for (File sharedFile : shared) {
		 * 
		 * if (sharedFile.getName().equals("ignore.json")) continue; File file = new
		 * File(mcDataDir, "config"); String name = sharedFile.getName(); File fileInner
		 * = new File(file, sharedFile.getName()); try {
		 * 
		 * File locInDir = new File(getMainFolder(), "sharedConfigs/" + name); if
		 * (locInDir.isDirectory()) {
		 * 
		 * Collection<File> files = FileUtils.listFilesAndDirs(locInDir,
		 * TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE); for (File filePers :
		 * files) {
		 * 
		 * if (filePers.isDirectory()) continue;
		 * 
		 * String relativePath =
		 * filePers.getPath().substring((mcDataDir.getPath().length()));
		 * 
		 * String loc =
		 * relativePath.split("defaultsettings")[1].substring(1).split("sharedConfigs")[
		 * 1].substring(1);
		 * 
		 * File configLoc = new File(file, loc);
		 * 
		 * File newF = new File(getMainFolder(), "sharedConfigs/" + loc); if
		 * ((!configLoc.exists() || !privateJson.currentHash.
		 * containsKey("sharedConfigs\\" + loc) || !privateJson.currentHash.get("
		 * sharedConfigs\\" + loc).equals(mainJson.hashes.get("sharedConfigs\\" + loc)))
		 * && newF.exists()) { FileUtils.copyFile(newF, configLoc);
		 * privateJson.currentHash.put("sharedConfigs\\" + loc, mainJson.hashes.get("
		 * sharedConfigs\\" + loc));
		 * 
		 * } }
		 * 
		 * } else { if ((!fileInner.exists() || !privateJson.currentHash.
		 * containsKey("sharedConfigs\\" + name) || !privateJson.currentHash.get("
		 * sharedConfigs\\" + name).equals(mainJson.hashes.get("sharedConfigs\\" +
		 * name))) && locInDir.exists()) {
		 * 
		 * FileUtils.copyFile(locInDir, fileInner);
		 * 
		 * privateJson.currentHash.put("sharedConfigs\\" + name, mainJson.hashes.get("
		 * sharedConfigs\\" + name));
		 * 
		 * } } } catch (IOException e) { if (e instanceof FileNotFoundException) {
		 * DefaultSettings.log.log(Level.DEBUG, "The file no longer exists: ", e); }
		 * else { DefaultSettings.log.log(Level.WARN, "Error while creating hash: ", e);
		 * } } }
		 */

		/*
		 * 
		 * for(String name : mainJson.activeConfigs) { File file = new File(mcDataDir,
		 * "config"); File fileInner = new File(file, name); try {
		 * 
		 * File locInDir = new File(getMainFolder(), activeProfile + "/" + name);
		 * 
		 * if(locInDir.isDirectory()) {
		 * 
		 * Collection<File> files = FileUtils.listFilesAndDirs(locInDir,
		 * TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE); for(File filePers : files)
		 * {
		 * 
		 * if(filePers.isDirectory()) continue;
		 * 
		 * String relativePath =
		 * filePers.getPath().substring((mcDataDir.getPath().length()));
		 * 
		 * 
		 * String loc =
		 * relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1]
		 * .substring(1);
		 * 
		 * File configLoc = new File(file, loc);
		 * 
		 * File newF = new File(getMainFolder(), activeProfile + "/" + loc);
		 * if((!configLoc.exists() || !privateJson.currentHash.containsKey(activeProfile
		 * + "/" + loc) || !privateJson.currentHash.get(activeProfile + "/" +
		 * loc).equals(mainJson.hashes.get(activeProfile + "/" + loc))) &&
		 * newF.exists()) { FileUtils.copyFile(newF, configLoc);
		 * 
		 * privateJson.currentHash.put(activeProfile + "/" + loc,
		 * mainJson.hashes.get(activeProfile + "/" + loc));
		 * 
		 * } }
		 * 
		 * }else {
		 * 
		 * if((!fileInner.exists() || !privateJson.currentHash.containsKey(activeProfile
		 * + "/" + name) || !privateJson.currentHash.get(activeProfile + "/" +
		 * name).equals(mainJson.hashes.get(activeProfile + "/" + name))) &&
		 * locInDir.exists()) {
		 * 
		 * FileUtils.copyFile(locInDir, fileInner);
		 * 
		 * privateJson.currentHash.put(activeProfile + "/" + name,
		 * mainJson.hashes.get(activeProfile + "/" + name));
		 * 
		 * } } }catch (IOException e) { if(e instanceof FileNotFoundException) {
		 * DefaultSettings.log.log(Level.DEBUG, "The file no longer exists: ", e);
		 * if(!otherCreator) toRemove.add(name); }else {
		 * DefaultSettings.log.log(Level.WARN, "Error while creating hash: ", e); } } }
		 * 
		 * for(String remove : toRemove) { mainJson.activeConfigs.remove(remove); }
		 */
		privateJson.save();

		if (toRemove.size() > 0) {
			mainJson.save();
		}
	}

	public static boolean optionsFilesExist() {
		final File optionsFile = new File(getMainFolder(), activeProfile + "/options.txt");
		final File optionsofFile = new File(getMainFolder(), activeProfile + "/optionsof.txt");
		final File optionsShadersFile = new File(getMainFolder(), activeProfile + "/optionsshaders.txt");
		return optionsFile.exists() || optionsofFile.exists() || optionsShadersFile.exists();
	}

	public static boolean keysFileExist() {
		final File keysFile = new File(getMainFolder(), activeProfile + "/keys.txt");
		return keysFile.exists();
	}

	public static void deleteKeys() {
		new File(getMainFolder(), activeProfile + "/keys.txt").delete();
		FileUtil.keys_exists = false;
	}

	public static void deleteServers() {
		new File(getMainFolder(), activeProfile + "/servers.dat").delete();
		FileUtil.servers_exists = false;
	}

	public static void deleteOptions() {
		new File(getMainFolder(), activeProfile + "/options.txt").delete();
		new File(getMainFolder(), activeProfile + "/optionsof.txt").delete();
		new File(getMainFolder(), activeProfile + "/optionsshaders.txt").delete();
		FileUtil.options_exists = false;
	}

	public static void restoreOptions() throws NullPointerException, IOException {
		final File optionsFile = new File(getMainFolder(), activeProfile + "/options.txt");
		if (optionsFile.exists()) {
			BufferedReader readerOptions = null;
			BufferedReader reader = null;
			PrintWriter writer = null;
			File opFile = new File(mcDataDir, "options.txt");
			boolean existed = false;
			ArrayList<String> list = new ArrayList<String>();
			try {
				reader = new BufferedReader(new FileReader(optionsFile));

				if (opFile.exists()) {
					existed = true;
					readerOptions = new BufferedReader(new FileReader(opFile));

					String lineOptions;
					while ((lineOptions = readerOptions.readLine()) != null) {
						if (lineOptions.startsWith("key_"))
							list.add(lineOptions);
					}
				}

				writer = new PrintWriter(new FileWriter(new File(mcDataDir, "options.txt")));

				String line;
				while ((line = reader.readLine()) != null)
					writer.print(line + "\n");

				for (String entry : list)
					writer.print(entry + "\n");

			} catch (IOException e) {
				throw e;
			} finally {
				try {
					reader.close();
					writer.close();
					if (existed)
						readerOptions.close();
				} catch (IOException e) {
					throw e;
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}
	}

	@SuppressWarnings("resource")
	public static void restoreKeys(boolean update, boolean initial) throws NullPointerException, IOException, NumberFormatException {

		DefaultSettings.keyRebinds.clear();
		final File keysFile = new File(getMainFolder(), activeProfile + "/keys.txt");
		if (keysFile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(keysFile));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty())
						continue;

					DefaultSettings.keyRebinds.put(line.split(":")[0], new KeyContainer(InputUtil.fromTranslationKey(line.split(":")[1]), null));
				}
			} catch (IOException e) {
				throw e;

			} catch (NullPointerException e) {
				throw e;
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					throw e;
				} catch (NullPointerException e) {
					throw e;
				}
			}

			if (update) {

				ArrayList<String> presentKeys = new ArrayList<String>();

				final File localKeysFile = new File(mcDataDir, "options.txt");
				if (localKeysFile.exists()) {
					BufferedReader localReader = null;
					try {
						localReader = new BufferedReader(new FileReader(localKeysFile));
						String line;
						while ((line = localReader.readLine()) != null) {
							if (line.isEmpty())
								continue;

							if (line.startsWith("key_key.")) {
								final String key = line.split("key_")[1].split(":")[0];
								presentKeys.add(key);
							}
						}
					} catch (IOException e) {
						throw e;

					} catch (NullPointerException e) {
						throw e;
					} finally {
						try {
							localReader.close();
						} catch (IOException e) {
							throw e;
						} catch (NullPointerException e) {
							throw e;
						}
					}
				}

				for (KeyBind keyBinding : MinecraftClient.getInstance().options.allKeys) {
					if (DefaultSettings.keyRebinds.containsKey(keyBinding.getTranslationKey())) {
						KeyContainer container = DefaultSettings.keyRebinds.get(keyBinding.getTranslationKey());

						if (initial || !presentKeys.contains(keyBinding.getTranslationKey()))
							keyBinding.boundKey = container.input;

						((DefaultSettingsMixin) keyBinding).setDefaultKey(container.input);

					}
				}

			}

		}
	}

	public static void restoreOptionsOF() throws IOException {
		final File optionsOFFile = new File(getMainFolder(), activeProfile + "/optionsof.txt");
		if (optionsOFFile.exists()) {
			BufferedReader reader = null;
			PrintWriter writer = null;
			try {
				reader = new BufferedReader(new FileReader(optionsOFFile));
				writer = new PrintWriter(new FileWriter(new File(mcDataDir, "optionsof.txt")));
				String line;
				while ((line = reader.readLine()) != null) {
					writer.print(line + "\n");
				}
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			} finally {
				try {
					reader.close();
					writer.close();
				} catch (IOException e) {
					throw e;
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}
	}

	public static void restoreOptionsShaders() throws IOException {
		final File optionsShadersFile = new File(getMainFolder(), activeProfile + "/optionsshaders.txt");
		if (optionsShadersFile.exists()) {
			BufferedReader reader = null;
			PrintWriter writer = null;
			try {
				reader = new BufferedReader(new FileReader(optionsShadersFile));
				writer = new PrintWriter(new FileWriter(new File(mcDataDir, "optionsshaders.txt")));
				String line;
				while ((line = reader.readLine()) != null) {
					writer.print(line + "\n");
				}
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			} finally {
				try {
					reader.close();
					writer.close();
				} catch (IOException e) {
					throw e;
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}
	}

	public static void restoreConfigs() throws IOException {
		try {
			FileUtils.copyDirectory(new File(getMainFolder(), activeProfile), new File(mcDataDir, "config"), fileFilterModular);
		} catch (IOException e) {
			throw e;
		}

		optUse.stream().map(file -> new File(getMainFolder(), activeProfile + "/" + file)).filter(file -> file.exists()).forEach(file -> {
			try {
				FileUtils.copyFile(file, new File(mcDataDir, file.getName()));
			} catch (IOException e) {
				DefaultSettings.log.log(Level.ERROR, "Process the files: ", e);
			}
		});

		try {
			FileUtils.copyDirectory(new File(getMainFolder(), "sharedConfigs/"), new File(mcDataDir, "config"), fileFilterModular);
		} catch (IOException e) {
			throw e;
		}

		FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).stream().filter(file -> !file.isDirectory()).forEach(file -> {
			try {
				privateJson.currentHash.put(activeProfile + "/" + file.getName(), fileToHash(new FileInputStream(file)));
			} catch (IOException e) {
				DefaultSettings.log.log(Level.ERROR, "Process the files: ", e);
			}
		});

		mainJson.save();
		privateJson.save();
	}

	public static void moveAllConfigs() throws IOException {
		mainJson.generatedBy = privateJson.privateIdentifier;

		try {

			/*
			 * FileUtils.copyDirectory(fileDir, new File(getMainFolder(), activeProfile),
			 * fileFilterModular); for (File f : fileDir.listFiles(fileFilterModular)) { try
			 * { if(f.isDirectory()) FileUtils.deleteDirectory(f); else //f.delete() calls
			 * updates, not appropriate Files.delete(f.toPath()); }catch(IOException e) {
			 * DefaultSettings.log.log(Level.ERROR, "Couldn't move config files: ", e); } }
			 * for(File file : new File(getMainFolder(), "sharedConfigs").listFiles()) {
			 * 
			 * if(!new File(fileDir, file.getName()).exists()) continue; if(new
			 * File(fileDir, file.getName()).isDirectory()) FileUtils.deleteDirectory(new
			 * File(fileDir, file.getName())); else //f.delete() calls updates, not
			 * appropriate Files.delete(new File(fileDir, file.getName()).toPath()); }
			 */

			FileUtils.copyDirectory(new File(getMainFolder(), activeProfile), new File(mcDataDir, "config"), fileFilterAnti);
			/*
			 * for (File f : new File(getMainFolder(),
			 * activeProfile).listFiles(fileFilterAnti)) { try { if(f.isDirectory())
			 * FileUtils.deleteDirectory(f); else //f.delete() calls updates, not
			 * appropriate Files.delete(f.toPath()); }catch(IOException e) {
			 * DefaultSettings.log.log(Level.ERROR, "Couldn't move config files: ", e); } }
			 */

		} catch (IOException e) {
			throw e;
		}

		mainJson.save();
	}

	public static void restoreServers() throws IOException {
		try {
			File file = new File(getMainFolder(), activeProfile + "/servers.dat");
			if (file.exists())
				FileUtils.copyFile(file, new File(mcDataDir, "servers.dat"));
			else
				DefaultSettings.log.log(Level.WARN, "Couldn't restore the server config as it's not included");
		} catch (IOException e) {
			DefaultSettings.log.log(Level.ERROR, "Couldn't restore the server config: ", e);
		}
	}

	@SuppressWarnings("resource")
	public static void saveKeys() throws IOException, NullPointerException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(getMainFolder(), activeProfile + "/keys.txt")));
			for (KeyBind keyBinding : MinecraftClient.getInstance().options.allKeys)
				writer.print(keyBinding.getTranslationKey() + ":" + keyBinding.boundKey.toString() + "\n");

		} catch (IOException e) {
			throw e;
		} catch (NullPointerException e) {
			throw e;
		} finally {
			writer.close();
		}
	}

	@SuppressWarnings("resource")
	public static boolean saveOptions() throws NullPointerException, IOException {
		MinecraftClient.getInstance().options.write();
		PrintWriter writer = null;
		BufferedReader reader = null;
		try {
			writer = new PrintWriter(new FileWriter(new File(getMainFolder(), activeProfile + "/options.txt")));
			reader = new BufferedReader(new FileReader(new File(mcDataDir, "options.txt")));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("key_"))
					continue;

				writer.print(line + "\n");
			}
		} catch (IOException e) {
			throw e;
		} catch (NullPointerException e) {
			throw e;
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			}
		}

		if (!new File(mcDataDir, "optionsof.txt").exists()) {
			return false;
		}

		try {
			writer = new PrintWriter(new FileWriter(new File(getMainFolder(), activeProfile + "/optionsof.txt")));
			reader = new BufferedReader(new FileReader(new File(mcDataDir, "optionsof.txt")));
			String line;
			while ((line = reader.readLine()) != null)
				writer.print(line + "\n");

		} catch (IOException e) {
			throw e;
		} catch (NullPointerException e) {
			throw e;
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			}
		}

		if (!new File(mcDataDir, "optionsshaders.txt").exists()) {
			return false;
		}

		try {
			writer = new PrintWriter(new FileWriter(new File(getMainFolder(), activeProfile + "/optionsshaders.txt")));
			reader = new BufferedReader(new FileReader(new File(mcDataDir, "optionsshaders.txt")));
			String line;
			while ((line = reader.readLine()) != null)
				writer.print(line + "\n");

		} catch (IOException e) {
			throw e;
		} catch (NullPointerException e) {
			throw e;
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			}
		}

		return true;
	}

	public static void saveServers() throws IOException {
		final File serversFile = new File(mcDataDir, "servers.dat");
		if (serversFile.exists()) {
			try {
				FileUtils.copyFile(serversFile, new File(getMainFolder(), activeProfile + "/servers.dat"));
			} catch (IOException e) {
				throw e;
			}
		}
	}

	public static InputStream getServersStream() throws IOException {
		final File serversFile = new File(mcDataDir, "servers.dat");
		if (serversFile.exists()) {
			return new FileInputStream(serversFile);
		}
		return null;
	}

	public static InputStream getOptionsOFStream() throws IOException {
		final File optionsFile = new File(mcDataDir, "optionsof.txt");
		if (optionsFile.exists()) {
			return new FileInputStream(optionsFile);
		}
		return null;
	}

	public static InputStream getOptionsShadersStream() throws IOException {
		final File optionsFile = new File(mcDataDir, "optionsof.txt");
		if (optionsFile.exists()) {
			return new FileInputStream(optionsFile);
		}
		return null;
	}

	@SuppressWarnings("resource")
	public static InputStream getKeysStream() throws IOException, NullPointerException {
		FileInputStream stream = null;
		PrintWriter writer = null;
		File file = new File(getMainFolder(), activeProfile + "/keys.txt_temp");
		try {
			writer = new PrintWriter(new FileWriter(file));
			for (KeyBind keyBinding : MinecraftClient.getInstance().options.allKeys)
				writer.print(keyBinding.getTranslationKey() + ":" + keyBinding.boundKey.toString() + "\n");
			stream = new FileInputStream(file);
		} catch (IOException e) {
			throw e;
		} catch (NullPointerException e) {
			throw e;
		} finally {
			writer.close();
		}

		return stream;
	}

	public static InputStream getOptionsStream() throws IOException, NullPointerException {
		final File keysFile = new File(mcDataDir, "options.txt");
		FileInputStream stream = null;
		if (keysFile.exists()) {
			BufferedReader reader = null;
			PrintWriter writer = null;
			File file = new File(getMainFolder(), activeProfile + "/options.txt_temp");
			try {
				writer = new PrintWriter(new FileWriter(file));
				reader = new BufferedReader(new FileReader(keysFile));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("key_"))
						continue;
					writer.print(line + "\n");

				}
				stream = new FileInputStream(file);
			} catch (IOException e) {
				throw e;
			} catch (NullPointerException e) {
				throw e;
			} finally {
				try {
					reader.close();
					writer.close();

				} catch (IOException e) {
					throw e;
				} catch (NullPointerException e) {
					throw e;
				}
			}
			return stream;
		}
		return null;
	}

	public static boolean serversFileExists() {
		final File serversFile = new File(getMainFolder(), activeProfile + "/servers.dat");
		return serversFile.exists();
	}

	public static String getUUID(String uuid) throws NoSuchAlgorithmException {
		return stringToHash(uuid);
	}

	public static String stringToHash(String string) throws NoSuchAlgorithmException {
		return DigestUtils.md5Hex(string).toUpperCase();
	}

	public static String byteToHash(byte[] array) throws NoSuchAlgorithmException {
		return Hashing.sha256().hashBytes(array).toString();
	}

	public static String fileToHash(InputStream is) throws IOException {
		return DigestUtils.md5Hex(is).toUpperCase();
	}

	public static boolean checkForConfigFiles() {
		try {

			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!(configFile.getName().equals(activeProfile) || configFile.getName().contains("defaultsettings") || optUse.contains(configFile.getName())))
					return false;
			}

		} catch (Exception e) {
			DefaultSettings.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return true;
	}

	public static boolean checkChangedConfig() {
		boolean ret = false;
		try {

			Collection<File> config = FileUtils.listFilesAndDirs(new File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			for (File configFile : config) {
				if (!configFile.isDirectory() && !configFile.getName().contains("defaultsettings") && !configFile.getName().equals("ignore.json") && !optUse.contains(configFile.getName())) {
					String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
					String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);

					if (!mainJson.hashes.containsKey(pathString))
						continue;

					String hashC = fileToHash(new FileInputStream(configFile));

					String writtenHashS = mainJson.hashes.get(pathString);

					if (!hashC.equals(writtenHashS)) {
						ret = true;
					}
				}
			}

		} catch (Exception e) {
			DefaultSettings.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return ret;
	}

	public static boolean checkChanged() {
		boolean ret = false;
		try {

			InputStream keys = getKeysStream();
			InputStream options = getOptionsStream();
			InputStream optionsOF = getOptionsOFStream();
			InputStream optionsShaders = getOptionsShadersStream();
			InputStream servers = getServersStream();

			String hashO = "";
			String writtenHashO = "";

			if (options != null) {

				hashO = fileToHash(options);
				writtenHashO = mainJson.hashes.get(activeProfile + "/options.txt");
			}

			String hashK = "";
			String writtenHashK = "";

			if (keys != null) {

				hashK = fileToHash(keys);
				writtenHashK = mainJson.hashes.get(activeProfile + "/keys.txt");

			}

			String hashOF = "";
			String writtenHashOF = "";

			if (optionsOF != null) {

				hashOF = fileToHash(optionsOF);
				writtenHashOF = mainJson.hashes.get(activeProfile + "/optionsof.txt");

			}

			String hashShaders = "";
			String writtenHashShaders = "";

			if (optionsShaders != null) {

				hashShaders = fileToHash(optionsShaders);
				writtenHashShaders = mainJson.hashes.get(activeProfile + "/optionsshaders.txt");

			}

			String hashS = "";
			String writtenHashS = "";

			if (servers != null) {

				hashS = fileToHash(servers);
				writtenHashS = mainJson.hashes.get(activeProfile + "/servers.dat");

			}

			if (mainJson.hashes.containsKey(activeProfile + "/options.txt") && !hashO.equals(writtenHashO)) {
				ret = true;
			} else if (mainJson.hashes.containsKey(activeProfile + "/keys.txt") && !hashK.equals(writtenHashK)) {
				ret = true;
			} else if (mainJson.hashes.containsKey(activeProfile + "/optionsof.txt") && !hashOF.equals(writtenHashOF)) {
				ret = true;
			} else if (mainJson.hashes.containsKey(activeProfile + "/optionsshaders.txt") && !hashShaders.equals(writtenHashShaders)) {
				ret = true;
			} else if (mainJson.hashes.containsKey(activeProfile + "/servers.dat") && !hashS.equals(writtenHashS)) {
				ret = true;
			}

			options.close();
			File fileO = new File(getMainFolder(), activeProfile + "/options.txt_temp");
			Files.delete(fileO.toPath());
			keys.close();
			File fileK = new File(getMainFolder(), activeProfile + "/keys.txt_temp");
			Files.delete(fileK.toPath());

		} catch (Exception e) {
			DefaultSettings.log.log(Level.ERROR, "Error while saving configs: ", e);
		}

		return ret;
	}

	public static ArrayList<String> listConfigFiles() throws FileNotFoundException, IOException {
		// Collection<File> config = FileUtils.listFilesAndDirs(new
		// File(getMainFolder(), activeProfile), TrueFileFilter.INSTANCE,
		// TrueFileFilter.INSTANCE);
		ArrayList<String> files = new ArrayList<String>();
		for (File configFile : new File(getMainFolder(), activeProfile).listFiles()) {
			if (!configFile.getName().equals("ignore.json") && !configFile.getName().contains("defaultsettings")) {
				if (optUse.contains(configFile.getName()))
					continue;
				String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
				String pathString = relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);
				files.add(pathString);
			}
		}
		return files;
	}

	public static void checkMD5(boolean updateExisting, boolean configs, String file) throws FileNotFoundException, IOException {
		Collection<File> config = null;
		File dir = new File(getMainFolder(), activeProfile);
		if (file == null) {
			config = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		} else {
			config = FileUtils.listFilesAndDirs(new File(dir, file), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		}

		for (File configFile : config) {
			if (!configFile.isDirectory() && !configFile.getName().equals("ignore.json") && !configFile.getName().contains("defaultsettings")) {
				if (optUse.contains(configFile.getName()) && configs)
					continue;
				String relativePath = configFile.getPath().substring((mcDataDir.getPath().length()));
				String pathString = activeProfile + "/" + relativePath.split("defaultsettings")[1].substring(1).split(activeProfile)[1].substring(1);
				if (!updateExisting && mainJson.hashes.containsKey(pathString)) {

				} else {
					mainJson.hashes.put(pathString, fileToHash(new FileInputStream(configFile)));
				}
			}
		}

		/*
		 * Collection<File> shared = FileUtils.listFilesAndDirs(new
		 * File(getMainFolder(), "sharedConfigs"), TrueFileFilter.INSTANCE,
		 * TrueFileFilter.INSTANCE); for (File sharedFile : shared) { if
		 * (!sharedFile.isDirectory() && !sharedFile.getName().equals("ignore.json")) {
		 * String relativePath =
		 * sharedFile.getPath().substring((mcDataDir.getPath().length())); String
		 * pathString = relativePath.split("defaultsettings")[1].substring(1); if
		 * (!updateExisting && mainJson.hashes.containsKey(pathString)) {
		 * 
		 * } else { mainJson.hashes.put(pathString, fileToHash(new
		 * FileInputStream(sharedFile))); }
		 * 
		 * } }
		 */

		mainJson.save();

	}

	public static final ArrayList<String> optUse = new ArrayList<String>() {
		private static final long serialVersionUID = -6765486158086901202L;
		{
			add("options.txt");
			add("servers.dat");
			add("optionsof.txt");
			add("optionsshaders.txt");
			add("keys.txt");
		}
	};

	public static class IgnoreJSON {

		// Not used at the moment
		public static transient final long serialVersionUID = 2349872L;
		public ArrayList<String> ignore = new ArrayList<String>();
		private transient File location;

		public IgnoreJSON(File location) {
			this.location = location;
		}

		public void save() {
			try (FileWriter writer = new FileWriter(this.location)) {
				FileUtil.gson.toJson(this, writer);
			} catch (IOException e) {
				DefaultSettings.log.log(Level.ERROR, "Exception at processing startup: ", e);
			}
		}
	}

}