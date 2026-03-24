package com.elh.search.handler;

import com.elh.commons.events.CommandReceivedEvent;
import com.elh.search.document.MediaDocument;
import com.elh.search.service.DiscordResponseSender;
import com.elh.search.service.GoogleSearchService;
import com.elh.search.service.GoogleSearchService.GoogleImageResult;
import com.elh.search.service.InternalSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchCommandHandler {

    private final GoogleSearchService googleSearch;
    private final InternalSearchService internalSearch;
    private final DiscordResponseSender sender;

    public void handle(CommandReceivedEvent event) {
        switch (event.getCommand()) {
            case "img"       -> handleImg(event);
            case "buscar"    -> handleBuscar(event);
            case "historico" -> handleHistorico(event);
            case "midia"     -> handleMidia(event);
            case "tag"       -> handleTag(event);
            default -> log.warn("Comando nao suportado: {}", event.getCommand());
        }
    }

    private void handleImg(CommandReceivedEvent event) {
        Map<String, String> opts = event.getOptions();
        String query = opts.get("query");
        int n = parseIntOrDefault(opts.get("n"), 3);

        if (query == null || query.isBlank()) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Preciso de um termo de busca! Ex: `/img cachorro fofo`");
            return;
        }

        List<GoogleImageResult> results = googleSearch.searchImages(query, event.getGuildId(), n);

        if (results.isEmpty()) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Nenhum resultado encontrado para: " + query);
            return;
        }

        List<String> imageUrls = results.stream().map(GoogleImageResult::imageUrl).toList();
        String description = results.stream()
                .map(r -> "[" + truncate(r.title(), 50) + "](" + r.imageUrl() + ") - " + r.source())
                .reduce("", (a, b) -> a + "\n" + b);

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "🔍 " + query,
                description,
                "#00e5c0",
                imageUrls,
                event.isFollowUp()
        );
    }

    private void handleBuscar(CommandReceivedEvent event) {
        Map<String, String> opts = event.getOptions();
        String termo = opts.get("termo");
        String autor = opts.get("autor");
        String tipo = opts.get("tipo");

        List<MediaDocument> results = internalSearch.search(event.getGuildId(), termo, autor, tipo, 10);

        if (results.isEmpty()) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Nenhuma midia encontrada para: " + termo);
            return;
        }

        String description = results.stream()
                .map(doc -> String.format("**%s** - %s `%s` [ver](%s)",
                        doc.getAuthorName(), doc.getMediaType(), doc.getId(), doc.getPermanentUrl()))
                .reduce("", (a, b) -> a + "\n" + b);

        List<String> thumbnails = results.stream()
                .filter(d -> d.getThumbnailUrl() != null)
                .map(MediaDocument::getThumbnailUrl)
                .limit(4)
                .toList();

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "📁 Resultados: " + termo,
                description,
                "#f4c430",
                thumbnails,
                event.isFollowUp()
        );
    }

    private void handleHistorico(CommandReceivedEvent event) {
        Map<String, String> opts = event.getOptions();
        String tipo = opts.get("tipo");
        int limit = parseIntOrDefault(opts.get("limit"), 10);

        List<MediaDocument> results = internalSearch.getHistory(
                event.getGuildId(), event.getChannelId(), tipo, limit);

        if (results.isEmpty()) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Nenhuma midia encontrada no historico deste canal.");
            return;
        }

        String description = results.stream()
                .map(doc -> String.format("`%s` **%s** por %s - %s",
                        doc.getId(), doc.getMediaType(), doc.getAuthorName(),
                        doc.getCreatedAt() != null ? doc.getCreatedAt().toString().substring(0, 10) : "?"))
                .reduce("", (a, b) -> a + "\n" + b);

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "📜 Historico do canal",
                description,
                "#5865f2",
                null,
                event.isFollowUp()
        );
    }

    private void handleMidia(CommandReceivedEvent event) {
        String id = event.getOptions().get("id");
        if (id == null) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Informe o ID da midia.");
            return;
        }

        MediaDocument doc = internalSearch.findById(id);
        if (doc == null) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Midia `" + id + "` nao encontrada.");
            return;
        }

        String description = String.format("""
                **Autor:** %s
                **Tipo:** %s
                **MIME:** %s
                **Tamanho:** %s KB
                **Tags:** %s""",
                doc.getAuthorName(),
                doc.getMediaType(),
                doc.getMimeType(),
                doc.getSizeBytes() != null ? doc.getSizeBytes() / 1024 : "?",
                doc.getTags() != null && !doc.getTags().isEmpty() ? String.join(", ", doc.getTags()) : "nenhuma");

        sender.sendEmbed(
                event.getChannelId(),
                event.getInteractionToken(),
                "🖼️ Midia #" + id,
                description,
                "#00e5c0",
                doc.getPermanentUrl() != null ? List.of(doc.getPermanentUrl()) : null,
                event.isFollowUp()
        );
    }

    private void handleTag(CommandReceivedEvent event) {
        Map<String, String> opts = event.getOptions();
        String mediaId = opts.get("midia-id");
        String tagsStr = opts.get("tags");

        if (mediaId == null || tagsStr == null) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Uso: `/tag <midia-id> <tags>`");
            return;
        }

        MediaDocument doc = internalSearch.findById(mediaId);
        if (doc == null) {
            sender.sendMessage(event.getChannelId(), event.getInteractionToken(), "Midia `" + mediaId + "` nao encontrada.");
            return;
        }

        List<String> newTags = List.of(tagsStr.split("\\s+"));
        doc.setTags(newTags);

        sender.sendMessage(event.getChannelId(), event.getInteractionToken(),
                "Tags atualizadas para midia `" + mediaId + "`: " + String.join(", ", newTags));
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
