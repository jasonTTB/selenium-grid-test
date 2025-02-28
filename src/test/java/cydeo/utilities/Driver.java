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

            String browserType = System.getProperty("BROWSER") != null ? System.getProperty("BROWSER") : ConfigurationReader.getProperty("browser");
            System.out.println("Browser: " + browserType);

            switch (browserType){
                case "remote-chrome":
                    // ... (keep existing remote-chrome code)
                    break;
                case "remote-firefox":
                    // ... (keep existing remote-firefox code)
                    break;
                case "chrome":
                    // ... (keep existing chrome code)
                    break;
                case "firefox":
                    String geckoDriverPath = "/usr/local/bin/geckodriver"; // Update this path
                    String firefoxBinaryPath = "/usr/bin/firefox-esr";     // Update this path if necessary

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

                    try {
                        driverPool.set(new FirefoxDriver(options));
                        driverPool.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
                    } catch (Exception e) {
                        System.out.println("Failed to initialize Firefox driver: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "headless-chrome":
                    // ... (keep existing headless-chrome code)
                    break;
            }
        }

        return driverPool.get();
    }

    public static void closeDriver(){
        if (driverPool.get()!=null){
            driverPool.get().quit();
            driverPool.remove();
        }
    }
}
