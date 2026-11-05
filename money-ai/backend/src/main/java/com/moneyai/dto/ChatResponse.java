package com.moneyai.dto;

import java.util.List;

/**
 * @param reply        texto exibido no balão do assistente
 * @param dataChanged  true quando a conversa alterou lançamentos (a tela recarrega a lista)
 * @param suggestions  atalhos de continuação; o primeiro vira o botão principal
 */
public record ChatResponse(String reply, boolean dataChanged, List<String> suggestions) {

    public static ChatResponse of(String reply, String... suggestions) {
        return new ChatResponse(reply, false, List.of(suggestions));
    }

    public static ChatResponse changed(String reply, String... suggestions) {
        return new ChatResponse(reply, true, List.of(suggestions));
    }
}
