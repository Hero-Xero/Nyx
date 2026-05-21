package dev.hero.test.nyxcore.discord.dashboard.actionhandlers;

import dev.hero.test.nyxcore.dto.AlertEvent;
import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.exceptions.ActionExecutionException;
import dev.hero.test.nyxcore.features.network.ping.PingProvider;
import dev.hero.test.nyxcore.features.network.wol.WolProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkActionHandler implements DashboardActionHandler {

    private final PingProvider pingProvider;
    private final WolProvider wolProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String getHandlerType() {
        return "network";
    }

    @Override
    public ExecutionResult execute(HostDto host, DashboardDto.Action action) {
        return switch (action.id().toLowerCase()) {
            case "ping" -> handlePing(host);
            case "wol"  -> handleWol(host);
            default     -> ExecutionResult.fail("Unknown network command: " + action.id());
        };
    }

    private ExecutionResult handlePing(HostDto host) {
        try {
            String rawOutput = pingProvider.ping(host.getIp());

            return ExecutionResult.pass("Target **" + host.getDisplayName() + "** is ONLINE");

        } catch (ActionExecutionException e) {
            log.error("Failed to execute ping on {}", host.getIp(), e);

            eventPublisher.publishEvent(new AlertEvent("Execution Failed", "Ping", e.getMessage(), host.getName()));
            return ExecutionResult.fail("Action Failed: " + e.getMessage());
        }
    }

    private ExecutionResult handleWol(HostDto host) {
        if (host.getMac() == null || host.getMac().isBlank()) {
            return ExecutionResult.fail("Validation Error: No MAC address configured for **" + host.getDisplayName() + "**.");
        }

        try {
            wolProvider.wake(host.getMac());
            return ExecutionResult.pass("Wake-on-LAN magic packet sent to **" + host.getMac() + "**.");

        } catch (ActionExecutionException e) {
            log.error("WOL Failed for {}.", host.getIp(), e);

            eventPublisher.publishEvent(new AlertEvent("Execution Failed", "Wake-On-LAN", e.getMessage(), host.getName()));
            return ExecutionResult.fail("Action Failed: " + e.getMessage());
        }
    }
}