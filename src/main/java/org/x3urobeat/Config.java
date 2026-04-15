/*
 * File: Config.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 20:54:10
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-15 17:42:03
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
import com.bitwig.extension.controller.api.SettableBooleanValue;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.SettableRangedValue;
import com.bitwig.extension.controller.api.SettableStringValue;


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

        this.registerSettings();
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

        SettableEnumValue enableSetting = documentState.getEnumSetting("Enable", "opts", booleanOptions, this.enable ? booleanOptions[1] : booleanOptions[0]); // Roundabout way to display a boolean setting (Off, On)
        enableSetting.addValueObserver(value -> {
            this.enable = value.equals(booleanOptions[1]);
            updateSetting();
        });

        // Not exposing statusUntil for now

        SettableStringValue tokenSetting = documentState.getStringSetting("Fluxer Token", "opts", 32, this.token);
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
        });
    }

}
