package dev.hero.test.nyxcore.services.registry.dashboard;

import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.services.registry.hosts.HostRegistryService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardRegistryService {

    private final DashboardLoaderService loader;
    private final HostRegistryService hostRegistry;
    private Map<String, HostDto> dashboardTargets = Collections.emptyMap();
    @Getter
    private DashboardDto dashboardDto;

    @PostConstruct
    public void init() {
        try {
            this.dashboardDto = loader.loadDashboard();
            this.dashboardTargets = assembleTargets();
            log.info("Dashboard Registry initialized with {} targets.", dashboardTargets.size());
        } catch (Exception e) {
            log.error("Failed to initialize Dashboard Registry", e);
            // In a production environment, you might throw a RuntimeException here
            // to stop the bot if the dashboard is critical.
        }
    }

    /**
     * Aggregates managed hosts and manual additions into a single view.
     * This uses references to existing HostDto objects, avoiding data duplication.
     */
    private Map<String, HostDto> assembleTargets() {
        Map<String, HostDto> map = new LinkedHashMap<>();

        // 1. Process Managed Hosts based on import settings
        if (dashboardDto.targets().importAllHosts()) {
            hostRegistry.getAllHosts().forEach(h -> map.put(h.getName(), h));
        } else if (dashboardDto.targets().importSpecificHosts() != null) {
            dashboardDto.targets().importSpecificHosts().forEach(name -> {
                HostDto managed = hostRegistry.getHost(name);
                if (managed != null) {
                    map.put(name, managed);
                } else {
                    log.warn("Dashboard config requested host '{}', but it is not in hosts.json", name);
                }
            });
        }

        if (dashboardDto.targets().manualAdditions() != null) {
            for (DashboardDto.ManualAddition manual : dashboardDto.targets().manualAdditions()) {
                map.put(manual.name(), convertToHostDto(manual));
            }
        }

        return map;
    }

    private HostDto convertToHostDto(DashboardDto.ManualAddition manual) {
        HostDto dto = new HostDto();
        dto.setName(manual.name());
        dto.setDisplayName(manual.displayName());
        dto.setIp(manual.ip());
        // Managed fields (mac, user, keyPath) remain null for manual targets.
        return dto;
    }

    public HostDto getTarget(String name) {
        return dashboardTargets.get(name);
    }

    public DashboardDto.Action getAction(String id) { return  dashboardDto.actions().stream()
            .filter(a -> a.id().equals(id))
            .findFirst()
            .orElse(null);
        }

    public Collection<HostDto> getAllTargets() { return dashboardTargets.values(); }

    public Collection<DashboardDto.Action> getAllActions() { return dashboardDto.actions(); }

}