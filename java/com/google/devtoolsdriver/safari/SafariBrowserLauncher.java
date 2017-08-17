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

import com.google.common.base.Suppliers;
import com.google.common.net.HostAndPort;
import com.google.devtoolsdriver.webdriver.BrowserException;
import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import com.google.iosdevicecontrol.IosDevice;
import com.google.iosdevicecontrol.IosDeviceException;
import com.google.iosdevicecontrol.IosDeviceHost;
import com.google.iosdevicecontrol.real.RealDeviceHost;
import com.google.iosdevicecontrol.simulator.SimulatorDevice;
import com.google.iosdevicecontrol.simulator.SimulatorDeviceHost;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import org.openqa.selenium.remote.DesiredCapabilities;

/** Launches and returns a Browser instance referencing a connected device. */
public abstract class SafariBrowserLauncher implements BrowserLauncher {
  private static final Supplier<SafariBrowserLauncher> REAL_DEVICE_LAUNCHER =
      Suppliers.memoize(
          () -> onRealDevice(RealDeviceHost.withDeveloperDiskImagesFromXcode().initialize()));

  /** Returns a launcher for Safari on a real device. */
  public static SafariBrowserLauncher onRealDevice() {
    return REAL_DEVICE_LAUNCHER.get();
  }

  /** Returns a launcher for Safari on a real device with a custom device host. */
  public static SafariBrowserLauncher onRealDevice(RealDeviceHost host) {
    return new SafariBrowserLauncher(host) {
      @Override
      public SafariBrowser launch(String udid) throws BrowserException {
        try {
          IosDevice device = super.deviceHost.connectedDevice(udid);
          return SafariBrowser.startOnRealDevice(device);
        } catch (IOException e) {
          throw new BrowserException(e);
        }
      }
    };
  }

  /** Returns a launcher for Safari on the simulator. All crash logs are deleted before launch. */
  public static SafariBrowserLauncher onSimulator(boolean force) {
    return new SafariBrowserLauncher(SimulatorDeviceHost.INSTANCE) {
      @Override
      public SafariBrowser launch(String udid) throws BrowserException {
        try {
          SimulatorDevice sim = (SimulatorDevice) super.deviceHost.connectedDevice(udid);
          SimulatorDeviceHost simHost = (SimulatorDeviceHost) super.deviceHost;
          if (force) {
            simHost.shutdownAllDevices();
            while (simHost.deviceOnInspectorPort().isPresent()) {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BrowserException(e);
              }
            }
          }
          simHost.clearCrashLogs();
          sim.startup();
          while (!simHost.deviceOnInspectorPort().isPresent()) {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new BrowserException(e);
            }
          }
          return SafariBrowser.startOnSimulator(sim);
        } catch (IOException | IosDeviceException e) {
          throw new BrowserException(e);
        }
      }
    };
  }

  private final IosDeviceHost deviceHost;

  private SafariBrowserLauncher(IosDeviceHost deviceHost) {
    this.deviceHost = deviceHost;
  }

  /** Hook method to launch Safari on the specified device udid. */
  public abstract SafariBrowser launch(String udid) throws BrowserException;

  /** Launches Safari on a connected devices. */
  public final SafariBrowser launch() throws BrowserException {
    try {
      return launch(deviceHost.connectedDevices().iterator().next().udid());
    } catch (IOException e) {
      throw new BrowserException(e);
    }
  }

  @Override
  public final SafariBrowser launch(DesiredCapabilities caps) throws BrowserException {
    String udid = (String) caps.getCapability("uuid");
    SafariBrowser browser = udid == null ? launch() : launch(udid);
    @SuppressWarnings("unchecked")
    Map<String, String> proxyDict = (Map<String, String>) caps.getCapability("proxy");
    if (proxyDict != null) {
      HostAndPort proxy = HostAndPort.fromString(proxyDict.get("httpProxy"));
      browser.setHttpProxy(proxy);
    }
    @SuppressWarnings("unchecked")
    Map<String, String> cert = (Map<String, String>) caps.getCapability("httpsCert");
    if (cert != null) {
      browser.installHttpsCert(cert.get("certName"), cert.get("certContentBase64"));
    }
    return browser;
  }
}
