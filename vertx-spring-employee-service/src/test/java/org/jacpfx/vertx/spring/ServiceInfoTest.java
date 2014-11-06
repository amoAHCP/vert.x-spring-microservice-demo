package org.jacpfx.vertx.spring;

import io.netty.channel.EventLoopGroup;
import org.jacpfx.model.common.JSONTool;
import org.jacpfx.vertx.spring.services.RESTGetVerticleService;
import org.junit.Assert;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Context;
import org.vertx.java.core.Handler;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.dns.DnsClient;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.impl.DefaultEventBus;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.impl.DefaultHttpServer;
import org.vertx.java.core.impl.DefaultContext;
import org.vertx.java.core.impl.EventLoopContext;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.impl.DefaultNetServer;
import org.vertx.java.core.net.impl.ServerID;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.spi.Action;
import org.vertx.java.core.spi.cluster.ClusterManager;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by amo on 06.11.14.
 */
public class ServiceInfoTest {

    @Test
    public void testGetAllOperations() {
        RESTGetVerticleService service = new RESTGetVerticleService();
        service.setVertx(new TestVertx());
        service.start();
        //System.out.println(service.getServiceDescriptor().encodePrettily());
        Assert.assertTrue(service.getServiceDescriptor().getString("serviceName").equals("/service-REST-GET"));
        final JsonArray operations = service.getServiceDescriptor().getArray("operations");
        List<JsonObject> l = JSONTool.getObjectListFromArray(operations) ;


       // l.forEach(o-> System.out.println(o.encodePrettily()));


        String converted = service.getServiceDescriptor().encode();

        JsonObject tmp2 = new JsonObject(converted);
        final JsonArray operations1 = tmp2.getArray("operations");
        List<JsonObject> l1 = JSONTool.getObjectListFromArray(operations1) ;


        l1.forEach(o-> System.out.println(o.encodePrettily()));

    }


    private static class TestVertx implements VertxInternal{
        @Override
        public NetServer createNetServer() {
            return null;
        }

        @Override
        public NetClient createNetClient() {
            return null;
        }

        @Override
        public HttpServer createHttpServer() {
            return null;
        }

        @Override
        public HttpClient createHttpClient() {
            return null;
        }

        @Override
        public DatagramSocket createDatagramSocket(InternetProtocolFamily family) {
            return null;
        }

        @Override
        public SockJSServer createSockJSServer(HttpServer httpServer) {
            return null;
        }

        @Override
        public FileSystem fileSystem() {
            return null;
        }

        @Override
        public EventBus eventBus() {
            return new DefaultEventBus(this);
        }

        @Override
        public DnsClient createDnsClient(InetSocketAddress... dnsServers) {
            return null;
        }

        @Override
        public SharedData sharedData() {
            return null;
        }

        @Override
        public long setTimer(long delay, Handler<Long> handler) {
            return 0;
        }

        @Override
        public long setPeriodic(long delay, Handler<Long> handler) {
            return 0;
        }

        @Override
        public boolean cancelTimer(long id) {
            return false;
        }

        @Override
        public Context currentContext() {
            return null;
        }

        @Override
        public void runOnContext(Handler<Void> action) {

        }

        @Override
        public boolean isEventLoop() {
            return false;
        }

        @Override
        public boolean isWorker() {
            return false;
        }

        @Override
        public void stop() {

        }

        @Override
        public EventLoopGroup getEventLoopGroup() {
            return null;
        }

        @Override
        public ExecutorService getBackgroundPool() {
            return null;
        }

        @Override
        public DefaultContext startOnEventLoop(Runnable runnable) {
            return null;
        }

        @Override
        public DefaultContext startInBackground(Runnable runnable, boolean multiThreaded) {
            return null;
        }

        @Override
        public DefaultContext getOrCreateContext() {
            return null;
        }

        @Override
        public void reportException(Throwable t) {

        }

        @Override
        public Map<ServerID, DefaultHttpServer> sharedHttpServers() {
            return null;
        }

        @Override
        public Map<ServerID, DefaultNetServer> sharedNetServers() {
            return null;
        }

        @Override
        public DefaultContext getContext() {
            return null;
        }

        @Override
        public void setContext(DefaultContext context) {

        }

        @Override
        public EventLoopContext createEventLoopContext() {
            return null;
        }

        @Override
        public ClusterManager clusterManager() {
            return null;
        }

        @Override
        public <T> void executeBlocking(Action<T> action, Handler<AsyncResult<T>> asyncResultHandler) {

        }
    }
}
