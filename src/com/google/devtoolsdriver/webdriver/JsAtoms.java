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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Verify;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;

/**
 * Utilities for constructing JavaScript expressions that invoke the Browser Automation Atoms.
 *
 * @see "https://github.com/SeleniumHQ/selenium/wiki/Automation-Atoms"
 * @see "https://github.com/SeleniumHQ/selenium/tree/master/javascript/atoms"
 */
public class JsAtoms {
  private static final String FRAGMENT_PATH_FORMAT =
      "third_party/javascript/browser_automation/bot/fragments/%s_ios.js";
  private static final Joiner ARG_JOINER = Joiner.on(',');

  @VisibleForTesting
  enum JsFunction {
    BACK(0),
    CLEAR(1),
    FORWARD(0),
    GET_EFFECTIVE_STYLE(2),
    GET_INTERACTABLE_SIZE(1),
    GET_LOCATION(1),
    GET_SIZE(1),
    GET_VISIBLE_TEXT(1),
    IS_ENABLED(1),
    IS_SELECTED(1),
    IS_SHOWN(1),
    MOVE_MOUSE(1),
    STRINGIFY(1),
    SUBMIT(1),
    TAP(1),
    TYPE(2),
    XPATH(2),
    XPATHS(2);

    private final int numArgs;
    private final String fragment;

    private JsFunction(int numArgs) {
      this.numArgs = numArgs;
      String fragmentPath = String.format(FRAGMENT_PATH_FORMAT, name().toLowerCase());
      URL fragmentUrl = Resources.getResource(fragmentPath);
      try {
        fragment = Resources.toString(fragmentUrl, UTF_8);
      } catch (IOException e) {
        // If we cannot read resources from the jar, no recovery is possible.
        throw new IllegalStateException(e);
      }
    }

    @VisibleForTesting
    int numArgs() {
      return numArgs;
    }

    @VisibleForTesting
    String call(String... args) {
      Verify.verify(args.length == numArgs);
      return fragment + '(' + ARG_JOINER.join(args) + ')';
    }
  }

  /** Calls {@code bot.window.back()}. */
  public static String back() {
    return JsFunction.BACK.call();
  }

  /** Calls {@code bot.action.clear(elementExpr)}. */
  public static String clear(String elementExpr) {
    return JsFunction.CLEAR.call(elementExpr);
  }

  /** Calls {@code bot.window.forward()}. */
  public static String forward() {
    return JsFunction.FORWARD.call();
  }

  /** Calls {@code bot.dom.getEffectiveStyle(elementExpr, propertyName)}. */
  public static String getEffectiveStyle(String elementExpr, String propertyName) {
    return JsFunction.GET_EFFECTIVE_STYLE.call(elementExpr, propertyName);
  }

  /** Calls {@code bot.window.getInteractableSize(windowExpr)}. */
  public static String getInteractableSize(String windowExpr) {
    return JsFunction.GET_INTERACTABLE_SIZE.call(windowExpr);
  }

  /** Calls {@code bot.fragments.getLocation(elementExpr)}. */
  public static String getLocation(String elementExpr) {
    return JsFunction.GET_LOCATION.call(elementExpr);
  }

  /** Calls {@code bot.fragments.getSize(elementExpr)}. */
  public static String getSize(String elementExpr) {
    return JsFunction.GET_SIZE.call(elementExpr);
  }

  /** Calls {@code bot.dom.getVisibleText(elementExpr)}. */
  public static String getVisibleText(String elementExpr) {
    return JsFunction.GET_VISIBLE_TEXT.call(elementExpr);
  }

  /** Calls {@code bot.dom.isEnabled(elementExpr)}. */
  public static String isEnabled(String elementExpr) {
    return JsFunction.IS_ENABLED.call(elementExpr);
  }

  /** Calls {@code bot.dom.isSelected(elementExpr)}. */
  public static String isSelected(String elementExpr) {
    return JsFunction.IS_SELECTED.call(elementExpr);
  }

  /** Calls {@code bot.dom.isShown(elementExpr)}. */
  public static String isShown(String elementExpr) {
    return JsFunction.IS_SHOWN.call(elementExpr);
  }

  /** Calls {@code bot.action.moveMouse(elementExpr)}. */
  public static String moveMouse(String elementExpr) {
    return JsFunction.MOVE_MOUSE.call(elementExpr);
  }

  /** Calls {@code bot.json.stringify(objectExpr)}. */
  public static String stringify(String objectExpr) {
    return JsFunction.STRINGIFY.call(objectExpr);
  }

  /** Calls {@code bot.action.submit(elementExpr)}. */
  public static String submit(String elementExpr) {
    return JsFunction.SUBMIT.call(elementExpr);
  }

  /** Calls {@code bot.action.tap(elementExpr)}. */
  public static String tap(String elementExpr) {
    return JsFunction.TAP.call(elementExpr);
  }

  /** Calls {@code bot.action.type(elementExpr, valueExpr)}. */
  public static String type(String elementExpr, String valueExpr) {
    return JsFunction.TYPE.call(elementExpr, valueExpr);
  }

  /** Calls {@code bot.locators.xpath.single(xpathExpr, elementExpr)}. */
  public static String xpath(String xpathExpr, String elementExpr) {
    return JsFunction.XPATH.call(xpathExpr, elementExpr);
  }

  /** Calls {@code bot.locators.xpath.many(xpathExpr, elementExpr)}. */
  public static String xpaths(String xpathExpr, String elementExpr) {
    return JsFunction.XPATHS.call(xpathExpr, elementExpr);
  }

  private JsAtoms() {}
}
