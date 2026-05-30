package dev.hero.test.nyxcore.discord.dashboard.actionhandlers;

import java.util.List;

import dev.hero.test.nyxcore.annotations.MonitoredAction;
import org.springframework.stereotype.Component;

import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;

import dev.hero.test.nyxcore.features.system.restart.RestartProvider;
import dev.hero.test.nyxcore.features.system.shutdown.ShutdownProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemActionHandler implements DashboardActionHandler {

    private final List<ShutdownProvider> shutdownProviders;
    private final List<RestartProvider> restartProviders;

    @Override
    public String getHandlerType() {
        return "system";
    }

    @Override
    @MonitoredAction
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
            // Validation failure, handle directly without throwing
            return ExecutionResult.fail("No shutdown provider found for OS: " + target.getOs());
        }

        // Execution. If it fails, it throws and the AOP aspect intercepts.
        targetProvider.shutdown(
                target.getUser(),
                target.getIp(),
                String.valueOf(target.getPort()),
                target.getKeyPath()
        );

        return ExecutionResult.pass("Shutdown signal sent successfully to **" + target.getDisplayName() + "**.");
    }

    private ExecutionResult handleRestart(HostDto target) {
        RestartProvider targetProvider = restartProviders.stream()
                .filter(provider -> provider.supports(target.getOs()))
                .findFirst()
                .orElse(null);

        if (targetProvider == null) {
            // Validation failure, handle directly without throwing
            return ExecutionResult.fail("No restart provider found for OS: " + target.getOs());
        }

        // Execution. If it fails, it throws and the AOP aspect intercepts.
        targetProvider.restart(
                target.getUser(),
                target.getIp(),
                String.valueOf(target.getPort()),
                target.getKeyPath()
        );

        return ExecutionResult.pass("Restart signal sent successfully to **" + target.getDisplayName() + "**.");
    }
}