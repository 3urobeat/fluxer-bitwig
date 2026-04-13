/*
 * File: Activity.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-13 17:42:14
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-13 19:11:32
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


/**
 * Handles getting Bitwig activity.
 * Credit for reference: https://github.com/Merlin04/bitwig-presence/blob/main/src/main/java/io/github/merlin04/discordrpc/DiscordRPCExtension.java
 */

public class Activity {

    String idleText           = "Idling";
    long   idleStartTimestamp = 0;

    private final fluxerbitwigExtension ext;


    /**
     * Constructor
     */
    public Activity(fluxerbitwigExtension ext) {
        this.ext = ext;
    }


    /**
     * Register a project update event handler
     * @param callback Function called when activity should be updated
     */
    public void getProjectUpdateObserver(Runnable callback) {
        this.ext.app.projectName().addValueObserver(_value -> { callback.run(); });
        this.ext.app.panelLayout().addValueObserver(_value -> { callback.run(); });
    }


    /**
     * Gets currently loaded project name
     * @return Returns project name, empty if none is open
     */
    private String getProjectName() {
        return "" + this.ext.app.projectName().get();
    }


    /**
     * Gets currently active panel (Arrange, Mixing, Editing)
     * @return Returns currently active panel matching 'Application.PANEL_LAYOUT_*'
     */
    private String getProjectActivePanel() {
        return "" + this.ext.app.panelLayout().get();
    }


    /**
     * Cuts long activity name
     * @param str String to trim
     * @return Returns trimmed `str`
     */
    private String sliceActivity(String str) {
        if (str != null && str.length() > 19) { // 16 + 3 dots
            return str.substring(0, 16) + "...";
        }

        return str;
    }


    /**
     * Gets formatted Bitwig activity
     * @return Returns activity string
     */
    public String getActivityText() {

        // Get active project name and panel
        String activeProjectName = sliceActivity(this.getProjectName());

        // Construct action string
        String action = "";

        if (activeProjectName.length() > 0) {
            String activePanel = this.getProjectActivePanel();

            action = activePanel.equals(Application.PANEL_LAYOUT_ARRANGE) ? "Arranging"
                : activePanel.equals(Application.PANEL_LAYOUT_MIX) ? "Mixing"
                : activePanel.equals(Application.PANEL_LAYOUT_EDIT) ? "Editing"
                : "";
        }

        // Construct activity string. Unset idleStartTimestamp if user is active
        String activity = "";

        if (action.length() > 0) {
            activity = action + " '" + activeProjectName + "'";
            idleStartTimestamp = 0;
        } else {                        // User is inactive, set idle text (if enabled) and set idleStartTimestamp if user just began idling
            if (this.ext.config.statusShowIdle) {
                activity = idleText;
            }

            if (idleStartTimestamp == 0) {
                this.ext.logDebug("User has started idling");
                idleStartTimestamp = System.currentTimeMillis();
            }
        }

        // Check if we shall clear status when user has been idle for a long time and abort
        int clearIdleAfter = this.ext.config.statusClearIdleAfter;

        if (clearIdleAfter > 0 && idleStartTimestamp != 0 && System.currentTimeMillis() > idleStartTimestamp + clearIdleAfter) {
            if (this.ext.request.getLastStatusText() != "") {
                this.ext.logInfo("Clearing status because 'statusClearIdleAfter' is enabled and user has been idle for ~" + Math.round((((System.currentTimeMillis() - idleStartTimestamp) / 1000) / 60) / 60) + " minutes...");
            }
            return "";
        }

        // Put activity string into user specified format
        String activityText = "";

        if (activity.length() > 0) {
            activityText = this.ext.config.statusActivityText
                .replace("{activity}", activity);
        }

        // Construct final string
        String appName = this.ext.config.statusAppName;

        return this.ext.config.statusFormat
            .replace("{appName}", appName)
            .replace("{activityText}", activityText);

    }

}
