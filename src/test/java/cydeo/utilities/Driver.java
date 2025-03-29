package cydeo.utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
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
                        // Assign your grid server address
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
                        // Assign your grid server address
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
                    ChromeOptions chromeOptions = new ChromeOptions();

                    // Enable headless mode with better stability
                    chromeOptions.addArguments("--headless=new");

                    // Optimize performance and avoid sandbox issues
                    chromeOptions.addArguments("--disable-gpu");
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");

                    // Ensure proper screen rendering in headless mode
                    chromeOptions.addArguments("--window-size=1920,1080");
                    chromeOptions.addArguments("--force-device-scale-factor=1");
                    chromeOptions.addArguments("--disable-blink-features=AutomationControlled");

                    // Set the ChromeDriver with configured options
                    driverPool.set(new ChromeDriver(chromeOptions));

                    // Set timeouts and ensure elements are visible before interaction
                    driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                    driverPool.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                    driverPool.get().manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

                    break;

                case "firefox":
                    // Disable Selenium Manager for manual GeckoDriver
                    System.setProperty("SELENIUM_MANAGER_DISABLE", "true");

                    // Specify the path to the manually installed GeckoDriver
                    String geckoDriverPath = "/usr/local/bin/geckodriver";
                    if (!new File(geckoDriverPath).exists()) {
                        throw new IllegalStateException("GeckoDriver not found at: " + geckoDriverPath);
                    }
                    System.setProperty("webdriver.gecko.driver", geckoDriverPath);

                    // Initialize Firefox options
                    FirefoxOptions firefoxOptions = new FirefoxOptions();

                    // Set the Firefox binary location (ensure Firefox is installed via the package manager)
                    String firefoxBinaryPath = "/usr/bin/firefox";
                    if (!new File(firefoxBinaryPath).exists()) {
                        throw new IllegalStateException("Firefox binary not found at: " + firefoxBinaryPath);
                    }
                    firefoxOptions.setBinary(firefoxBinaryPath);

                    // Set log level to TRACE for detailed logs
                    firefoxOptions.setLogLevel(FirefoxDriverLogLevel.TRACE);

                    // Check if headless mode is required via system property
                    boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "true"));
                    if (isHeadless) {
                        firefoxOptions.addArguments("--headless");
                        firefoxOptions.addArguments("--window-size=1920,1080"); // Set resolution for headless
                    }

                    // Set a custom user-agent to mimic real browser usage
                    firefoxOptions.addPreference("general.useragent.override",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Gecko/20100101 Firefox/119.0");

                    // Initialize the Firefox driver
                    driverPool.set(new FirefoxDriver(firefoxOptions));

                    // No need to maximize window in headless mode
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
