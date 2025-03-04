package cydeo.utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class Driver {

    // Private constructor to prevent instantiation
    private Driver() {}

    // Thread-safe driver pool
    private static InheritableThreadLocal<WebDriver> driverPool = new InheritableThreadLocal<>();

    /*
     * Returns the same driver instance once we call it.
     * If an instance doesn't exist, it will create one.
     */
    public static WebDriver getDriver() {

        if(driverPool.get() == null){

            // Read the browserType from system property or configuration
            String browserType = (System.getProperty("BROWSER") == null)
                    ? ConfigurationReader.getProperty("browser")
                    : System.getProperty("BROWSER");
            System.out.println("Browser: " + browserType);

            switch (browserType){
                case "remote-chrome":
                    try {
                        // assign your grid server address
                        String gridAddress = "100.24.34.37";
                        URL url = new URL("http://" + gridAddress + ":4444/wd/hub");
                        ChromeOptions chromeOptions = new ChromeOptions();
                        chromeOptions.addArguments("--start-maximized");
                        driverPool.set(new RemoteWebDriver(url, chromeOptions));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "remote-firefox":
                    try {
                        // assign your grid server address
                        String gridAddress = "34.239.154.115";
                        URL url = new URL("http://" + gridAddress + ":4444/wd/hub");
                        FirefoxOptions remoteFirefoxOptions = new FirefoxOptions();
                        remoteFirefoxOptions.addArguments("--start-maximized");
                        driverPool.set(new RemoteWebDriver(url, remoteFirefoxOptions));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case "chrome":
                    driverPool.set(new ChromeDriver());
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;

                case "firefox":
                    // Disable Selenium Manager on unsupported architectures (e.g., ARM64)
                    System.setProperty("SELENIUM_MANAGER_DISABLE", "true");
                    // Specify the path to the manually installed GeckoDriver for ARM64
                    System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");

                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    // Explicitly set the Firefox binary to the one inside the snap package
                    firefoxOptions.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");

                    // Optionally adjust the log level if needed
                    firefoxOptions.setLogLevel(FirefoxDriverLogLevel.TRACE);
                    firefoxOptions.addArguments("--start-maximized");

                    driverPool.set(new FirefoxDriver(firefoxOptions));
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;

                case "headless-chrome":
                    ChromeOptions headlessOptions = new ChromeOptions();
                    headlessOptions.addArguments("--headless=new");
                    driverPool.set(new ChromeDriver(headlessOptions));
                    driverPool.get().manage().window().maximize();
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    break;

                default:
                    throw new RuntimeException("Browser type is not supported: " + browserType);
            }
        }

        return driverPool.get();
    }

    /*
     * Closes the driver instance and removes it from the thread-local storage.
     */
    public static void closeDriver(){
        if (driverPool.get() != null){
            driverPool.get().quit();
            driverPool.remove();
        }
    }
}
