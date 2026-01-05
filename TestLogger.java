import com.universallogger.UniversalLogger;
import org.slf4j.Logger;

public class TestLogger {
    public static void main(String[] args) {
        System.out.println("=== Test 1: Auto initialization ===");
        try {
            Logger logger = UniversalLogger.getLogger(TestLogger.class);
            logger.info("Test message 1 - auto initialization");
            System.out.println("[OK] Test 1 passed");
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1 failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Test 2: getInstance() usage ===");
        try {
            var instance = UniversalLogger.getInstance();
            Logger logger = instance.getLogger(TestLogger.class);
            logger.info("Test message 2 - getInstance()");
            System.out.println("[OK] Test 2 passed");
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2 failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Test 3: Different log levels ===");
        try {
            Logger logger = UniversalLogger.getLogger(TestLogger.class);
            logger.trace("Trace message");
            logger.debug("Debug message");
            logger.info("Info message");
            logger.warn("Warning message");
            logger.error("Error message");
            System.out.println("[OK] Test 3 passed");
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3 failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== All tests completed ===");
    }
}
