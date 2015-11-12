package com.walmart.store.location;

import com.google.common.collect.Lists;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.github.reinert.jjschema.JsonSchemaGenerator;
import com.github.reinert.jjschema.SchemaGeneratorBuilder;
import com.walmart.store.location.endpoint.v1.Attribute;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Schema generator test
 */
public class SchemaGeneratorTest {

    @Test
    public void generatorTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();//new YAMLFactory());
        SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
        mapper.acceptJsonFormatVisitor(mapper.constructType(MyBean.class), visitor);
        //JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        try {
            JsonSchema schema = visitor.finalSchema();
            System.out.println(mapper.writeValueAsString(schema));
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sampleEntity() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();//new YAMLFactory());
        try {
            System.out.println(mapper.writeValueAsString(Lists.newArrayList(new Zone("1.0"),new Zone("2.0"))));
        } catch (JsonMappingException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void generatorProductSchemaTest() throws JsonProcessingException {
        JsonSchemaGenerator v4generator = SchemaGeneratorBuilder.draftV4Schema().build();
        JsonNode productSchema = v4generator.generateSchema(Product.class);
        System.out.println(productSchema);

    }

    @Test
    public void evaluate() {
        String rangeValidator = "(v > 10 && v <= 100)";
        JexlEngine jexl = new JexlEngine();
        jexl.setSilent(true);
        jexl.setLenient(true);

        Expression expression = jexl.createExpression(rangeValidator);
        MapContext jexlContext = new MapContext();

        //b and c and d should pass
        jexlContext.set("v", 8);

        Assert.assertTrue((Boolean) expression.evaluate(jexlContext));
        System.out.println(expression.evaluate(jexlContext));
    }

    @Test
    public void evaluateArray() {
        Attribute attribute = new Attribute("usageType","String", true, "eval.contains(m,v)", new String[]{"HAZMAT", "TIRE", "DOG"});

        String rangeValidator = attribute.getExpression();
        JexlEngine jexl = new JexlEngine();

        Expression expression = jexl.createExpression(rangeValidator);
        JexlContext jexlContext = new MapContext();

        jexlContext.set("eval", new SchemaGeneratorTest.Evaluate());
        jexlContext.set("v", "HAZMAT");
        jexlContext.set("m", attribute.getMetaData());

        Assert.assertTrue((Boolean) expression.evaluate(jexlContext));
        System.out.println(expression.evaluate(jexlContext));
    }


    @Test
    public void evaluateLength() {
        Attribute attribute = new Attribute("fixedSize","String", true, "v.length()>=10 && v.length()<=15",null);

        String rangeValidator = attribute.getExpression();
        JexlEngine jexl = new JexlEngine();

        Expression expression = jexl.createExpression(rangeValidator);
        JexlContext jexlContext = new MapContext();

        jexlContext.set("eval", new SchemaGeneratorTest.Evaluate());
        jexlContext.set("v", "somelonger");

        Assert.assertTrue((Boolean) expression.evaluate(jexlContext));
        System.out.println(expression.evaluate(jexlContext));
    }
    public static class Evaluate {
        public boolean contains(Object[] arr, String targetValue) {
            String[] meta = (String[]) arr;
            Set<String> set = new HashSet<>(Arrays.asList(meta));
            return set.contains(targetValue);
        }
    }
}
