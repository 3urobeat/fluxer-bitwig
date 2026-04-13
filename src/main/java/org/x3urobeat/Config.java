/*
 * File: Config.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 20:54:10
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-13 17:26:19
 * Modified By: 3urobeat
 *
 * Copyright (c) 2026 3urobeat <https://github.com/3urobeat>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */


package org.x3urobeat;


/**
 * Contains extension configuration
 */

public class Config {

    // Config items
    public boolean  enable              = true;
    public int      updateCheckInterval = 5000;
    public int      statusUntil         = 300000;
    public String   token               = "";

    public String  statusFormat         = "🎮 {appName} {activityText}";
    public String  statusActivityText   = "- {activity}";
    public String  statusAppName        = "VSCode";
    public boolean statusShowIdle       = true;
    public int     statusClearIdleAfter = 0;


    /**
     * Constructor
     */
    public Config() {

    }

}
