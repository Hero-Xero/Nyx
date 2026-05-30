package dev.hero.test.nyxcore.discord.dashboard.actionhandlers;

import dev.hero.test.nyxcore.annotations.MonitoredAction;
import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.features.network.ping.PingProvider;
import dev.hero.test.nyxcore.features.network.wol.WolProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetworkActionHandler implements DashboardActionHandler {

    private final PingProvider pingProvider;
    private final WolProvider wolProvider;

    @Override
    public String getHandlerType() {
        return "network";
    }

    @Override
    @MonitoredAction // <-- PERFECT PLACEMENT
    public ExecutionResult execute(HostDto host, DashboardDto.Action action) {
        return switch (action.id().toLowerCase()) {
            case "ping" -> handlePing(host);
            case "wol"  -> handleWol(host);
            default     -> ExecutionResult.fail("Unknown network command: " + action.id());
        };
    }

    private ExecutionResult handlePing(HostDto host) {
        // If ping fails, it throws an exception and the Aspect catches it instantly.
        // If it passes, it moves to the next line. No if/else needed.
        pingProvider.ping(host.getIp());
        return ExecutionResult.pass("Target **" + host.getDisplayName() + "** is ONLINE.");
    }

    private ExecutionResult handleWol(HostDto host) {
        if (host.getMac() == null || host.getMac().isBlank()) {
            // This is a validation failure, not an execution crash, so we return fail directly
            return ExecutionResult.fail("No MAC address configured for **" + host.getDisplayName() + "**.");
        }

        // If WOL fails, it throws an exception.
        wolProvider.wake(host.getMac());
        return ExecutionResult.pass("Wake-on-LAN magic packet sent to **" + host.getMac() + "**.");
    }
}