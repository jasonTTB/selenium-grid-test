package cydeo.utilities;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class Driver {

    private Driver(){}

    private static InheritableThreadLocal<WebDriver> driverPool = new InheritableThreadLocal<>();

    public static WebDriver getDriver(){
        if(driverPool.get() == null){
            try {
                String browserType = System.getProperty("BROWSER") != null ? System.getProperty("BROWSER") : ConfigurationReader.getProperty("browser");
                System.out.println("Browser: " + browserType);

                switch (browserType){
                    case "chrome":
                        WebDriverManager.chromedriver().setup();
                        driverPool.set(new ChromeDriver());
                        break;
                    case "firefox":
                        String geckoDriverPath = "/snap/bin/geckodriver";
                        String firefoxBinaryPath = "/usr/bin/firefox";

                        System.setProperty("webdriver.gecko.driver", geckoDriverPath);
                        System.setProperty("webdriver.firefox.bin", firefoxBinaryPath);

                        FirefoxOptions options = new FirefoxOptions();
                        options.setBinary(firefoxBinaryPath);
                        options.addArguments("--headless");
                        options.addArguments("--no-sandbox");
                        options.addArguments("--disable-dev-shm-usage");

                        System.out.println("Setting up Firefox driver...");
                        System.out.println("GeckoDriver path: " + geckoDriverPath);
                        System.out.println("Firefox binary path: " + firefoxBinaryPath);

                        driverPool.set(new FirefoxDriver(options));
                        break;
                    // Add other browser cases as needed
                    default:
                        throw new RuntimeException("Unsupported browser type: " + browserType);
                }

                WebDriver driver = driverPool.get();
                driver.manage().window().maximize();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
            } catch (Exception e) {
                System.out.println("Failed to initialize WebDriver: " + e.getMessage());
                e.printStackTrace();
            }
        }

        WebDriver driver = driverPool.get();
        if (driver == null) {
            throw new RuntimeException("Driver was not initialized. Check previous logs for errors.");
        }
        return driver;
    }

    public static void closeDriver(){
        if (driverPool.get() != null){
            try {
                driverPool.get().quit();
            } catch (Exception e) {
                System.out.println("Error while closing the driver: " + e.getMessage());
            } finally {
                driverPool.remove();
            }
        }
    }
}
