package ReportConfig;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class ReportExtension implements BeforeAllCallback, AfterAllCallback, TestWatcher {

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtentManager.getInstance();
        System.out.println(">>> ExtentReport inizializzato");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ExtentManager.getInstance().flush();
        System.out.println(">>> ExtentReport salvato");
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        ExtentTestManager.getTest().pass("✔ Test passato: " + context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        ExtentTestManager.getTest().fail("❌ Test fallito: " + cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        ExtentTestManager.getTest().skip("⚠ Test disabilitato");
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        ExtentTestManager.getTest().skip("⚠ Test interrotto: " + cause.getMessage());
    }
}
