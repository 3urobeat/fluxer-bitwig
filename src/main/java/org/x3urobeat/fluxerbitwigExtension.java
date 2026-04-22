/*
 * File: fluxerbitwigExtension.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 12:25:38
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-22 18:05:25
 * Modified By: 3urobeat
 *
 * Copyright (c) 2026 3urobeat <https://github.com/3urobeat>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


package org.x3urobeat;

import com.bitwig.extension.controller.api.Application;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.ControllerExtension;


/**
 * Bitwig extension main file
 * Bitwig Studio Extension API Manual: file:///opt/bitwig-studio/resources/doc/control-surface/api/index.html
 */

public class fluxerbitwigExtension extends ControllerExtension  {

    public Activity activity;
    public Config   config;
    public Request  request;

    public Application app; // Contains active project & app state information

    public boolean isInitialized = false;

    public static final boolean enableDebugLogs = false;


    /**
     * Constructor, called by Bitwig (Host)
     */
    protected fluxerbitwigExtension(final fluxerbitwigExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    /** Logs debug message to Bitwig controller console */
    public void logDebug(String txt) {
        if (enableDebugLogs) {
            getHost().println("[fluxer-bitwig | DEBUG] " + txt);
        }
    }

    /** Logs info message to Bitwig controller console */
    public void logInfo(String txt) {
        getHost().println("[fluxer-bitwig | INFO] " + txt);
    }

    /** Logs error message to Bitwig controller console */
    public void logErr(String txt) {
        getHost().errorln("[fluxer-bitwig | ERROR] " + txt);
    }

    /** Shows a on screen popup notification */
    public void showNotification(String txt) {
        getHost().showPopupNotification(txt);
    }


    /**
     * Initiates a status update event
     */
    public void updateStatus() {
        if (!isInitialized) return; // Extension is disabled

        this.logDebug("Got Project Update Event");

        try {
            request.updatePresence(false);
        } catch (Exception e) {
            logErr("Failed to consume project update event: " + e.getMessage());
        }
    }


    /**
     * Internal: Initializes the extension
     */
    private void initializeExtension() {
        if (isInitialized) return; // Safeguard

        final ControllerHost host = getHost();

        // Is extension disabled in settings?
        if (!this.config.enable) {
            this.logInfo("Ignoring extension init because extension is disabled");
            return;
        }

        this.logInfo("Initializing extension...");
        //host.showPopupNotification("[fluxer-bitwig] Extension activated!");

        // Deny further init if no token is set
        if (config.token == null || config.token.length() == 0) {
            this.logErr("No Fluxer token set!");
            host.showPopupNotification("Please configure your Fluxer token in the extension settings!");
        }

        this.isInitialized = true;
    }


    /**
     * Internal: Uninitializes the extension
     */
    private void uninitializeExtension() {
        if (!isInitialized) return; // Safeguard

        this.logInfo("Uninitializing extension...");

        // Clear status if we ever sent one. Ignore errors because whatyagonnadonow
        try {
            if (this.request.getLastStatusText().length() > 0) {
                this.request.sendPresenceUpdate("", true); // Force
            }
        } catch(Exception e) {}

        this.isInitialized = false;
    }


    /**
     * Called (once!) by Bitwig when the extension is activated
     */
    @Override
    public void init() {
        final ControllerHost host = getHost();
        this.app = host.createApplication();   // Has to be created in init or Bitwig gets angry at us

        this.logInfo("Extension got activated!");

        // Create plugin class instances and register handler for update events
        config = new Config(this, () -> {
            this.logDebug("Got Config Update Event");

            if (!this.config.enable) {
                this.uninitializeExtension();
                return;
            }

            this.initializeExtension(); // If extension is already initialized, nothing will happen
            this.updateStatus();        // ...but in any enabled case we want to update our status to apply changed settings
        });
        activity = new Activity(this, () -> this.updateStatus());
        request  = new Request(this);
    }


    /**
     * Called by Bitwig when extension is deactivated
     */
    @Override
    public void exit() {
        this.uninitializeExtension();
        this.config.stopWatcher();

        // Goodbye
        // getHost().showPopupNotification("[fluxer-bitwig] Extension deactivated");
        this.logInfo("Extension got deactivated");
    }


    /**
     * Called when extension should flush any pending updates
     */
    @Override
    public void flush() { }

}
