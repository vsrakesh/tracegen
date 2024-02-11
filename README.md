# tracegen

An jaeger trace generator utility using predefined templates

## Intended Usage

The generator can be used to define a bunch of traces (related or otherwise) using the handlebars template files.
These templates can then be used to generate traces by substituting values that change for each trace without changing
the structure defined in the template file.
Variables that change in a trace data are typically

1. traceId
2. spanIds
3. startTime for each span

The examples `simple-isolated-traces.handlebars` and `cross-referenced-traces.handlebars` can be used as reference

### Example

`simple-isolated-traces.handlebars`: has 2 unique trace structures with 3 spans each. The traces are not related to each
other

```text
Trace-1: A->B->C
Trace-2: A->[B,C]
```

`cross-referenced-traces.handlebars` has three traces with the first trace having references to trace2 and trace3

## TODO
// Currently the tests only demonstrate constructing a Model.Batch from the handlebars template.
1. Hookup with apache JMeter or similar to generate based on JMeter load requirements
2. Evaluate and add more templates if needed
3. Add gRPC/HTTP sender to send the constructed traces to consumer

