package dev.hero.test.nyxcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CommandDto {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "command is required")
    private String command;

    @NotBlank(message = "description is required")
    private String description;

    @JsonProperty("fixed_flags")
    private String fixedFlags;

    private boolean sudo = false;

    @JsonProperty("is_image")
    private boolean isImage = false;

    @Valid
    private List<FlagDefinition> flags;

    @Valid
    private List<SubcommandDefinition> subcommands;

    @Valid
    private List<ArgumentDefinition> arguments;


    @Data
    public static class FlagDefinition {
        @NotBlank(message = "name is required")
        private String name;
        @NotBlank(message = "flag is required")
        private String flag;
        @NotBlank(message = "description is required")
        private String description;
        private Type type = Type.STRING; // Default to STRING
        private Map<String, String> choices;
        private boolean required = false;
    }

    @Data
    public static class SubcommandDefinition {
        @NotBlank(message = "subcommand name is required")
        private String name;

        @NotBlank(message = "description is required")
        private String description;

        @NotBlank(message = "subcommand is required")
        private String subcommand;

        @Valid
        List<FlagDefinition> flags;
        @Valid
        List<ArgumentDefinition> arguments;

    }

     @Data
    public static class ArgumentDefinition {
        @NotBlank(message = "argument name is required")
        private String name;

        @NotBlank(message = "description is required")
        private String description;
        private Type type = Type.STRING; // Default to STRING
        private Map<String, String> choices;
        private boolean required = false;
    }

    public enum Type {
        STRING,
        BOOLEAN,
        INTEGER,
        NUMBER,
        USER,
        ROLE

    }

    // --- methods ---
    public FlagDefinition getFlagByName(String optionName) {
        if (this.flags == null) return null;
        return this.flags.stream()
                .filter(f -> f.getName().equalsIgnoreCase(optionName))
                .findFirst()
                .orElse(null);
    }

    public ArgumentDefinition getArgumentByName(String optionName) {
        if (this.arguments == null) return null;
        return this.arguments.stream()
                .filter(f -> f.getName().equalsIgnoreCase(optionName))
                .findFirst()
                .orElse(null);
    }
}