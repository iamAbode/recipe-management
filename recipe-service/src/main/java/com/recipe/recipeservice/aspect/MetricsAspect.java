package com.recipe.recipeservice.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.recipe.recipeservice.controller.*.*(..))")
    public Object measureControllerMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        return measureExecutionTime(pjp, "controller");
    }

    @Around("execution(* com.recipe.recipeservice.service.*.*(..))")
    public Object measureServiceMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        return measureExecutionTime(pjp, "service");
    }

    @Around("execution(* com.recipe.recipeservice.repository.*.*(..))")
    public Object measureRepositoryMethodExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        return measureExecutionTime(pjp, "repository");
    }

    private Object measureExecutionTime(ProceedingJoinPoint pjp, String type) throws Throwable {
        long start = System.nanoTime();
        
        Object result = pjp.proceed();
        
        long end = System.nanoTime();
        long executionTime = end - start;
        
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        
        // Record method execution time
        Timer.builder("method.execution.time")
                .tag("type", type)
                .tag("class", className)
                .tag("method", methodName)
                .description("Execution time of " + className + "." + methodName)
                .register(meterRegistry)
                .record(executionTime, TimeUnit.NANOSECONDS);
        
        return result;
    }
}