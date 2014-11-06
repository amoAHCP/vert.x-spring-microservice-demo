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
        pm.deployVerticle("spring:org.jacpfx.vertx.spring.services.RESTGetVerticleService",
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
    public  void testSimpleRESTGETWithParameterPath() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeFour/123/employee/andy", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "123:andy");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }

    @Test
    public  void testSimpleRESTGETWithParameterPath2() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeThree/456/andy", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "456:andy");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }

    @Test
    public  void testSimpleRESTGETWithParameterPathError() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeFour/123/andy", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "no route found");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }

    @Test
    public  void testSimpleRESTGETWithQueryParameter() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeOne?name=789&lastname=andy", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response: " + body.toString());
                    Assert.assertEquals(body.toString(), "789:andy");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }

    @Test
    public  void testSimpleRESTGETWithQueryParameterAndReturnValue() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeTwo?id=123", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response123: " + body.toString());
                    Assert.assertEquals(body.toString(), "{\n" +
                            "  \"id\" : \"123\"\n" +
                            "}");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }

    @Test
    public  void testSimpleRESTGETWithQueryParameterAndObjectReturnValue() throws InterruptedException, MalformedURLException {
        connectMain(1);
        CountDownLatch latch = new CountDownLatch(1);


        HttpClientRequest request = getClient().get("/service-REST-GET/testEmployeeFive?id=123", new Handler<HttpClientResponse>() {
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(body -> {System.out.println("Got a response123: " + body.toString());
                    Assert.assertEquals(body.toString(), "{\"employeeId\":\"fg\",\"jobDescription\":\"dfg\",\"jobType\":\"dfg\",\"firstName\":\"fdg\",\"lastName\":\"dfg\"}");});

                latch.countDown();
            }
        });

        request.end();
        latch.await();
    }
}
