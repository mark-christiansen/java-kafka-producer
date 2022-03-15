package com.jnj.kafka.producer;

import org.apache.avro.Schema;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SchemaLoader {

    private static final String SCHEMAS_PATH = "/**/*.avsc";

    private final Map<String, Schema> schemas = new HashMap<>();

    public SchemaLoader() throws IOException {
        loadSchemas();
    }

    public Schema getSchema(String name) {
        return schemas.get(name);
    }

    private void loadSchemas() throws IOException {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(SCHEMAS_PATH);

        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }

                String fileName = resource.getFilename();
                String name = fileName != null ? fileName.substring(0, fileName.indexOf(".")) : "<null>";
                Schema schema = new Schema.Parser().parse(sb.toString());
                schemas.put(name, schema);
            }
        }
    }
}
