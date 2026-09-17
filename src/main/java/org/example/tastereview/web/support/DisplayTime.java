package org.example.tastereview.web.support;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.example.tastereview.config.AppProperties;
import org.springframework.stereotype.Component;

/** SRS 5절 공통: UTC 저장 시각을 app.display.time-zone 기준 yyyy-MM-dd HH:mm 으로 표시. */
@Component("displayTime")
public class DisplayTime {

    private static final String PATTERN = "yyyy-MM-dd HH:mm";

    private final ZoneId zone;

    public DisplayTime(AppProperties appProperties) {
        this.zone = ZoneId.of(appProperties.getDisplay().getTimeZone());
    }

    public String format(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(PATTERN).withZone(zone).format(instant);
    }
}
