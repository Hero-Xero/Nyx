package dev.hero.test.nyxcore.discord.dashboard.actionhandlers;

import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;

public interface DashboardActionHandler {
    String getHandlerType();
    ExecutionResult execute(HostDto target, DashboardDto.Action action);
}