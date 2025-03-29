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
                    // For headless environments like Jenkins, run Firefox in headless mode
                    System.setProperty("SELENIUM_MANAGER_DISABLE", "true");

                    // Specify the path to the manually installed GeckoDriver
                    System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");

                    // Initialize Firefox options
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    // Set the Firefox binary location (ensure Firefox is installed via the package manager)
                    firefoxOptions.setBinary("/usr/bin/firefox");

                    // Set log level to TRACE for detailed logs
                    //firefoxOptions.setLogLevel(FirefoxDriverLogLevel.TRACE);

                    // Run Firefox in headless mode to prevent display issues on CI servers
                    firefoxOptions.addArguments("--headless");

                    // Initialize the Firefox driver
                    driverPool.set(new FirefoxDriver(firefoxOptions));
                    // No need to maximize window in headless mode
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
