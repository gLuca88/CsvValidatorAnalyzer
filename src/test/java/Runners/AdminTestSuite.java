package Runners;



import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


import TestAdmin.TestLoginAdmin;


@Suite
@SelectClasses({
        TestLoginAdmin.class,  
})
public class AdminTestSuite {
    
}
