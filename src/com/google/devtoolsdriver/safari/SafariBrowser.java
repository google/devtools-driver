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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.iosdevicecontrol.IosAppBundleId;
import com.google.iosdevicecontrol.IosAppProcess;
import com.google.iosdevicecontrol.IosDevice;
import com.google.iosdevicecontrol.IosDeviceException;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.DevtoolsDebugger;
import com.google.devtoolsdriver.devtools.DevtoolsErrorException;
import com.google.devtoolsdriver.devtools.DevtoolsEvent;
import com.google.devtoolsdriver.devtools.DevtoolsResult;
import com.google.devtoolsdriver.devtools.Runtime;
import com.google.devtoolsdriver.openurl.OpenUrlApp;
import com.google.iosdevicecontrol.real.RealDevice;
import com.google.devtoolsdriver.safari.InspectorMessenger.AppListing;
import com.google.iosdevicecontrol.simulator.SimulatorDevice;
import com.google.devtoolsdriver.webdriver.Browser;
import com.google.devtoolsdriver.webdriver.BrowserException;
import com.google.devtoolsdriver.webdriver.PageId;
import com.google.iosdevicecontrol.webinspector.InspectorPage;
import com.google.iosdevicecontrol.webinspector.WebInspector;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import javax.json.JsonObject;

/** An implementation of the Devtools browser interface for mobile Safari */
public abstract class SafariBrowser implements Browser {
  private static final String SAFARI_BUNDLE_ID = "com.apple.mobilesafari";
  private static final DevtoolsCommand HAS_FOCUS_COMMAND = Runtime.evaluate("document.hasFocus()");
  private static final DevtoolsCommand CLOSE_COMMAND = Runtime.evaluate("window.close()");
  private static final Duration HAS_FOCUS_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(15);

  static SafariBrowser startOnRealDevice(IosDevice device) throws BrowserException {
    return new RealDeviceSafariBrowser(device);
  }

  static SafariBrowser startOnSimulator(IosDevice device) throws BrowserException {
    return new SimulatorSafariBrowser(device);
  }

  private final IosDevice device;
  private final InspectorMessenger messenger;
  private final SafariDebugger debugger = new SafariDebugger();

  private SafariBrowser(IosDevice device) throws BrowserException {
    this.device = checkNotNull(device);
    messenger = new InspectorMessenger(openSafari());
    messenger.setEventListener(debugger);
    connect();
  }

  private void connect() throws BrowserException {
    try {
      messenger.sendConnect();
      for (AppListing appInfo : messenger.awaitAllAppListings(SAFARI_BUNDLE_ID)) {
        for (InspectorPage page : appInfo.listing.get()) {
          messenger.sendSwitchTo(appInfo.app.applicationId(), page.pageId());
          DevtoolsResult result = sendCommand(HAS_FOCUS_COMMAND, HAS_FOCUS_TIMEOUT);
          if (result.json().getJsonObject("result").getBoolean("value")) {
            return;
          }
        }
      }
    } catch (IOException e) {
      throw new BrowserException(e);
    }
    throw new BrowserException("could not find a page in focus");
  }

  @Override
  public final PageId activePage() {
    return PageId.of(Integer.toString(messenger.activePageId().getAsInt()));
  }

  @Override
  public final ImmutableList<PageId> listPages() throws BrowserException {
    try {
      messenger.sendListPages();
      return messenger
          .awaitPages()
          .stream()
          .map(p -> PageId.of(Integer.toString(p.pageId())))
          .collect(ImmutableList.toImmutableList());
    } catch (IOException e) {
      throw new BrowserException(e);
    }
  }

  @Override
  public final boolean switchTo(PageId pageId) throws BrowserException {
    try {
      return messenger.sendSwitchTo(Integer.parseInt(pageId.asString()));
    } catch (IOException e) {
      throw new BrowserException(e);
    }
  }

