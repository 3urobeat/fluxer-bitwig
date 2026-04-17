/*
 * File: Config.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 20:54:10
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-17 15:20:40
 * Modified By: 3urobeat
 *
 * Copyright (c) 2026 3urobeat <https://github.com/3urobeat>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


package org.x3urobeat;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.DocumentState;
import com.bitwig.extension.controller.api.SettableEnumValue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * Contains extension configuration
 */

public class Config {

    private final fluxerbitwigExtension ext;
    private final Runnable              configUpdateEventHandler;

    private static final String booleanOptions[] = { "Off", "On" };

    // Config items
    public boolean  enable              = true;
    public int      statusUntil         = 300000;
    public String   token               = "";

    public String  statusFormat         = "🎹 {appName} {activityText}";
    public String  statusActivityText   = "- {activity}";
    public String  statusAppName        = "Bitwig Studio";
    public boolean statusShowIdle       = true;
    public int     statusClearIdleAfter = 0;


    /**
     * Constructor
     * @param configUpdateEventHandler Function called on config update event
     */
    public Config(fluxerbitwigExtension ext, Runnable configUpdateEventHandler) {
        this.ext = ext;
        this.configUpdateEventHandler = configUpdateEventHandler;

        this.loadConfig();
        this.registerSettings();
    }


