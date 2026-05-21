package dev.hero.test.nyxcore.services.registry.hosts;

import dev.hero.test.nyxcore.dto.HostDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HostRegistryService {

    private final HostLoaderService loader;

    private Map<String, HostDto> hosts = Collections.emptyMap();

    @PostConstruct
    void init() {
        try {
            hosts = loader.loadHosts().stream()
                    .collect(Collectors.toMap(HostDto::getName, Function.identity(),
                            (a, b) -> {
                                throw new IllegalStateException("Duplicate host at" + a.getName() + " and " + b.getName());
                            }));
            // Function.identity is same as h -> h, u cant just write HostDto because it requires a function
            // a,b are the 2 keys that are duplicates and collide, if we want to keep one we do (a,b) -> a
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load hosts.json", e);
        }
    }

    public HostDto getHost(String hostName) {
        return hosts.get(hostName);
    }

    public Collection<HostDto> getAllHosts() {
        return hosts.values();
    }
}
