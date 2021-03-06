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
package org.graylog2.geckoboardinitializer.initializer.responsebuilders;

import java.util.List;
import java.util.Map;
import org.graylog2.geckoboardinitializer.initializer.ResponseBuilder;
import org.graylog2.plugin.GraylogServer;
import org.graylog2.plugin.Tools;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class StreamCountResponse implements ResponseBuilder {

    public static final int STANDARD_TIMEFRAME = 5*60;
    
    private GraylogServer server;

    public StreamCountResponse(GraylogServer server) {
        this.server = server;
    }
    
    /*
     * "Number + optional secondary stat"
     * 
     * http://docs.geckoboard.com/custom-widgets/number.html
     */
    public String build(QueryStringDecoder qsd) throws StreamNotFoundException {
        String streamId = ResponseBuilderTools.customParameterFromQueryParameters(qsd, "stream_id");
        int timeframe = ResponseBuilderTools.timeframeFromQueryParameters(qsd);
        int since = Tools.getUTCTimestamp()-timeframe;
        
        if (!server.getEnabledStreams().containsKey(streamId)) {
            throw new StreamNotFoundException();
        }

        // We are only adding an Integer here. No real Map to JSON stuff required.
        return "{\"item\":[{\"text\":\"\",\"value\": " + server.getMessageGateway().streamMessageCount(streamId, since) + "}]}";
    }
    
}
