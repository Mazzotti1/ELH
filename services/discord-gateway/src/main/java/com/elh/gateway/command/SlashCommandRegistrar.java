package com.elh.gateway.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandRegistrar {

    private final JDA jda;

    @EventListener(ApplicationReadyEvent.class)
    public void registerCommands() {
        log.info("Registrando slash commands globais...");

        jda.updateCommands().addCommands(

                Commands.slash("img", "Busca imagens no Google")
                        .addOptions(
                                new OptionData(OptionType.STRING, "query", "Termo de busca", true),
                                new OptionData(OptionType.INTEGER, "n", "Quantidade de resultados (1-10)", false)
                        ),

                Commands.slash("buscar", "Busca midias salvas no servidor")
                        .addOptions(
                                new OptionData(OptionType.STRING, "termo", "Termo de busca", true),
                                new OptionData(OptionType.STRING, "autor", "Filtrar por autor", false),
                                new OptionData(OptionType.STRING, "tipo", "Filtrar por tipo: imagem, video, link", false),
                                new OptionData(OptionType.STRING, "mes", "Filtrar por mes", false)
                        ),

                Commands.slash("midia", "Recupera uma midia pelo ID")
                        .addOptions(
                                new OptionData(OptionType.STRING, "id", "ID da midia", true)
                        ),

                Commands.slash("top", "Ranking das midias mais reagidas")
                        .addOptions(
                                new OptionData(OptionType.STRING, "periodo", "semana, mes ou all-time", false)
                        ),

                Commands.slash("elh", "Conversa com o bot")
                        .addOptions(
                                new OptionData(OptionType.STRING, "mensagem", "Sua mensagem", true)
                        ),

                Commands.slash("poll", "Cria uma enquete manual")
                        .addOptions(
                                new OptionData(OptionType.STRING, "titulo", "Titulo da enquete", true)
                        ),

                Commands.slash("stats", "Estatisticas do servidor")
                        .addOptions(
                                new OptionData(OptionType.STRING, "member", "Ver stats de um membro especifico", false)
                        ),

                Commands.slash("tag", "Adiciona tags em uma midia")
                        .addOptions(
                                new OptionData(OptionType.STRING, "midia-id", "ID da midia", true),
                                new OptionData(OptionType.STRING, "tags", "Tags separadas por espaco", true)
                        ),

                Commands.slash("historico", "Timeline de midias do canal")
                        .addOptions(
                                new OptionData(OptionType.STRING, "tipo", "Filtrar: imagem, video, link", false),
                                new OptionData(OptionType.INTEGER, "limit", "Quantidade (max 25)", false)
                        )

        ).queue(
                cmds -> log.info("{} slash commands registrados com sucesso", cmds.size()),
                err -> log.error("Falha ao registrar slash commands: {}", err.getMessage())
        );
    }
}
