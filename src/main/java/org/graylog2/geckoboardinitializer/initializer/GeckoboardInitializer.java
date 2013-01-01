/**
 * Copyright 2013 Lennart Koopmann <lennart@socketfeed.com>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.geckoboardinitializer.initializer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.graylog2.plugin.GraylogServer;
import org.graylog2.plugin.initializers.Initializer;
import org.graylog2.plugin.initializers.InitializerConfigurationException;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class GeckoboardInitializer implements Initializer {

    public static final String NAME = "Geckoboard API endpoint";
    
    public static final String DEFAULT_LISTEN_ADDRESS = "0.0.0.0";
    public static final int DEFAULT_PORT = 12210;
    
    public void initialize(GraylogServer server, Map<String, String> config) throws InitializerConfigurationException {
        final InetSocketAddress socketAddress = new InetSocketAddress(getListenAddress(config), getPort(config));

        final ExecutorService bossExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("geckoboard-boss-%d")
                .build());

        final ExecutorService workerExecutor = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("geckoboard-worker-%d")
                .build());

        final ServerBootstrap httpBootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(bossExecutor, workerExecutor)
        );
        httpBootstrap.setPipelineFactory(new PipelineFactory(server));

        try {
            httpBootstrap.bind(socketAddress);
            System.out.println("Started HTTP GELF server on " + socketAddress);
        } catch (final ChannelException e) {
            System.out.println("Could not bind HTTP GELF server to address " + socketAddress);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                httpBootstrap.releaseExternalResources();
            }
        });
    }

    public Map<String, String> getRequestedConfiguration() {
        Map<String, String> config = new HashMap<String, String>();
        
        config.put("listen_address", "Address to listen on. Default: " + DEFAULT_LISTEN_ADDRESS);
        config.put("port", "Port to listen on. Default: " + DEFAULT_PORT);
        
        return config;
    }

    public String getName() {
        return NAME;
    }

    public boolean masterOnly() {
        return false;
    }
    
    private String getListenAddress(Map<String, String> config) {
        if (config == null || config.get("listen_address") == null || config.get("listen_address").isEmpty()) {
            return DEFAULT_LISTEN_ADDRESS;
        }
        
        return config.get("listen_address");
    }
    
    private int getPort(Map<String, String> config) {
        if (config == null || config.get("port") == null || config.get("port").isEmpty()) {
            return DEFAULT_PORT;
        }

        return Integer.parseInt(config.get("port"));
    }

}
