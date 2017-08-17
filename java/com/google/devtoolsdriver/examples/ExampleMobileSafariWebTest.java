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

package com.google.devtoolsdriver.examples;

import com.google.iosdevicecontrol.util.FluentLogger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * This class contains an example use case of the DevTools Driver library for automating iOS devices
 * running on a Selenium server.
 */
public class ExampleMobileSafariWebTest {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static WebDriver driver;

  public static void main(String[] args) throws Exception {
    // Create a DesiredCapabilities object to request specific devices from the WebDriver server.
    // A udid can be optionally specified, otherwise an arbitrary device is chosen.
    DesiredCapabilities caps = new DesiredCapabilities();
    // caps.setCapability("uuid", udid);
    // Start a WebDriver session. The local machine has to be running the SafariDriverServer, or
    // change localhost below to an IP running the SafariDriverServer.
    driver = new RemoteWebDriver(new URL("http://localhost:5555/wd/hub"), caps);
    // Connect to a URL
    driver.get("http://www.google.com");

    // Interact with the web page. In this example use case, the Webdriver API is used to find
    // specific elements, test a google search and take a screenshot.
    driver.findElement(By.id("hplogo"));

    // Google New York
    WebElement mobileSearchBox = driver.findElement(By.id("lst-ib"));
    mobileSearchBox.sendKeys("New York");
    WebElement searchBox;
    try {
      searchBox = driver.findElement(By.id("tsbb"));
    } catch (NoSuchElementException e) {
      searchBox = driver.findElement(By.name("btnG"));
    }
    searchBox.click();

    takeScreenshot();
    driver.navigate().refresh();
    takeScreenshot();

    // Quit the WebDriver instance on completion of the test.
    driver.quit();
    driver = null;
  }

  private static void takeScreenshot() throws Exception {
    byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    Path screenshotPath = Files.createTempFile("screenshot", ".png");
    Files.write(screenshotPath, screenshotBytes);
    logger.atInfo().log("Screenshot written to: %s", screenshotPath);
  }
}
