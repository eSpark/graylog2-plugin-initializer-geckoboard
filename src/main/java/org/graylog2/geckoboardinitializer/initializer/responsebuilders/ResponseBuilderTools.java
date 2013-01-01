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
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class ResponseBuilderTools {
    
    public static final int STANDARD_TIMEFRAME = 5*60;
    
    public static int timeframeFromQueryParameters(QueryStringDecoder qsd) {
        Map<String, List<String>> params = qsd.getParameters();
        
        if (params != null) {
            if (params.containsKey("timeframe") && !params.get("timeframe").get(0).isEmpty()) {
                return Integer.parseInt(params.get("timeframe").get(0));
            } else {
                return STANDARD_TIMEFRAME;
            }
        } else {
            throw new RuntimeException("No parameters provided.");
        }
    }
    
    public static String customParameterFromQueryParameters(QueryStringDecoder qsd, String what) {
        Map<String, List<String>> params = qsd.getParameters();
        
        if (params.containsKey(what) && !params.get(what).get(0).isEmpty()) {
            return params.get(what).get(0);
        } else {
            throw new RuntimeException("Missing parameter: " + what);
        }
    }
    
}
