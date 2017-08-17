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

package com.google.devtoolsdriver.devtools;

import static com.google.devtoolsdriver.devtools.DevtoolsDomain.NETWORK;

import javax.json.JsonObject;

/**
 * Factory for messages in the devtools Network domain. For a specification of this domain's
 * methods, see the <a href="https://chromedevtools.github.io/debugger-protocol-viewer/tot/Network/"
 * >debugging protocol viewer</a>. Note that not all the domain's methods have been implemented yet.
 */
public final class Network {
  private static DevtoolsCommand command(String methodSuffix) {
    return new DevtoolsCommand.NoOptionals(NETWORK.methodName(methodSuffix));
  }

  /** A fluent Devtools command exposing the ability to add optional properties */
  public static final class EnableCommand extends DevtoolsCommand.WithOptionals<EnableCommand> {
    private EnableCommand() {
      super(NETWORK.methodName("enable"));
    }

    private EnableCommand(JsonObject params) {
      super(NETWORK.methodName("enable"), params);
    }

    public EnableCommand withMaxTotalBufferSize(int maxTotalBufferSize) {
      return with("maxTotalBufferSize", maxTotalBufferSize);
    }

    public EnableCommand withMaxResourceBufferSize(int maxResourceBufferSize) {
      return with("maxResourceBufferSize", maxResourceBufferSize);
    }

    @Override
    EnableCommand create(JsonObject params) {
      return new EnableCommand(params);
    }
  }

  public static EnableCommand enable() {
    return new EnableCommand();
  }

  public static DevtoolsCommand disable() {
    return command("disable");
  }

  public static DevtoolsCommand setUserAgentOverride(String userAgent) {
    return command("setUserAgentOverride").with("userAgent", userAgent);
  }

  public static DevtoolsCommand getResponseBody(String requestId) {
    return command("getResponseBody").with("requestId", requestId);
  }

  public static DevtoolsCommand addBlockedUrl(String url) {
    return command("addBlockedUrl").with("url", url);
  }

  public static DevtoolsCommand removeBlockedUrl(String url) {
    return command("removeBlockedUrl").with("url", url);
  }

  public static DevtoolsCommand replayXHR(String requestId) {
    return command("replayXHR").with("requestId", requestId);
  }

  public static DevtoolsCommand setMonitoringXHREnabled(boolean enabled) {
    return command("setMonitoringXHREnabled").with("enabled", enabled);
  }

  public static DevtoolsCommand canClearBrowserCache() {
    return command("canClearBrowserCache");
  }

  public static DevtoolsCommand clearBrowserCache() {
    return command("clearBrowserCache");
  }

  public static DevtoolsCommand canClearBrowserCookies() {
    return command("canClearBrowserCookies");
  }

  public static DevtoolsCommand clearBrowserCookies() {
    return command("clearBrowserCookies");
  }

  /** Moved from Page in the latest tip-of-tree */
  public static DevtoolsCommand getCookies() {
    return command("getCookies");
  }

  /** Moved from Page in the latest tip-of-tree */
  public static DevtoolsCommand deleteCookie(String cookieName, String url) {
    return command("deleteCookie").with("cookieName", cookieName).with("url", url);
  }

  public static DevtoolsCommand canEmulateNetworkConditions() {
    return command("canEmulateNetworkConditions");
  }

  public static DevtoolsCommand emulateNetworkConditions(
      boolean offline, long latency, long downloadThroughput, long uploadThroughput) {
    return command("emulateNetworkConditions")
        .with("offline", offline)
        .with("latency", latency)
        .with("downloadThroughput", downloadThroughput)
        .with("uploadThroughput", uploadThroughput);
  }

  public static DevtoolsCommand setCacheDisabled(boolean cacheDisabled) {
    return command("addCacheDisabled").with("cacheDisabled", cacheDisabled);
  }

  public static DevtoolsCommand setBypassServiceWorker(boolean bypass) {
    return command("setBypassServiceWorker").with("bypass", bypass);
  }

  public static DevtoolsCommand setDataSizeLimitsForTest(int maxTotalSize, int maxResourceSize) {
    return command("setDataSizeLimitsForTest")
        .with("maxTotalSize", maxTotalSize)
        .with("maxResourceSize", maxResourceSize);
  }

  public static DevtoolsCommand getCertificateDetails(int certificateId) {
    return command("getCertificateDetails").with("certificateId", certificateId);
  }

  public static DevtoolsCommand showCertificateViewer(int certificateId) {
    return command("showCertificateViewer").with("certificateId", certificateId);
  }

  private Network() {}
}
