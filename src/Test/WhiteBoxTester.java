package Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
//import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ClientTester.class,
	ConversationTest.class, 
	ServerUserTester.class,
	MessageTester.class
})
public class WhiteBoxTester {
}
