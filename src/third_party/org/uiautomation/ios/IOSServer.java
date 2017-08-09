/*
 * Copyright 2012-2013 eBay Software Foundation and ios-driver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.uiautomation.ios;

import com.google.devtoolsdriver.webdriver.BrowserLauncher;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.uiautomation.ios.servlet.IOSServlet;

/** The entry point for the ios-driver server. */
public final class IOSServer {
  public static final String DRIVER = IOSServerManager.class.getName();
  public static final String SERVER = "serverInstance";

  private static final Logger log = Logger.getLogger(IOSServer.class.getName());

  private final IOSServerConfiguration options;
  private boolean initialized = false;
  private final BrowserLauncher launcher;
  private Server server;
  private IOSServerManager driver;

  IOSServer(IOSServerConfiguration options, BrowserLauncher launcher) {
    this.options = options;
    this.launcher = launcher;
  }

  private void initDriver() {
    driver = new IOSServerManager(launcher);
    p(String.format("Inspector: http://0.0.0.0:%d/inspector/", options.getPort()));
    p(String.format("Tests can access the server at http://0.0.0.0:%d/wd/hub", options.getPort()));
    p(String.format("Server status: http://0.0.0.0:%d/wd/hub/status", options.getPort()));
    p(String.format("Connected devices: http://0.0.0.0:%d/wd/hub/devices/all", options.getPort()));
    p(String.format("Applications: http://0.0.0.0:%d/wd/hub/applications/all", options.getPort()));
    p(String.format("Capabilities: http://0.0.0.0:%d/wd/hub/capabilities/all", options.getPort()));
    p("Running on: " + HostInfo.osInfo());
    p("Using java: " + HostInfo.javaVersion());
  }

  private void p(String msg) {
    System.out.println(msg);
    log.fine(msg);
  }

  private void initServer() {
    String host = System.getProperty("ios-driver.host");
    if (host == null) {
      host = "0.0.0.0";
    }
    server = new Server(new InetSocketAddress(host, options.getPort()));

    ServletContextHandler wd = new ServletContextHandler(server, "/wd/hub", true, false);
    wd.addServlet(
        new ServletHolder(new HttpServlet() {
          @Override
          protected void doGet(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            response.getWriter().print("{}");
            response.getWriter().close();
            p("Received /shutdown command. Shutting down.");
            new Thread() {
              @Override
              public void run() {
                try {
                  IOSServer.this.stop();
                } catch (Exception e) {
                  log.warning("Exception shutting down: " + e);
                  Runtime.getRuntime().exit(1);
                }
              }
            }.start();
          }
        }),
        "/shutdown");
    wd.addServlet(IOSServlet.class, "/*");

    wd.getServletContext().getContextHandler().setMaxFormContentSize(500000);
    wd.setAttribute(DRIVER, driver);
    wd.setAttribute(SERVER, this);

    ServletContextHandler extra = new ServletContextHandler(server, "/", true, false);
    extra.setAttribute(DRIVER, driver);

    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] {wd, extra});
    server.setHandler(handlers);
  }

  void start() throws Exception {
    if (!initialized) {
      initialized = true;
      initDriver();
      initServer();
    }
    if (!server.isRunning()) {
      server.start();
    }
  }

  public void stopGracefully() throws Exception {
    if (!initialized) {
      return;
    }
    if (driver != null) {
      driver.stopGracefully();
    }
    stop();
    log.info("server stopped");
  }

  private void stop() throws Exception {
    if (!initialized) {
      return;
    }
    if (driver != null) {
      try {
        driver.stop();
      } catch (Exception e) {
        log.warning("exception stopping: " + e);
      }
    }
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        log.warning("exception stopping: " + e);
      }
    }
  }
}
