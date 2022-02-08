package bg.fmi.mjt.splitwise.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoggerFactoryTest {

    @Test
    void testGetLoggerThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> LoggerFactory.getLogger(null), "Did not throw on null argument");
    }

}