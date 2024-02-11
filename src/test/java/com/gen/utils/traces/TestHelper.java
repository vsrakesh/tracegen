package com.gen.utils.traces;

import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * Helper class for tests
 */
public final class TestHelper {

    /*
        Return unique data for traceId, n spanIds and n startTimes to generate trace.
        Note: Expand this to include more spans when needed.
     */
    public static Map<String, String> generateTraceSampleData() {
        return Map.of(
                "traceId", generateNewTraceId(),
                "s1", generateNewSpanId(),
                "s2", generateNewSpanId(),
                "s3", generateNewSpanId(),
                "s4", generateNewSpanId(),
                "st1", getTimeWithOffset(100),
                "st2", getTimeWithOffset(100),
                "st3", getTimeWithOffset(100),
                "st4", getTimeWithOffset(100)
        );
    }

    public static String getTimeWithOffset(long offsetInMillis) {
        return DateTimeFormatter.ISO_INSTANT.format(Clock.systemUTC().instant().plusMillis(offsetInMillis));
    }

    @SneakyThrows
    public static String getTemplateContent(String templatePath) {
        var templateStream = TestHelper.class.getResourceAsStream(templatePath);
        try (Reader reader = new InputStreamReader(templateStream, StandardCharsets.UTF_8)) {
            char[] charBuffer = new char[1024];
            var stringBuilder = new StringBuilder();
            int bytesRead;
            while ((bytesRead = reader.read(charBuffer)) != -1) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
            return stringBuilder.toString();
        } finally {
            if (templateStream != null) {
                templateStream.close();
            }
        }
    }

    public static String generateNewTraceId() {
        return new StringBuilder()
                .append("abcdefab-")
                .append(randomNumeric(8)).append("-")
                .append(randomNumeric(8)).append("-")
                .append(randomNumeric(8)).toString();
    }

    public static String generateNewSpanId() {
        return new StringBuilder()
                .append("abcdefab")
                .append(randomNumeric(8)).toString();
    }
}
