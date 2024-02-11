package com.gen.utils.traces;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.protobuf.ByteString;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Timestamps;
import io.jaegertracing.api_v2.Model;
import lombok.SneakyThrows;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gen.utils.traces.TestHelper.*;
import static io.jaegertracing.api_v2.Model.SpanRefType.FOLLOWS_FROM;

public class TestJaegerProto {
    private static final String SIMPLE_TRACES = getTemplateContent("/simple-isolated-traces.handlebars");
    private static final String X_REF_TRACES = getTemplateContent("/cross-referenced-traces.handlebars");

    @SneakyThrows
    @Test
    // Generate Model.Batch using handlebars template with simple non related traces
    public void should_generate_simple_batch_of_traces() {
        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(SIMPLE_TRACES);

        // Provide data to replace placeholders in the template
        Map<String, Object> data = new HashMap();
        data.put("t1", generateTraceSampleData());
        data.put("t2", generateTraceSampleData());
        data.put("t3", generateTraceSampleData());
        String traceData = template.apply(data);
        // System.out.println(traceData);
        Model.Batch.Builder messageBuilder = Model.Batch.newBuilder();
        JsonFormat.parser().usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(traceData, messageBuilder);
        var batch = messageBuilder.build();
        System.out.println(batch.getSpansCount());
    }


    @SneakyThrows
    @Test
    // Generate Model.Batch using handlebars template with cross-referenced traces
    public void should_generate_x_referenced_batch_of_traces() {
        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(X_REF_TRACES);

        // Provide data to replace placeholders in the template
        Map<String, Object> data = new HashMap();
        data.put("t1", generateTraceSampleData());
        data.put("t2", generateTraceSampleData());
        data.put("t3", generateTraceSampleData());
        String traceData = template.apply(data);
        // System.out.println(traceData);
        Model.Batch.Builder messageBuilder = Model.Batch.newBuilder();
        JsonFormat.parser().usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry()).merge(traceData, messageBuilder);
        var batch = messageBuilder.build();
        System.out.println(batch.getSpansCount());
    }


    // The below test useful only to verify jaeger proto format. This does not generate using handlebars
    @Test
    @SneakyThrows
    public void should_serialise() {
        Model.Process process = Model.Process.newBuilder().setServiceName("service-a").build();
        var traceId = ByteString.copyFromUtf8(generateNewTraceId());
        var spanIdA = ByteString.copyFromUtf8(generateNewSpanId());
        var spanIdB = ByteString.copyFromUtf8(generateNewSpanId());
        var spanIdC = ByteString.copyFromUtf8(generateNewSpanId());
        Model.Batch trace = Model.Batch.newBuilder()
                .addAllSpans(List.of(
                                Model.Span.newBuilder()
                                        .setDuration(Durations.fromMillis(100L))
                                        .setOperationName("operation-a")
                                        .setTraceId(traceId)
                                        .setSpanId(spanIdA)
                                        .setStartTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                                        .addAllTags(List.of(Model.KeyValue.newBuilder().setKey("key-1").setVType(Model.ValueType.STRING).setVStr("val-1").build()))
                                        .addAllReferences(List.of())
                                        .setProcessId(process.getServiceName())
                                        .setProcess(process)
                                        .build(),
                                Model.Span.newBuilder()
                                        .setDuration(Durations.fromMillis(100L))
                                        .setOperationName("operation-b")
                                        .setTraceId(traceId)
                                        .setSpanId(spanIdB)
                                        .setStartTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                                        .addAllTags(List.of(Model.KeyValue.newBuilder().setKey("key-1").setVType(Model.ValueType.STRING).setVStr("val-1").build()))
                                        .addAllReferences(List.of(
                                                Model.SpanRef.newBuilder().setRefType(FOLLOWS_FROM).setTraceId(traceId).setSpanId(spanIdA).build()
                                        ))
                                        .setProcessId(process.getServiceName())
                                        .setProcess(process)
                                        .build(),
                                Model.Span.newBuilder()
                                        .setDuration(Durations.fromMillis(100L))
                                        .setOperationName("operation-c")
                                        .setTraceId(traceId)
                                        .setSpanId(spanIdC)
                                        .setStartTime(Timestamps.fromMillis(Instant.now().toEpochMilli()))
                                        .addAllTags(List.of(Model.KeyValue.newBuilder().setKey("key-1").setVType(Model.ValueType.STRING).setVStr("val-1").build()))
                                        .addAllReferences(List.of(
                                                Model.SpanRef.newBuilder().setRefType(FOLLOWS_FROM).setTraceId(traceId).setSpanId(spanIdB).build()
                                        ))
                                        .setProcessId(process.getServiceName())
                                        .setProcess(process)
                                        .build()
                        )
                )
                .build();
        System.out.println(JsonFormat.printer().print(trace));
    }
}
