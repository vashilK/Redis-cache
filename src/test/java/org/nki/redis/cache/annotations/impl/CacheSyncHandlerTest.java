package org.nki.redis.cache.annotations.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nki.redis.cache.model.MethodInvocation;
import org.nki.redis.cache.model.WrapperPair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CacheSyncHandlerTest {

    @InjectMocks
    CacheSyncHandler cacheSyncHandler;

    @Mock
    ObjectMapper objectMapper;

    private static final Set<String> singleKeys = Set.of(
            "Book::getBookByAuthor::String=\"34\"");
    private static final Set<String> multipleKeys = Set.of(
            "Book::getBookByAuthor::String=\"34\"",
            "Book::getBookByAuthor::String=\"9\"");

    @Test
    public void buildMethodParamsBuilderWithSingleKey() throws JsonProcessingException {
        when(objectMapper.readValue("\"34\"", String.class)).thenReturn("34");

        Map<String, List<WrapperPair>> result = cacheSyncHandler.initParams(singleKeys);

        assertEquals(List.of("34"), result.get("getBookByAuthor").get(0).getParams());
    }

    @Test
    public void buildMethodParamsBuilderWithMultipleKey() throws JsonProcessingException {
        when(objectMapper.readValue("\"34\"", String.class)).thenReturn("34");
        when(objectMapper.readValue("\"9\"", String.class)).thenReturn("9");

        Map<String, List<WrapperPair>> result = cacheSyncHandler.initParams(multipleKeys);

        assertEquals(2, result.get("getBookByAuthor").size());
    }

    @Test
    public void buildMultipleSingleMethodInvocationsTest()
            throws JsonProcessingException, NoSuchMethodException {
        when(objectMapper.readValue("\"34\"", String.class)).thenReturn("34");
        when(objectMapper.readValue("\"9\"", String.class)).thenReturn("9");

        Map<String, List<WrapperPair>> methodWrappers = cacheSyncHandler.initParams(multipleKeys);
        Method method = getMethod(CacheSyncHandlerTest.class, String.class);
        
        List<MethodInvocation> methodInvocations = cacheSyncHandler
                .getMethodInvocations(List.of(method), methodWrappers);

        assertEquals(2 ,methodInvocations.size());
        assertEquals(1, methodInvocations.get(0).getInvocationParams().size());
    }

    private <T, V> Method getMethod(Class<T> clazz, Class<V> param) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("getBookByAuthor", param);
    }
    
    private void getBookByAuthor(String param){}
}