package Runners;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import TestAdmin.TestAdminProtected_EndPointUpDate;
import TestAdmin.TestLoginAdmin;

@Suite
@SelectClasses({ TestLoginAdmin.class, TestAdminProtected_EndPointUpDate.class })
public class AdminTestSuite {

}
