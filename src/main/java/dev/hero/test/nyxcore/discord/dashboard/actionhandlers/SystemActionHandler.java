package dev.hero.test.nyxcore.discord.dashboard.actionhandlers;

import dev.hero.test.nyxcore.dto.AlertEvent;
import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import dev.hero.test.nyxcore.features.system.restart.RestartProvider;
import dev.hero.test.nyxcore.features.system.shutdown.ShutdownProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemActionHandler implements DashboardActionHandler {

    private final List<ShutdownProvider> shutdownProviders;
    private final List<RestartProvider> restartProviders;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getHandlerType() {
        return "system";
    }

    @Override
    public ExecutionResult execute(HostDto target, DashboardDto.Action action) {
        return switch (action.id().toLowerCase()) {
            case "shutdown" -> handleShutdown(target);
            case "restart" -> handleRestart(target);
            default -> ExecutionResult.fail("Action does not exist: " + action.id());
        };
    }

    private ExecutionResult handleShutdown(HostDto target) {
        ShutdownProvider targetProvider = shutdownProviders.stream()
                .filter(provider -> provider.supports(target.getOs()))
                .findFirst()
                .orElse(null);

        if (targetProvider == null) {
            eventPublisher.publishEvent(new AlertEvent("Shutdown Error", "System", "No shutdown provider found for OS: " + target.getOs(), target.getName()));
            return ExecutionResult.fail("No shutdown provider found for OS: " + target.getOs());
        }

        try {
            targetProvider.shutdown(
                    target.getUser(),
                    target.getIp(),
                    String.valueOf(target.getPort()),
                    target.getKeyPath()
            );
            return ExecutionResult.pass("Shutdown signal sent successfully to **" + target.getDisplayName() + "**.");

        } catch (ActionExecutionException e) {
            log.error("Failed to execute shutdown on {}", target.getIp(), e);

            eventPublisher.publishEvent(new AlertEvent("Execution Failed", "Shutdown", e.getMessage(), target.getName()));
            return ExecutionResult.fail("Action Failed: " + e.getMessage());
        }
    }

    private ExecutionResult handleRestart(HostDto target) {
        RestartProvider targetProvider = restartProviders.stream()
                .filter(provider -> provider.supports(target.getOs()))
                .findFirst()
                .orElse(null);

        if (targetProvider == null) {
            eventPublisher.publishEvent(new AlertEvent("Restart Error", "System", "No restart provider found for OS: " + target.getOs(), target.getName()));
            return ExecutionResult.fail("No restart provider found for OS: " + target.getOs());
        }

        try {
            targetProvider.restart(
                    target.getUser(),
                    target.getIp(),
                    String.valueOf(target.getPort()),
                    target.getKeyPath()
            );
            return ExecutionResult.pass("Restart signal sent successfully to **" + target.getDisplayName() + "**.");

        } catch (ActionExecutionException e) {
            log.error("Failed to execute restart on {}", target.getIp(), e);

            eventPublisher.publishEvent(new AlertEvent("Execution Failed", "Restart", e.getMessage(), target.getName()));
            return ExecutionResult.fail("Action Failed: " + e.getMessage());
        }
    }
}