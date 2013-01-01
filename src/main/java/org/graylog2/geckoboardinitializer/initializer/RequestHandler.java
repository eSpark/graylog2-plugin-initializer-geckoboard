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

import org.graylog2.geckoboardinitializer.initializer.responsebuilders.TotalCountResponse;
import org.graylog2.geckoboardinitializer.initializer.responsebuilders.StreamCountResponse;
import org.graylog2.geckoboardinitializer.initializer.responsebuilders.StreamNotFoundException;
import org.graylog2.plugin.GraylogServer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class RequestHandler extends SimpleChannelHandler {
    
    public static final String NAMESPACE = "/geckoboard";
    
    private final GraylogServer server;
    
    public RequestHandler(GraylogServer server) {
        this.server = server;
    }
    
    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        HttpVersion httpRequestVersion = request.getProtocolVersion();

        if (request.getMethod() != HttpMethod.GET) {
            writeResponse(null, e.getChannel(), httpRequestVersion, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }

        ChannelBuffer buffer = request.getContent();
        byte[] message = new byte[buffer.readableBytes()];
        buffer.toByteBuffer().get(message, buffer.readerIndex(), buffer.readableBytes());
        
        QueryStringDecoder qsd = new QueryStringDecoder(request.getUri());
        
        if (!qsd.getPath().startsWith(NAMESPACE)) {
            writeResponse(null, e.getChannel(), httpRequestVersion, HttpResponseStatus.NOT_FOUND);
        } else {
            try {
                ResponseBuilder builder = getResponseBuilder(qsd);
                writeResponse(builder.build(qsd), e.getChannel(), httpRequestVersion, HttpResponseStatus.OK);
            } catch (NoResponseBuilderAvailableException ex) {
                writeResponse("Invalid type requested.", e.getChannel(), httpRequestVersion, HttpResponseStatus.NOT_FOUND);
            } catch (StreamNotFoundException ex) {
                writeResponse("Stream not found.", e.getChannel(), httpRequestVersion, HttpResponseStatus.NOT_FOUND);
            }
        }
    }

    private void writeResponse(String text, Channel channel, HttpVersion httpRequestVersion, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(httpRequestVersion, status);
        
        if (text != null) {
            response.setContent(ChannelBuffers.copiedBuffer(text, CharsetUtil.UTF_8));
        }
        
        channel.write(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    private ResponseBuilder getResponseBuilder(QueryStringDecoder qsd) throws NoResponseBuilderAvailableException {
        if (qsd.getPath().equals(NAMESPACE + "/stream_count")) {
            return new StreamCountResponse(server);
        }
        
        if (qsd.getPath().equals(NAMESPACE + "/total_count")) {
            return new TotalCountResponse(server);
        }
        
        throw new NoResponseBuilderAvailableException();
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        try {
            writeResponse("internal server error", e.getChannel(), HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            System.out.println("Could not handle Geckoboard request: " + e.getCause());
            e.getCause().printStackTrace(System.out);
        } catch (Exception ex) {
            System.out.println("Error while trying to handle an uncaught exception. " + e.getCause());
            e.getCause().printStackTrace(System.out);
        }
    }

}
