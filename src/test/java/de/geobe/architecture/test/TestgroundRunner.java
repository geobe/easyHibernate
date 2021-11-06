package de.geobe.architecture.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestgroundRunner {

    String[] args = {};
    @Test
    @DisplayName("Run Testgroung")
    void coverageTest() {
        Testground.main(args);
    }
}
