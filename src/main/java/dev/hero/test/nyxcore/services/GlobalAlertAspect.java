package dev.hero.test.nyxcore.services;

import dev.hero.test.nyxcore.dto.AlertEvent;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalAlertAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Around("@annotation(dev.hero.test.nyxcore.annotations.MonitoredAction)")
    public Object handleActionExecution(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            ActionContext.Metadata meta = ActionContext.get();

            String source = (meta != null) ? meta.source() : "Unknown";
            String host = (meta != null) ? meta.hostName() : "Unknown";
            int exitCode = -1;
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            String stdout = "";

            if (ex instanceof ActionExecutionException aex) {
                exitCode = aex.getExitCode();
                if (aex.getStdOut() != null && !aex.getStdOut().isBlank()) {
                    stdout = aex.getStdOut();
                }
            }

            log.error("Alert Aspect captured error from {}: {}", source, message, ex);

            eventPublisher.publishEvent(new AlertEvent(
                    "Execution Failed",
                    source,
                    exitCode,
                    message,
                    host,
                    stdout
            ));

            return ExecutionResult.fail(message);
        }
    }
}
