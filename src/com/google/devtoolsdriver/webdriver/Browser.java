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

package com.google.devtoolsdriver.webdriver;

import com.google.common.collect.ImmutableList;
import com.google.devtoolsdriver.devtools.DevtoolsCommand;
import com.google.devtoolsdriver.devtools.DevtoolsEvent;
import com.google.devtoolsdriver.devtools.DevtoolsResult;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * An strategy to interact with a browser for the purpose of providing a WebDriver implementation.
 */
public interface Browser extends AutoCloseable {
  /** Returns the id of the currently active page in the browser. */
  PageId activePage();

  /** Returns a list of {@link PageId} for every open page. */
  ImmutableList<PageId> listPages() throws BrowserException;

  /**
   * Switches the browser to the specified pageId; a noop if {@code pageId} identifies the page that
   * is already active. Returns whether the specified page differed from the active page.
   */
  boolean switchTo(PageId pageId) throws BrowserException;

  /** Send a devtools command to the browser. */
  DevtoolsResult sendCommand(DevtoolsCommand command, Duration timeout) throws BrowserException;

  /** Set a listener for devtools events */
  void addEventListener(Consumer<DevtoolsEvent> listener);

  /** Takes a screenshot in PNG format and returns it as a byte array. */
  byte[] takeScreenshot() throws BrowserException;

  /** Returns the WebDriver "browserName" capability for this browser. */
  String browserName();

  /** Returns the WebDriver "platformName" capability for this browser. */
  String platformName();

  @Override
  void close() throws BrowserException;
}
