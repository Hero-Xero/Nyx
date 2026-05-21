package dev.hero.test.nyxcore.discord.dashboard;

import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.discord.dashboard.actionhandlers.DashboardActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardRouter {

    private final ApplicationEventPublisher publisher;
    private final Map<String, DashboardActionHandler> handlers;

    public DashboardRouter(List<DashboardActionHandler> handlerList, ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(DashboardActionHandler::getHandlerType, Function.identity()));
    }

    public ExecutionResult route(HostDto host, DashboardDto.Action action) {

        DashboardActionHandler handler = handlers.get(action.type());

        if (handler == null) {
            return ExecutionResult.fail("System Error: No handler mapped for type '" + action.type() + "'");
        }

        try {
            return handler.execute(host, action);
        } catch (Exception e) {
            log.error("CRITICAL ROUTER ERROR: Failed to execute action '{}' on host '{}'", action.id(), host.getName(), e);

            publisher.publishEvent(e);
            return ExecutionResult.fail("Internal Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}