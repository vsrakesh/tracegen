package com.gen.utils.benchmark;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.gen.utils.traces.TestHelper.getTemplateContent;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})

public class BenchmarkHandleBars {

    private static final Handlebars handlebars = new Handlebars();
    private static final String SIMPLE_TRACES = getTemplateContent("/simple-isolated-traces.handlebars");
    private static final String X_REF_TRACES = getTemplateContent("/cross-referenced-traces.handlebars");
    private static final Template T1, T2;

    private static final Map<String, Object> data = Map.of("t1", generateTraceSampleData(),
            "t2", generateTraceSampleData(),
            "t3", generateTraceSampleData());

    static {
        try {
            T1 = handlebars.compileInline(SIMPLE_TRACES);
            T2 = handlebars.compileInline(X_REF_TRACES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BenchmarkHandleBars.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @SneakyThrows
    @Benchmark
    public void benchmarkHandleBarsApplyT1(Blackhole bh) {
        bh.consume(T1.apply(data));
    }

    @SneakyThrows
    @Benchmark
    public void benchmarkHandleBarsApplyT2(Blackhole bh) {
        bh.consume(T2.apply(data));
    }

    @SneakyThrows
    @Benchmark
    public void benchmarkHandleBarsApplyT1T2Async(Blackhole bh) {
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    try {
                        bh.consume(T1.apply(data));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }),
                CompletableFuture.runAsync(() -> {
                    try {
                        bh.consume(T1.apply(data));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
        ).join();

    }

    @Benchmark
    @SneakyThrows
    public void benchmarkProduceSpans(Blackhole bh) {
        var data = Map.of("t1", generateTraceSampleData(),
                "t2", generateTraceSampleData(),
                "t3", generateTraceSampleData());
        var t1 = handlebars.compileInline(SIMPLE_TRACES);
        var t2 = handlebars.compileInline(X_REF_TRACES);

        bh.consume(t1.apply(data));
        bh.consume(t2.apply(data));
    }

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

    public static String getTimeWithOffset(long offsetInMillis) {
        return DateTimeFormatter.ISO_INSTANT.format(Clock.systemUTC().instant().plusMillis(offsetInMillis));
    }

}
