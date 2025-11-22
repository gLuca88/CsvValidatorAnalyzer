package ReportConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {

	private static ExtentReports extent;

	// cartella base per tutti i report
	private static final String BASE_DIR = "test-output";

	// formattazione data
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	public static ExtentReports getInstance() {
		if (extent == null) {
			createInstance();
		}
		return extent;
	}

	private static void createInstance() {
		try {
			// nome cartella includendo data/ora
			String timestamp = sdf.format(new Date());
			String reportDirPath = BASE_DIR + "/Report_" + timestamp;

			Path reportDir = Paths.get(reportDirPath);
			Files.createDirectories(reportDir);

			// nome del file del report
			String reportFile = reportDirPath + "/ExecutionReport.html";
			ExtentSparkReporter spark = new ExtentSparkReporter(reportFile);

			spark.config().setReportName("API Test Report");
			spark.config().setDocumentTitle("CSVValidatorAnalyzer - Test Report");

			extent = new ExtentReports();
			extent.attachReporter(spark);

			extent.setSystemInfo("Tester", "Gianluca");
			extent.setSystemInfo("Ambiente", "Localhost");
			extent.setSystemInfo("Framework", "JUnit + RestAssured");

		} catch (Exception e) {
			throw new RuntimeException("Errore creazione ExtentReport", e);
		}
	}
}
