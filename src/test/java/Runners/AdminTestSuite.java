package Runners;



import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import TestAdmin.TestLogOut;
import TestAdmin.TestLoginAdmin;
import TestAdmin.TestModificaProfiloAdminProtetto;
import TestAdmin.TestRegistrazioneAdmin;

@Suite
@SelectClasses({
        TestLoginAdmin.class,
        TestLogOut.class,
        TestRegistrazioneAdmin.class,
        TestModificaProfiloAdminProtetto.class
})
public class AdminTestSuite {
    // NON serve nulla qui dentro
}
