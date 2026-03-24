package com.makers.memoir.feature;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LandingFeatureTest {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        //browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500)
        );
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void unauthenticatedUserSeesLandingPage() {
        page.navigate("http://localhost:8080");

        assertThat(page.locator(".l-brand")).containsText("Memoir");
        assertThat(page.locator(".l-btn-outline")).isVisible();
        assertThat(page.locator(".l-title")).isVisible();
    }

    @Test
    void userCanLoginAndSeesDashboard() {
        page.navigate("http://localhost:8080");

        // Click sign in
        page.click(".l-btn-outline");

        // Wait for Auth0
        //page.waitForURL("**auth0.com**");
        page.waitForSelector("input[name='username']");

        // Fill in credentials
        page.fill("input[name='username']", "test@test.com");
        page.fill("input[name='password']", "Test_1234");
        page.click("button[type='submit']");

        // Wait for redirect back to app
        page.waitForURL("http://localhost:8080/");

        // Assert we're on the dashboard
        assertThat(page.locator(".memoir-nav")).isVisible();
        assertThat(page.locator("body")).containsText("Your Moments");
    }
}