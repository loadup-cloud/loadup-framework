package com.github.vincentrussell.json.datagenerator.impl;

import com.github.vincentrussell.json.datagenerator.functions.FunctionRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FunctionTokenResolverTest {

    FunctionTokenResolver functionTokenResolver;
    FunctionRegistry functionRegistry;

    @BeforeEach
    public void setup() {
        functionRegistry = new FunctionRegistry();
        functionTokenResolver = new FunctionTokenResolver(functionRegistry);
    }

    @Test
    public void unknownFunction() {

    }


    @Test
    public void badArgumentsForFunction() {

    }

}