// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtoolsdriver.safari;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.google.common.io.Resources;
import com.google.common.net.HostAndPort;
import com.google.iosdevicecontrol.IosDeviceException;
import com.google.iosdevicecontrol.real.RealDevice;
import com.google.iosdevicecontrol.util.PlistParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** ProfileEditorUtils provides utilities for editing real device configuration profiles. */
final class ConfigProfiles {
  private static final String PROXY_PROFILE_PATH =
      "com/google/devtoolsdriver/safari/profiles/http_proxy_template.mobileconfig";
  private static final String PROXY_PROFILE_ID = "com.google.devtoolsdriver.safari.http_proxy";

  /** Generates a profile to communicate with a specified HTTP/S proxy and port. */
  static void installProxyProfile(HostAndPort hostAndPort, RealDevice device)
      throws IOException, IosDeviceException {
    checkState(hostAndPort.hasPort());
    String templateXml = Resources.toString(Resources.getResource(PROXY_PROFILE_PATH), UTF_8);
    NSDictionary plistDict = (NSDictionary) PlistParser.fromXml(templateXml);
    NSArray plistArray = (NSArray) plistDict.get("PayloadContent");
    NSDictionary plistInnerDict = (NSDictionary) plistArray.objectAtIndex(0);
    plistInnerDict.put("ProxyServer", hostAndPort.getHost());
    plistInnerDict.put("ProxyServerPort", hostAndPort.getPort());
    String plist = plistDict.toXMLPropertyList();

    Path configFile = Files.createTempFile("modified_proxy_profile", ".mobileconfig");
    Files.write(configFile, plist.getBytes(UTF_8));
    device.installProfile(configFile.toAbsolutePath());
  }

  static void removeProxyProfile(RealDevice device) throws IosDeviceException {
    if (isProfileInstalled(device, PROXY_PROFILE_ID)) {
      device.removeProfile(PROXY_PROFILE_ID);
    }
  }

  private static boolean isProfileInstalled(RealDevice device, String profileId)
      throws IosDeviceException {
    return device
        .listConfigurationProfiles()
        .stream()
        .anyMatch(p -> p.identifier().equals(profileId));
  }

  private ConfigProfiles() {}
}
