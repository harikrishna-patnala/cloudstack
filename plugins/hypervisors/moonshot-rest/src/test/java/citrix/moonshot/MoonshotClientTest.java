package citrix.moonshot;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.naming.ConfigurationException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import citrix.moonshot.enums.HttpScheme;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Concurrent;

@RunWith(ConcurrentTestRunner.class)
public class MoonshotClientTest {

	@Rule
	public ConcurrentRule concurrently = new ConcurrentRule();

	private static final String USERNAME = "Administrator";

	private static final String PASSWORD = "password";

	private static final String HOST = "10.223.79.5";

	private static final HttpScheme SCHEME = HttpScheme.HTTP;

	private static final Integer PORT = 8080;

	private static MoonshotClient client;

	@BeforeClass
	public static void beforeClass() throws ConfigurationException {
		client = new MoonshotClient(USERNAME, PASSWORD, HOST, SCHEME.toString(), PORT);
	}

	@Before
	public void before() {
		client.setCredentials(USERNAME, PASSWORD); //each thread will add it's own credentials in the threadlocal in client
	}

	@Test
	@Concurrent(count = 5)
	public void test1() {
		test();
	}

	@Test
	@Concurrent(count = 5)
	public void test2() {
		test();
	}
	
	private void test() {
		System.out.println(Thread.currentThread().getName() + "  @@@@@@  " + client.getAllNodes());
	}

}
