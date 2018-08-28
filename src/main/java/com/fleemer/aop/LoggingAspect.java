package com.fleemer.aop;

import com.fleemer.model.Person;
import javax.servlet.http.HttpSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final String PERSON_SESSION_ATTR = "person";

    @AfterReturning("@annotation(com.fleemer.aop.LogAfterReturning)")
    public void afterReturning(JoinPoint joinPoint) {
        Person person = null;
        Object[] args = joinPoint.getArgs();
        for (Object arg: args) {
            if (arg instanceof Person) {
                person = (Person) arg;
                break;
            }
            if (arg instanceof HttpSession) {
                person = (Person) ((HttpSession) arg).getAttribute(PERSON_SESSION_ATTR);
                break;
            }
        }
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Logger logger = LoggerFactory.getLogger(targetClass);
        String email = person != null ? person.getEmail() : null;
        String executionMethodName = targetClass.getSimpleName() + '.' + joinPoint.getSignature().getName();
        logger.info("Operation completed. User: {} Method: {}.", email, executionMethodName);
    }
}
