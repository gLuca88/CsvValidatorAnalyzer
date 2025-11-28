package Runners;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import TestAdmin.TestAdminProtected_EndPointUpDate;
import TestAdmin.TestAdminProtected_EndPoint_IsProtectedAdmin;
import TestAdmin.TestLoginAdmin;

@Suite
@SelectClasses({ TestLoginAdmin.class, TestAdminProtected_EndPointUpDate.class,
		TestAdminProtected_EndPoint_IsProtectedAdmin.class })
public class AdminTestSuite {

}
