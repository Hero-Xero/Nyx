package dev.hero.test.nyxcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DashboardDto(
        @NotNull(message = "ui_settings are required")
        @Valid
        @JsonProperty("ui_settings")
        UiSettings uiSettings,

        @JsonProperty("quick_links")
        List<QuickLink> quickLinks,

        @NotNull(message = "targets are required")
        @Valid
        Targets targets,

        @NotNull(message = "actions are required")
        @Valid
        List<Action> actions
) {
    public record UiSettings(
            @NotBlank(message = "title cannot be blank")
            String title,
            String description,
            @JsonProperty("image_url")
            String image,
            @JsonProperty("thumbnail_url")
            String thumbnail,
            String footer
    ) {}

    public record QuickLink(String label, String url) {}

    public record Targets(
            @JsonProperty("import_all_hosts")
            boolean importAllHosts,

            @JsonProperty("import_specific_hosts")
            List<String> importSpecificHosts,

            @JsonProperty("manual_additions")
            List<ManualAddition> manualAdditions
    ) {}

    public record ManualAddition(
            @NotBlank(message = "name is required")
            String name,
            @JsonProperty("display_name")
            @NotBlank(message = "display_name is required")
            String displayName,
            @NotBlank(message = "ip is required")
            String ip
    ) {}

    public record Action(
            @NotBlank(message = "action id is required")
            String id,
            String label,
            String description,
            String type,
            String command
    ) {}
}