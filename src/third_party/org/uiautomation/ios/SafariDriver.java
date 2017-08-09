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

package org.uiautomation.ios;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.iosdevicecontrol.real.RealDeviceHost;
import com.google.devtoolsdriver.safari.SafariBrowserLauncher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runner class for a webdriver for Safari */
public final class SafariDriver {
  public static void main(String[] args) throws Exception {
    WebDriverMain.getTmpIOSFolder(); // Ensures logging directory exists.
    SafariIOSServerConfiguration options = new SafariIOSServerConfiguration();
    new JCommander(options).parse(args);

    SafariBrowserLauncher launcher;
    if (options.getIsSimulator()) {
      launcher = SafariBrowserLauncher.onSimulator(true);
    } else {
      RealDeviceHost.Configuration hostConf = RealDeviceHost.withDeveloperDiskImagesFromXcode();
      if (!options.supervisionCert.isEmpty() && !options.supervisionKey.isEmpty()) {
        hostConf =
            hostConf.withSupervisionIdentity(
                Paths.get(options.supervisionCert), Paths.get(options.supervisionKey));
      } else if (options.supervisionCert.isEmpty() != options.supervisionKey.isEmpty()) {
        throw new IllegalArgumentException("Supervision cert passed without key, or vice-versa");
      }
      launcher = SafariBrowserLauncher.onRealDevice(hostConf.initialize());
    }

    WebDriverMain.run(options, launcher);
  }

  private static class SafariIOSServerConfiguration extends IOSServerConfiguration {
    @Parameter(description = "supported real device uuid to whitelist.", names = "-uuid")
    private final List<String> uuidWhitelist = new ArrayList<>();

    @Parameter(
      description = "optional set true to run against simulator, defaults to real device",
      names = "-simulator"
    )
    private boolean isSimulator = false;

    @Parameter(
      description = "path to the supervision certificate for real devices.",
      names = "-supervision_cert"
    )
    private String supervisionCert = "";

    @Parameter(
      description = "path to the supervision private key for real devices.",
      names = "-supervision_key"
    )
    private String supervisionKey = "";

    private SafariIOSServerConfiguration() {}

    public Set<String> getUuidWhitelist() {
      return Collections.unmodifiableSet(new HashSet<>(uuidWhitelist));
    }

    boolean getIsSimulator() {
      return isSimulator;
    }

    public String getSupervisionCert() {
      return supervisionCert;
    }

    public String getSupervisionKey() {
      return supervisionKey;
    }
  }

  private SafariDriver() {}
}