    /**
     * Returns the config file path for the current OS.
     * Linux: ~/.config/fluxer-bitwig/config
     * macOS: ~/Library/Application Support/fluxer-bitwig/config
     * Windows: %APPDATA%\fluxer-bitwig\config
     * @return Path to the config file
     */
    private Path getConfigFilePath() {
        String homeDir = System.getProperty("user.home");
        Path configDir;

        String osName = System.getProperty("os.name").toLowerCase();

        // Construct configDir path per platform
        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            configDir = Paths.get(appData != null ? appData : homeDir, "fluxer-bitwig");

        } else if (osName.contains("mac") || osName.contains("darwin")) {
            configDir = Paths.get(homeDir, "Library", "Application Support", "fluxer-bitwig");

        } else {
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

            if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
                configDir = Paths.get(xdgConfigHome, "fluxer-bitwig");
            } else {
                configDir = Paths.get(homeDir, ".config", "fluxer-bitwig");
            }
        }

        // Append config.properties filename to path
        Path configPath = configDir.resolve("config.properties");

        // Ensure directory exists
        try {
            if (configDir != null && !Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            this.ext.logErr("Failed to create config directory: " + e.getMessage());
        }

        // ...aaaand return
        return configPath;
    }


    /**
     * Loads configuration from the config file.
     * If the file doesn't exist, default values are used.
     */
    private void loadConfig() {
        Path configFile = getConfigFilePath();

        // Write default config file if none exists yet
        if (!Files.exists(configFile)) {
            this.ext.logInfo("Config file not found at '" + configFile + "', using defaults.");
            this.saveConfig();
            return;
        }

        // Attempt to read properties file and stream props into class variables
        try (InputStream input = new FileInputStream(configFile.toFile())) {
            Properties props = new Properties();
            props.load(input);

            if (props.containsKey("statusUntil"))          this.statusUntil = Integer.parseInt(props.getProperty("statusUntil"));
            if (props.containsKey("token"))                this.token = props.getProperty("token");
            if (props.containsKey("statusFormat"))         this.statusFormat = props.getProperty("statusFormat");
            if (props.containsKey("statusActivityText"))   this.statusActivityText = props.getProperty("statusActivityText");
            if (props.containsKey("statusAppName"))        this.statusAppName = props.getProperty("statusAppName");
            if (props.containsKey("statusShowIdle"))       this.statusShowIdle = Boolean.parseBoolean(props.getProperty("statusShowIdle"));
            if (props.containsKey("statusClearIdleAfter")) this.statusClearIdleAfter = Integer.parseInt(props.getProperty("statusClearIdleAfter"));

            this.ext.logInfo("Loaded config from '" + configFile + "'");

        } catch (IOException e) {
            this.ext.logInfo("Failed to read config file: " + e.getMessage());
        } catch (NumberFormatException e) {
            this.ext.logInfo("Invalid number format in config file: " + e.getMessage());
        }
    }


    /**
     * Saves the current configuration to the config file.
     * @return true on success, false on error
     */
    public boolean saveConfig() {
        Path configFile = getConfigFilePath();

        // Attempt to open config file and write class variables to props
        try (OutputStream output = new FileOutputStream(configFile.toFile())) {
            Properties props = new Properties();

            props.setProperty("statusUntil", String.valueOf(this.statusUntil));
            props.setProperty("token", this.token);
            props.setProperty("statusFormat", this.statusFormat);
            props.setProperty("statusActivityText", this.statusActivityText);
            props.setProperty("statusAppName", this.statusAppName);
            props.setProperty("statusShowIdle", String.valueOf(this.statusShowIdle));
            props.setProperty("statusClearIdleAfter", String.valueOf(this.statusClearIdleAfter));

            props.store(output, "fluxer-bitwig config");

            this.ext.logInfo("Saved config to '" + configFile + "'");
            return true;

        } catch (IOException e) {
            this.ext.logErr("Failed to save config file: " + e.getMessage());
            return false;
        }
    }


    /**
     * Handles setting update event
     */
    private void updateSetting() {
        // Emit update to registered event handler
        this.configUpdateEventHandler.run();
    }


    /**
     * Registers setting inputs in Bitwig Controller popout
     */
    private void registerSettings() {
        ControllerHost host = this.ext.getHost();

        // Register setting inputs & handlers that appear in the controller popout
        DocumentState documentState = host.getDocumentState();

        SettableEnumValue enableSetting = documentState.getEnumSetting("Enable for this project", "opts", booleanOptions, this.enable ? booleanOptions[1] : booleanOptions[0]); // Roundabout way to display a boolean setting (Off, On)
        enableSetting.addValueObserver(value -> {
            this.enable = value.equals(booleanOptions[1]);
            updateSetting();
        });

        // Not exposing statusUntil for now

        // These settings have been moved to a config file to apply to all Bitwig projects instead of being saved in them

        /* SettableStringValue tokenSetting = documentState.getStringSetting("Fluxer Token", "opts", 32, this.token);
        tokenSetting.addValueObserver(value -> {
            this.token = value;
            updateSetting();
        });

        SettableStringValue statusFormatSetting = documentState.getStringSetting("Status Format", "opts", 32, this.statusFormat);
        statusFormatSetting.addValueObserver(value -> {
            this.statusFormat = value;
            updateSetting();
        });

        SettableStringValue statusActivityTextSetting = documentState.getStringSetting("Status Activity Text", "opts", 32, this.statusActivityText);
        statusActivityTextSetting.addValueObserver(value -> {
            this.statusActivityText = value;
            updateSetting();
        });

        SettableStringValue statusAppNameSetting = documentState.getStringSetting("Status App Name", "opts", 32, this.statusAppName);
        statusAppNameSetting.addValueObserver(value -> {
            this.statusAppName = value;
            updateSetting();
        });

        SettableEnumValue statusShowIdleSetting = documentState.getEnumSetting("Show Idle", "opts", booleanOptions, this.statusShowIdle ? booleanOptions[1] : booleanOptions[0]); // Roundabout way to display a boolean setting (Off, On)
        statusShowIdleSetting.addValueObserver(value -> {
            this.statusShowIdle = value.equals(booleanOptions[1]);;
            updateSetting();
        });

        SettableRangedValue statusClearIdleAfterSetting = documentState.getNumberSetting("Clear Idle After", "opts", 0, 3600, 60, "sec", this.statusClearIdleAfter / 1000);
        statusClearIdleAfterSetting.addValueObserver(value -> {
            this.statusClearIdleAfter = (int) value * 1000;
            updateSetting();
        }); */
    }

}
