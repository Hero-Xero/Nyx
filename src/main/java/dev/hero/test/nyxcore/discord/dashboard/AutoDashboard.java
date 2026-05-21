package dev.hero.test.nyxcore.discord.dashboard;

import dev.hero.test.nyxcore.config.DiscordProperties;
import dev.hero.test.nyxcore.dto.DashboardDto;
import dev.hero.test.nyxcore.dto.ExecutionResult;
import dev.hero.test.nyxcore.dto.HostDto;
import dev.hero.test.nyxcore.services.registry.dashboard.DashboardRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoDashboard extends ListenerAdapter {

    private final DashboardRegistryService registry;
    private final DashboardRouter dashboardRouter;
    private final DiscordProperties discordProps;

    // Triggered manually by the bootloader to avoid double-refresh bugs
    public void refresh(JDA jda) {
        String dashboardChannelId = discordProps.channels().dashboard();

        if (dashboardChannelId == null || dashboardChannelId.isBlank()) {
            log.warn("Dashboard channel ID not set.");
            return;
        }

        TextChannel channel = jda.getTextChannelById(dashboardChannelId);
        if (channel == null) {
            log.error("Dashboard channel not found: {}", dashboardChannelId);
            return;
        }

        log.info("Refreshing Dashboard in channel: {}", channel.getName());

        // Fetch history asynchronously
        channel.getIterableHistory().takeAsync(10).thenAccept(messages -> {
            try {
                if (!messages.isEmpty()) {
                    log.info("Found {} old messages. Purging...", messages.size());
                    channel.purgeMessages(messages); // Delete them
                }
            } catch (Exception e) {
                log.warn("Purge failed (likely permissions or old messages): {}", e.getMessage());
            } finally {
                // Zis guarantees the spawn only happens after the purge is completely done.
                log.info("Spawning new dashboard...");
                spawnDashboard(channel);
            }
        });
    }

    private void spawnDashboard(TextChannel channel) {
        DashboardDto dto = registry.getDashboardDto();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(dto.uiSettings().title())
                .setColor(Color.MAGENTA)
                .appendDescription(dto.uiSettings().description())
                .setThumbnail(dto.uiSettings().thumbnail())
                .setImage(dto.uiSettings().image())
                .setFooter(dto.uiSettings().footer());

        String quickUrls = dto.quickLinks().stream()
                .map(quickLink -> "[" + quickLink.label() + "](" + quickLink.url() + ")")
                .collect(Collectors.joining("\n"));

        embed.addField("Quick Links", quickUrls, false);

        // Initial spawn has no selected host, so we just pass null
        channel.sendMessageEmbeds(embed.build()).addComponents(
                ActionRow.of(buildHostMenu(null)),
                ActionRow.of(buildDisabledActionMenu())
        ).queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getComponentId();

        switch (id) {
            case "target-selector" -> handleDeviceSelection(event);
            case "action-selector" -> handleActionSelection(event);
            default -> log.warn("Unknown selection ID: {}", id);
        }
    }

    private void handleDeviceSelection(StringSelectInteractionEvent event) {
        String hostId = event.getValues().getFirst();

        // Pass the hostId back into the builder so it locks in as the selected visual default
        event.editComponents(
                ActionRow.of(buildHostMenu(hostId)),
                ActionRow.of(buildActiveActionMenu(hostId))
        ).queue();
    }

    private void handleActionSelection(StringSelectInteractionEvent event) {
        String value = event.getValues().getFirst();
        String[] parts = value.split(":");

        if (parts.length < 2) return;

        HostDto hostDto = registry.getTarget(parts[0]);
        DashboardDto.Action action = registry.getAction(parts[1]);

        if (hostDto == null || action == null) {
            event.reply("❌ Configuration error. Action or Host not found.").setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        ExecutionResult result = dashboardRouter.route(hostDto, action);

        MessageEmbed currentEmbed = event.getMessage().getEmbeds().getFirst();
        MessageEmbed.Field quickLinksField = currentEmbed.getFields().getFirst();

        EmbedBuilder updatedEmbed = new EmbedBuilder(currentEmbed)
                .setColor(result.success() ? Color.GREEN : Color.RED)
                .clearFields()
                .addField(quickLinksField)
                .addField(action.label(), result.message(), false);

        event.getHook().editOriginalEmbeds(updatedEmbed.build()).queue();
    }

    // --- Ze Component Builders ---

    private StringSelectMenu buildHostMenu(String selectedHostId) {
        StringSelectMenu.Builder hostMenuBuilder = StringSelectMenu.create("target-selector")
                .setPlaceholder("Select a target device...");

        registry.getAllTargets().forEach(host ->
                hostMenuBuilder.addOption(host.getDisplayName(), host.getName())
        );

        // This prevents the menu from resetting to the placeholder
        if (selectedHostId != null) {
            hostMenuBuilder.setDefaultValues(selectedHostId);
        }

        return hostMenuBuilder.build();
    }

    private StringSelectMenu buildDisabledActionMenu() {
        return StringSelectMenu.create("menu:action:disabled")
                .setPlaceholder("Select an Action...")
                .setDisabled(true)
                .addOption("...", "dummy")
                .build();
    }

    private StringSelectMenu buildActiveActionMenu(String hostId) {
        StringSelectMenu.Builder actionBuilder = StringSelectMenu.create("action-selector")
                .setPlaceholder("Select an action...");

        registry.getAllActions().forEach(action -> {
            actionBuilder.addOption(action.label(), hostId + ":" + action.id(), action.description());
        });


        return actionBuilder.build();
    }
}
