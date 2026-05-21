package dev.hero.test.nyxcore.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HostDto {

    @NotBlank(message = "display name cannot be blank")
    @JsonAlias("display_name")
    private String displayName;

    @NotBlank(message = "Host name cannot be blank")
    private String name;

    @NotBlank(message = "user cannot be blank")
    private String user;

    @JsonAlias("sudo_password")
    private String sudoPassword;

    @NotBlank(message = "IP cannot be blank")
    @Pattern(
            regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
            message = "Invalid IPv4 address"
    )
    private String ip;

    private Integer port = 22;

    @JsonAlias("os")
    @NotBlank
    private String os = "linux";

    @Pattern(
            regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Invalid MAC address"
    )
    @NotBlank(message = "mac is required")
    private String mac;

    @JsonAlias("key-path")
    @NotBlank(message = "keyPath is required")
    @Pattern(regexp = "^/.*", message = "keyPath must be an absolute path")
    private String keyPath;
}

