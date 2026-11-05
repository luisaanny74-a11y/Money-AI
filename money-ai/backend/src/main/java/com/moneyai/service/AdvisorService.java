package com.moneyai.service;

import com.moneyai.dto.ChatResponse;
import com.moneyai.dto.TransactionRequest;
import com.moneyai.model.TransactionType;
import com.moneyai.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Assistente financeiro: entende comandos em português e responde com base nos dados do banco. */
@Service
public class AdvisorService {

    private static final Pattern EXPENSE = Pattern.compile("(?:gastei|paguei|gasto)\\s+(?:r\\$\\s*)?(\\d+(?:[.,]\\d{1,2})?)\\s*(?:reais\\s*)?(?:em|com|de|no|na)?\\s*(.*)");
    private static final Pattern INCOME = Pattern.compile("(?:recebi|ganhei|entrada)\\s+(?:r\\$\\s*)?(\\d+(?:[.,]\\d{1,2})?)\\s*(?:reais\\s*)?(?:de|com|em|do|da)?\\s*(.*)");
    private static final NumberFormat BRL = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private final TransactionService transactions;
    private final TransactionRepository repository;

    public AdvisorService(TransactionService transactions, TransactionRepository repository) {
        this.transactions = transactions;
        this.repository = repository;
    }

    public ChatResponse reply(String message) {
        String text = normalize(message);

        Matcher expense = EXPENSE.matcher(text);
        if (expense.find()) return register(expense, TransactionType.EXPENSE);

        Matcher income = INCOME.matcher(text);
        if (income.find()) return register(income, TransactionType.INCOME);

        if (containsAny(text, "orcamento"))                                 return budget();
        if (containsAny(text, "categoria", "onde gasto", "analisar gastos")) return byCategory();
        if (containsAny(text, "aprender", "investimento", "investir"))       return investing();
        if (containsAny(text, "dica", "economizar", "poupar"))               return tips();
        if (containsAny(text, "saldo", "resumo", "quanto tenho", "situacao")) return summary();
        return help();
    }

    public String analyzeDocument(String fileName, String content) {
        List<BigDecimal> values = DocumentValueExtractor.extract(content);
        String preview = content.length() > 400 ? content.substring(0, 400) + "…" : content;
        if (values.isEmpty()) {
            return "Li %s, mas não encontrei valores em reais.\n\n%s".formatted(fileName, preview);
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return ("Li %s e encontrei %d valores, somando %s (maior: %s).\n"
                + "Para registrar, me diga por exemplo: \"gastei 120 em mercado\".\n\n%s")
                .formatted(fileName, values.size(), BRL.format(total), BRL.format(max), preview);
    }

    private ChatResponse register(Matcher m, TransactionType type) {
        BigDecimal amount = new BigDecimal(m.group(1).replace(",", "."));
        String what = m.group(2).isBlank() ? "Sem descrição" : m.group(2).trim();
        transactions.create(new TransactionRequest(capitalize(what), amount, type, capitalize(what), null));
        String verb = type == TransactionType.EXPENSE ? "Gasto" : "Receita";
        return ChatResponse.changed("%s de %s registrado em %s.".formatted(verb, BRL.format(amount), capitalize(what)), "Ver meu saldo", "Analisar gastos");
    }

    private ChatResponse summary() {
        BigDecimal income = repository.sumByType(TransactionType.INCOME);
        BigDecimal expense = repository.sumByType(TransactionType.EXPENSE);
        return ChatResponse.of("Receitas: %s\nDespesas: %s\nSaldo: %s"
                .formatted(BRL.format(income), BRL.format(expense), BRL.format(income.subtract(expense))),
                "Analisar gastos", "Dicas de economia");
    }

    private ChatResponse byCategory() {
        List<Object[]> rows = repository.expensesByCategory();
        if (rows.isEmpty()) return ChatResponse.of("Você ainda não tem despesas. Tente: \"gastei 50 em lanche\".");
        StringBuilder sb = new StringBuilder("Suas despesas por categoria:\n");
        rows.forEach(r -> sb.append("• %s: %s\n".formatted(r[0], BRL.format((BigDecimal) r[1]))));
        return ChatResponse.of(sb.toString().trim(), "Dicas de economia", "Criar orçamento");
    }

    private ChatResponse tips() {
        BigDecimal income = repository.sumByType(TransactionType.INCOME);
        BigDecimal expense = repository.sumByType(TransactionType.EXPENSE);
        if (income.signum() == 0) return ChatResponse.of("Registre suas receitas (\"recebi 3000 de salário\") para eu poder analisar seu orçamento.");

        BigDecimal ratio = expense.multiply(BigDecimal.valueOf(100)).divide(income, 0, RoundingMode.HALF_UP);
        String base = "Você gasta %s%% do que recebe. ".formatted(ratio);
        if (ratio.intValue() > 100) return ChatResponse.of(base + "Está no vermelho: corte primeiro as maiores categorias e evite parcelar novas compras.", "Analisar gastos", "Criar orçamento");
        if (ratio.intValue() > 80)  return ChatResponse.of(base + "Margem apertada. Tente a regra 50/30/20: 50% essenciais, 30% desejos, 20% reserva.", "Criar orçamento");
        return ChatResponse.of(base + "Ótimo! Guarde a sobra numa reserva de emergência (3 a 6 meses de despesas) antes de investir.", "Aprender a investir");
    }

    private ChatResponse budget() {
        BigDecimal income = repository.sumByType(TransactionType.INCOME);
        if (income.signum() == 0) return ChatResponse.of("Para montar seu orçamento preciso saber sua renda. Diga, por exemplo: \"recebi 3000 de salário\".");
        return ChatResponse.of("Orçamento 50/30/20 para %s:\n• Essenciais (50%%): %s\n• Desejos (30%%): %s\n• Reserva (20%%): %s"
                .formatted(BRL.format(income), BRL.format(share(income, 50)), BRL.format(share(income, 30)), BRL.format(share(income, 20))),
                "Analisar gastos", "Dicas de economia");
    }

    private ChatResponse investing() {
        return ChatResponse.of("O caminho mais seguro: 1) quitar dívidas caras, 2) montar reserva de emergência em renda fixa líquida (Tesouro Selic, CDB com liquidez diária), "
                + "3) só depois diversificar em renda fixa de prazo maior e renda variável. Isto é educação financeira, não recomendação de investimento.",
                "Criar orçamento");
    }

    private static BigDecimal share(BigDecimal total, int percent) {
        return total.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private ChatResponse help() {
        return ChatResponse.of(helpText(), "Ver meu saldo", "Analisar gastos");
    }

    private String helpText() {
        return """
                Posso ajudar com:
                • "saldo" ou "resumo": visão geral
                • "gastos por categoria"
                • "dicas para economizar"
                • "gastei 50 em mercado" / "recebi 3000 de salário"
                • Anexar PDF, DOCX ou PNG (extratos, faturas, notas)""";
    }

    private static boolean containsAny(String text, String... terms) {
        for (String t : terms) if (text.contains(t)) return true;
        return false;
    }

    private static String normalize(String s) {
        return Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