  @Override
  public final DevtoolsResult sendCommand(DevtoolsCommand command, Duration timeout)
      throws BrowserException {
    try {
      return debugger.sendCommand(command, timeout);
    } catch (DevtoolsErrorException | IOException e) {
      throw new BrowserException(e);
    }
  }

  @Override
  public final void addEventListener(Consumer<DevtoolsEvent> listener) {
    debugger.addEventListener(listener);
  }

  @Override
  public final byte[] takeScreenshot() throws BrowserException {
    try {
      return device.takeScreenshot();
    } catch (IosDeviceException e) {
      throw new BrowserException(e);
    }
  }

  @Override
  public final String browserName() {
    return "safari";
  }

  @Override
  public final String platformName() {
    return "IOS";
  }

  @Override
  public final void close() throws BrowserException {
    try (Closeable ignored = this.messenger) {
      // Attempt to close all the tabs through JavaScript.
      for (PageId pageId : listPages()) {
        switchTo(pageId);
        sendCommand(CLOSE_COMMAND, CLOSE_TIMEOUT);
      }
      closeSafari();
    } catch (IOException e) {
      throw new BrowserException(e);
    }
  }

  /** Hook method to open Safari. */
  abstract WebInspector openSafari() throws BrowserException;

  /** Hook method to close Safari */
  abstract void closeSafari() throws BrowserException;

  abstract void setHttpProxy(HostAndPort hostAndPort) throws BrowserException;

  private final class SafariDebugger extends DevtoolsDebugger implements Consumer<JsonObject> {
    @Override
    protected synchronized void sendMessage(JsonObject message) throws IOException {
      messenger.sendCommand(message);
    }

    @Override
    public void accept(JsonObject message) {
      notifyMessageReceived(message);
    }
  }

  private static final class RealDeviceSafariBrowser extends SafariBrowser {
    RealDeviceSafariBrowser(IosDevice device) throws BrowserException {
      super(device);
    }

    @Override
    WebInspector openSafari() throws BrowserException {
      OpenUrlApp openUrlResource = OpenUrlApp.fromResource();
      try {
        openUrlResource.openBlankPage(super.device);
        return WebInspector.connectToRealDevice(super.device.udid());
      } catch (IOException e) {
        throw new BrowserException(new IosDeviceException(super.device, e));
      } catch (IosDeviceException e) {
        throw new BrowserException(e);
      }
    }

    @Override
    void closeSafari() throws BrowserException {
      try {
        // Uninstall the HTTP proxy profile if it's installed. If it's not, this is a no-op.
        RealDevice realDevice = (RealDevice) super.device;
        ConfigProfiles.removeProxyProfile(realDevice);
      } catch (IosDeviceException e) {
        throw new BrowserException(e);
      }
      // We have no way to close Safari on a real device.
    }

    @Override
    void setHttpProxy(HostAndPort hostAndPort) throws BrowserException {
      try {
        RealDevice realDevice = (RealDevice) super.device;
        ConfigProfiles.installProxyProfile(hostAndPort, realDevice);
      } catch (IosDeviceException | IOException e) {
        throw new BrowserException(e);
      }
    }
  }

  private static final class SimulatorSafariBrowser extends SafariBrowser {
    private IosAppProcess safariProcess;

    private SimulatorSafariBrowser(IosDevice device) throws BrowserException {
      super(device);
    }

    @Override
    WebInspector openSafari() throws BrowserException {
      SimulatorDevice sim = (SimulatorDevice) super.device;
      try {
        safariProcess = sim.runApplication(new IosAppBundleId(SAFARI_BUNDLE_ID));
        return WebInspector.connectToSimulator();
      } catch (IOException e) {
        throw new BrowserException(new IosDeviceException(sim, e));
      } catch (IosDeviceException e) {
        throw new BrowserException(e);
      }
    }

    @Override
    void closeSafari() throws BrowserException {
      safariProcess.kill();
    }

    @Override
    void setHttpProxy(HostAndPort hostAndPort) throws BrowserException {
      // Setting proxy is not yet supported on simulator.
    }
  }
}
