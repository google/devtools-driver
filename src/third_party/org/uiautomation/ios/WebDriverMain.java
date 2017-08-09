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

import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Runner class for a webdriver for a generic browser */
final class WebDriverMain {
  private static final Logger log = Logger.getLogger(WebDriverMain.class.getName());

  /** Start a webdriver server for the browser specified by factory. */
  static void run(IOSServerConfiguration options, BrowserLauncher launcher) {
    final IOSServer server = new IOSServer(options, launcher);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  server.stopGracefully();
                } catch (Exception e) {
                  log.log(Level.SEVERE, "error in shutdown hook", e);
                }
              }
            });

    try {
      server.start();
    } catch (Exception e) {
      log.log(Level.SEVERE, "cannot start ios-driver server.", e);
      Runtime.getRuntime().exit(1);
    }
  }

  static File getTmpIOSFolder() {
    File f = new File(System.getProperty("java.io.tmpdir") + "/.ios-driver/");
    f.mkdirs();
    return f;
  }

  private WebDriverMain() {}
}
