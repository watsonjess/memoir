package com.makers.memoir.feature;

import com.makers.memoir.model.User;
import com.makers.memoir.repository.UserRepository;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class LandingFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchPlaywright() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void createTestUser() {
        if (userRepository.findUserByEmail(TEST_EMAIL).isEmpty()) {
            User user = new User();
            user.setEmail(TEST_EMAIL);
            user.setUsername("testuser");
            user.setFirstname("Test");
            user.setLastname("User");
            userRepository.save(user);
        }
    }

    @BeforeEach
    void createContextAndPage() {
        if (browser == null || !browser.isConnected()) {
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(List.of(
                                    "--disable-web-security",
                                    "--no-sandbox",
                                    "--disable-features=IsolateOrigins",
                                    "--disable-site-isolation-trials"
                            ))
            );
        }
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(true)
        );
        page = context.newPage();
        page.setDefaultTimeout(60000);
    }

    @AfterEach
    void cleanUp() {
        userRepository.findUserByEmail(TEST_EMAIL)
                .ifPresent(userRepository::delete);
    }

    @Test
    void unauthenticatedUserSeesLandingPage() {
        page.navigate("http://localhost:8080");

        assertThat(page.locator(".l-brand")).containsText("Memoir");
        assertThat(page.locator(".l-btn-outline")).isVisible();
        assertThat(page.locator(".l-title")).isVisible();
    }

    @Test
    void unauthenticatedUserIsShownLandingTemplate() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("landing"));
    }

    @Test
    void authenticatedUserIsShownDashboard() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token
                                        .claim("email", TEST_EMAIL)
                                        .claim("given_name", "Test")
                                        .claim("family_name", "User"))))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void authenticatedUserCanAccessGroups() throws Exception {
        mockMvc.perform(get("/groups")
                        .with(oidcLogin()
                                .idToken(token -> token
                                        .claim("email", TEST_EMAIL))))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserIsRedirectedFromProtectedPage() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().is3xxRedirection());
    }
}