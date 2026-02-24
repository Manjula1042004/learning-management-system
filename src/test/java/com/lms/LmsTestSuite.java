package com.lms;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("LMS Complete Test Suite")
@SelectPackages({
        "com.lms.controller",
        "com.lms.service",
        "com.lms.repository",
        "com.lms.security",
        "com.lms.config"
})
public class LmsTestSuite {
    // This class runs ALL tests in all packages!
}