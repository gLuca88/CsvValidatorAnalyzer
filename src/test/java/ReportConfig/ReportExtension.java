package ReportConfig;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ReportExtension implements BeforeAllCallback, AfterAllCallback {
	
	   @Override
	    public void beforeAll(ExtensionContext context) {
	        // Inizializza il report una sola volta
	        ExtentManager.getInstance();
	        System.out.println(">>> ExtentReport inizializzato");
	    }

	    @Override
	    public void afterAll(ExtensionContext context) {
	        // Chiude e salva il report
	        ExtentManager.getInstance().flush();
	        System.out.println(">>> ExtentReport salvato");
	    }

}
