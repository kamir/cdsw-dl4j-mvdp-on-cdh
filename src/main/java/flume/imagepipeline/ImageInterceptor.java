package flume.imagepipeline;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.apache.flume.interceptor.Interceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kamir on 09.05.17.
 */
public class ImageInterceptor implements Interceptor {

        private String scriptPath;

        public ImageInterceptor(String scriptPath){
            this.scriptPath = scriptPath;
        }

        @Override
        public void initialize() {
            // At interceptor start up
            try {

                File file = new File( scriptPath + "/convert-png-to-pgm.sh" );

                System.out.println( "Create a converter script: " + file.getAbsolutePath() );

            }
            catch (Exception e) {
                throw new FlumeException("Cannot find a scriptPath: " , e);
            }
        }

        @Override
        public Event intercept(Event event) {

            // This is the event's body
            byte[] body = event.getBody();

            // These are the event's headers
            Map<String, String> headers = event.getHeaders();

            // Enrich header with hostname
            headers.put("imageFormat", "PGM");


            byte[] converted = convert( body, "PNG", "PGM" );


            event.setBody( converted );
            // Let the enriched event go
            return event;
        }


    /**
     *
     * Image conversion has to be implemented here ...
     *
     * @param body
     * @param src
     * @param dest
     * @return
     */
    private byte[] convert(byte[] body, String src, String dest) {

            return body;

    }


    @Override
        public List<Event> intercept(List<Event> events) {

            List<Event> interceptedEvents =
                    new ArrayList<Event>(events.size());
            for (Event event : events) {
                // Intercept any event
                Event interceptedEvent = intercept(event);
                interceptedEvents.add(interceptedEvent);
            }

            return interceptedEvents;
        }

        @Override
        public void close() {
            // At interceptor shutdown
        }

        public static class Builder implements Interceptor.Builder {

            private String scriptPath;

            @Override
            public void configure(Context context) {
                // Retrieve property from flume conf
                scriptPath = context.getString("scriptPath");
            }

            @Override
            public Interceptor build() {
                return new ImageInterceptor(scriptPath);
            }

        }
    }



