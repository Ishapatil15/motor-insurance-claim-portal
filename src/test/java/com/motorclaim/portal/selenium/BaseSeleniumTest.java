package com.motorclaim.portal.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base Selenium Test class configuring WebDriver in headless Chrome mode
 * and providing automatic screenshot capture on test failures.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(BaseSeleniumTest.ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    protected static WebDriver driver;
    protected String baseUrl;

    @BeforeAll
    static void setupClass() {
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            System.err.println("WebDriverManager auto-setup fallback: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUpDriver() {
        baseUrl = "http://localhost:" + port;

        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
        }
    }

    @AfterEach
    void tearDown() {
        // Driver can be retained across ordered tests
    }

    @AfterAll
    static void closeDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {
        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            boolean failed = context.getExecutionException().isPresent();
            if (failed && driver instanceof TakesScreenshot) {
                TakesScreenshot camera = (TakesScreenshot) driver;
                File screenshot = camera.getScreenshotAs(OutputType.FILE);
                String testName = context.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_");
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path destinationDir = Paths.get("target", "screenshots");
                try {
                    Files.createDirectories(destinationDir);
                    Path destination = destinationDir.resolve(testName + "_" + timestamp + ".png");
                    Files.copy(screenshot.toPath(), destination);
                    System.out.println("FAILURE SCREENSHOT CAPTURED: " + destination.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Failed to write failure screenshot: " + e.getMessage());
                }
            }
        }
    }
}
