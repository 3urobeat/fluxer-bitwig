/*
 * File: fluxerbitwigExtensionDefinition.java
 * Project: fluxer-bitwig
 * Created Date: 2026-04-12 12:25:38
 * Author: 3urobeat
 *
 * Last Modified: 2026-04-13 17:09:12
 * Modified By: 3urobeat
 *
 * Copyright (c) 2026 3urobeat <https://github.com/3urobeat>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.x3urobeat;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class fluxerbitwigExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("9cd5b042-7449-4c13-910a-b14b79716d0c");

   public fluxerbitwigExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "fluxer-bitwig";
   }

   @Override
   public String getAuthor()
   {
      return "3urobeat";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }

   @Override
   public String getHardwareVendor()
   {
      return "3urobeat";
   }

   @Override
   public String getHardwareModel()
   {
      return "fluxer-bitwig";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 25;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 0;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 0;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
   }

   @Override
   public fluxerbitwigExtension createInstance(final ControllerHost host)
   {
      return new fluxerbitwigExtension(this, host);
   }
}
