package dev.hero.test.nyxcore.discord.dashboard;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import dev.hero.test.nyxcore.discord.dashboard.actionhandlers.DashboardActionHandler;
import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.services.ActionContext;
import lombok.extern.slf4j.Slf4j;

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
            // Set context for Aspect
            ActionContext.set(action.id(), host.getName());
            
            return handler.execute(host, action);
        } finally {
            ActionContext.clear();
        }
    }
}