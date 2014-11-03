package integration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by amo on 30.10.14.
 */
public class RestGetTest {
    static final PlatformManager pm = PlatformLocator.factory.createPlatformManager();

    @BeforeClass
    public static void init() {
        System.setProperty("vertx.langs.spring", "org.jacpfx.vertx.spring~vertx-spring-mod~1.0-SNAPSHOT:org.jacpfx.vertx.spring.SpringVerticleFactory");

    }

    private static PlatformManager connectMain(int instances) throws MalformedURLException, InterruptedException {
        System.out.println(System.getProperty("vertx.langs.spring"));
        final CountDownLatch waitForDeploy = new CountDownLatch(2);
        pm.deployModule("org.jacpfx.vertx.spring~vertx-spring-mongo-application~1.0-SNAPSHOT",
                null,
                instances,
                (event) -> {
                    if (event.succeeded()) waitForDeploy.countDown();
                });
        pm.deployVerticle("spring:org.jacpfx.vertx.spring.services.EmployeeVerticleService",
                null,
                new URL[]{new File(".").toURI().toURL()},
                instances,
                null,
                (event) -> {
                    if (event.succeeded()) waitForDeploy.countDown();
                });
        waitForDeploy.await(10000, TimeUnit.MILLISECONDS);
        return pm;

    }

    private HttpClient getClient() {

        Vertx vertx = VertxFactory.newVertx();
        HttpClient client = vertx.
                createHttpClient().
                setHost("localhost").
                setPort(8080);

        return client;
    }



    @Test
    public  void testSimpleRESTGET() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/testEmployeeFour/123/employee/andy", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "123:andy");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }
}
