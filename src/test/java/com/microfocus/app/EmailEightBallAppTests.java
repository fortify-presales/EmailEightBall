package com.microfocus.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
		properties = {
				"app.operation=test"
		}
)
class EmailEightBallAppTests extends BaseTest {

	private Gson gson = new GsonBuilder().create();

	@Test
	void contextLoads() {
	}

	@Test public void eightBallTest1() {
		run();
	}

	@Test public void eightBallTest2() {
		run();
	}

	@Test public void eightBallTest3() {
		run();
	}

	@Test public void eightBallTest4() {
		run();
	}

	@Test public void eightBallTest5() {
		run();
	}
}
