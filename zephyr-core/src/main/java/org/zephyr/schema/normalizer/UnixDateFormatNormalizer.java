package org.zephyr.schema.normalizer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UnixDateFormatNormalizer implements Normalizer {

    private final String outgoingFormat;
    private final TimeZone timezone;

    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * If no timezone information is in <code>incomingFormat</code> than the systems default TimeZone will be used.
     *
     * @param incomingFormat
     * @param outgoingFormat
     */
    public UnixDateFormatNormalizer(String outgoingFormat) {
        this(null, outgoingFormat);
    }

    /**
     * @param timezone       - specified TimeZone that will be used, even if there is timezone information in <code>incomingFormat</code>
     * @param incomingFormat
     * @param outgoingFormat
     */
    public UnixDateFormatNormalizer(TimeZone timezone, String outgoingFormat) {
        this.outgoingFormat = outgoingFormat;
        this.timezone = timezone;

    }


    @Override
    public String normalize(String value) throws NormalizationException {
        try {
            String splitOnDecimal[] = value.split("\\.");

            long milliseconds = Long.parseLong(splitOnDecimal[0]) * 1000;
            if (splitOnDecimal.length == 2) {
                String remainder = splitOnDecimal[1];
                if (remainder.length() > 3) {
                    remainder = remainder.substring(0, 3); // java can't do anything with stuff past millis, at least in a Date field
                }
                milliseconds = milliseconds + Long.parseLong(remainder);
            }

            Calendar calendar = null;
            if (timezone != null) {
                calendar = Calendar.getInstance(timezone);
            } else {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(milliseconds);
            Date date = calendar.getTime();
            DateFormat out = new SimpleDateFormat(outgoingFormat);
            out.setTimeZone(TimeZone.getTimeZone("GMT"));
            return out.format(date);
        } catch (Throwable t) {
            throw new NormalizationException(t);
        }
    }

}
