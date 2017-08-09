# DevTools Driver

DevTools Driver is a Java framework for creating a WebDriver server
implementation for any browser that supports the [DevTools Remote Debugging
Protocol](https://chromedevtools.github.io/devtools-protocol/), including any
browser based on the WebKit or Blink browser engines. Implementations for such
browsers can be created simply by providing a few relatively simple hook
methods, and the framework handles the rest, largely through JavaScript
injection of [browser automation
atoms](https://github.com/SeleniumHQ/selenium/wiki/Automation-Atoms) over
DevTools.

This project includes a WebDriver implementation for Mobile Safari on iOS. This
implementation, which works for both iOS simulators and real devices, uses the
[iOS Device Control library](https://github.com/google/ios-device-control) to
control devices. An example of a web test controlling a remote Mobile Safari
Devtools Driver can be found in [ExampleMobileSafariWebTest.java](src/com/google/devtoolsdriver/examples/ExampleMobileSafariWebTest.java).

## Installation

1.  Before assembling a runnable jar for a Selenium server, the iOS Device
    Control dependency has to be installed. This can be done by following the
    steps outlined [here](https://github.com/google/ios-device-control).

2.  The library must then be installed into the local Maven repository:

    ```console
    git clone https://github.com/google/ios-device-control.git
    cd ios-device-control/
    mvn install
    ```

3.  A runnable jar of a Mobile Safari capable Selenium server can be assembled
    (along with the example provided) by running:

    ```console
    git clone https://github.com/google/devtools-driver
    cd devtools-driver/
    mvn assembly:assembly
    ```

4.  Run the assembled jar found at
    target/SafariDriverServer-jar-with-dependencies.jar on a Mac:

    ```console
    # The -simulator flag indicates that all requested WebDriver instances will be of an iOS Simulator. Omit it for real devices
    java -jar SafariDriverServer-jar-with-dependencies.jar -simulator
    ```

## License

DevTools Driver is licensed under the open-source [Apache 2.0 license](LICENSE)

## Contributing

Please [see the guidelines for contributing](CONTRIBUTING.md) before creating
pull requests
