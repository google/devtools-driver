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
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.google.common.io.Resources;
import com.google.common.net.HostAndPort;
import com.google.iosdevicecontrol.IosDeviceException;
import com.google.iosdevicecontrol.real.RealDevice;
import com.google.iosdevicecontrol.util.PlistParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** ProfileEditorUtils provides utilities for editing real device configuration profiles. */
final class ConfigProfiles {
  private static final String PROFILES_ROOT = "com/google/devtoolsdriver/safari/profiles/";
  private static final String PROXY_PROFILE_PATH =
      PROFILES_ROOT + "http_proxy_template.mobileconfig";
  private static final String CERT_PROFILE_PATH =
      PROFILES_ROOT + "cert_profile_template.mobileconfig";
  private static final String PROXY_PROFILE_ID = "com.google.devtoolsdriver.safari.http_proxy";
  private static final String CERT_PROFILE_ID = "com.google.devtoolsdriver.safari.https_cert";

  /** Installs a profile to communicate with a specified HTTP/S proxy and port. */
  static void installProxyProfile(RealDevice device, HostAndPort hostAndPort)
      throws IosDeviceException {
    checkState(hostAndPort.hasPort());
    NSDictionary replacementDict = new NSDictionary();
    replacementDict.put("ProxyServer", hostAndPort.getHost());
    replacementDict.put("ProxyServerPort", hostAndPort.getPort());
    ConfigProfiles.installProfile(device, PROXY_PROFILE_PATH, replacementDict);
  }

  /** Installs a profile to set up an HTTPS certificate on the device. */
  static void installCertProfile(RealDevice device, String certName, String certContentBase64)
      throws IosDeviceException {
    NSDictionary replacementDict = new NSDictionary();
    replacementDict.put("PayloadCertificateFileName", certName + ".cer");
    try {
      replacementDict.put("PayloadContent", new NSData(certContentBase64));
    } catch (IOException e) {
      throw new IosDeviceException(device, e);
    }
    replacementDict.put("PayloadDisplayName", certName);
    ConfigProfiles.installProfile(device, CERT_PROFILE_PATH, replacementDict);
  }

  private static void installProfile(RealDevice device, String profilePath, NSDictionary newPayload)
      throws IosDeviceException {
    try {
      String templateXml = Resources.toString(Resources.getResource(profilePath), UTF_8);
      NSDictionary plistDict = (NSDictionary) PlistParser.fromXml(templateXml);
      NSArray plistArray = (NSArray) plistDict.get("PayloadContent");
      NSDictionary plistInnerDict = (NSDictionary) plistArray.objectAtIndex(0);
      for (Map.Entry<String, NSObject> entry : newPayload.entrySet()) {
        plistInnerDict.put(entry.getKey(), entry.getValue());
      }
      String plist = plistDict.toXMLPropertyList();

      Path configFile = Files.createTempFile("modified_profile", ".mobileconfig");
      Files.write(configFile, plist.getBytes(UTF_8));
      device.installProfile(configFile.toAbsolutePath());
    } catch (IOException e) {
      throw new IosDeviceException(device, e);
    }
  }

  static void removeProxyProfile(RealDevice device) throws IosDeviceException {
    removeProfile(device, PROXY_PROFILE_ID);
  }

  static void removeCertProfile(RealDevice device) throws IosDeviceException {
    removeProfile(device, CERT_PROFILE_ID);
  }

  private static void removeProfile(RealDevice device, String profileId) throws IosDeviceException {
    boolean profileInstalled =
        device.listConfigurationProfiles().stream().anyMatch(p -> p.identifier().equals(profileId));
    if (profileInstalled) {
      device.removeProfile(profileId);
    }
  }

  private ConfigProfiles() {}
}
