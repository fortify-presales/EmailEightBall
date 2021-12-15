package com.microfocus.app;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

class BaseTest {

    @Autowired
    private Gson gson;

    protected void run() {
        // uncomment for random failing tests!
        /*double r = Math.random();
        if (r < 0.1) {
            fail("oops");
        } else if (r < 0.2) {
            throw new AssumptionViolatedException("skipping");
        }*/
    }

}
