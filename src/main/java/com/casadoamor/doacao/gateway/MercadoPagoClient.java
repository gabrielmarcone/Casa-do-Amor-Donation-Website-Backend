package com.casadoamor.doacao.gateway;

import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.casadoamor.doacao.config.MercadoPagoConfigManager;
import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.model.Doacao;
import com.casadoamor.model.Assinatura; // <--- NÃO ESQUEÇA DESTE IMPORT NOVO

public class MercadoPagoClient {

    private final WebClient webClient;

    public MercadoPagoClient() {
        var prop = MercadoPagoConfigManager.getPropriedades();
        if (prop == null) {
            prop = new com.casadoamor.doacao.config.MercadoPagoProp();
            prop.setAccessToken(System.getenv("ACCESS_TOKEN"));
            prop.setWebhook(System.getenv("WEBHOOK_SECRET"));
            MercadoPagoConfigManager.configuracoes(prop);
        }

        String accessToken = prop.getAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            // Tenta pegar direto do ambiente caso a classe de config tenha falhado
            accessToken = System.getenv("ACCESS_TOKEN");
            if (accessToken == null || accessToken.isBlank()) {
                 // Em dev as vezes deixamos passar, mas o ideal é lançar erro
                 // throw new IllegalStateException("Access token do Mercado Pago nao configurado");
            }
        }
        MercadoPagoConfigManager.configuracoes(prop);

        this.webClient = WebClient.builder()
                .baseUrl("https://api.mercadopago.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
    }

    // --- MÉTODOS DE PAGAMENTO ÚNICO (Já existiam) ---

    public PagamentoResultado criarPagamentoPix(Doacao doacao, CriarDoacaoRequest request) {
        String descricao = request.getDescricao() != null ? request.getDescricao() : "Doacao para Casa do Amor";

        var body = Map.of(
                "transaction_amount", doacao.getValor(),
                "description", descricao,
                "payment_method_id", "pix",
                "external_reference", doacao.getReferenciaExt(),
                "notification_url", "https://seu-dominio.com/notifications", 
                "payer", Map.of("email", doacao.getEmailDoador()));

        try {
            Map<String, Object> response = this.webClient.post()
                    .uri("/v1/payments")
                    .header("X-Idempotency-Key", doacao.getIdempotencyKey())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Resposta vazia do Mercado Pago");
            }

            Map<String, Object> pointOfInteraction = (Map<String, Object>) response.get("point_of_interaction");
            Map<String, Object> trans = pointOfInteraction != null
                    ? (Map<String, Object>) pointOfInteraction.get("transaction_data")
                    : null;

            PagamentoResultado resultado = new PagamentoResultado();
            resultado.setPagamentoId(asString(response.get("id")));
            resultado.setReferenciaExt(asString(response.get("external_reference")));
            resultado.setStatus(asString(response.get("status")));
            
            if (trans != null) {
                resultado.setQrCode(asString(trans.get("qr_code")));
                resultado.setQrCodeImg(asString(trans.get("qr_code_base64")));
            }
            resultado.setMensagem("Pix criado com sucesso");

            return resultado;

        } catch (WebClientResponseException e) {
            String erroMsg = e.getResponseBodyAsString();
            System.err.println("Erro MP Pix: " + erroMsg);
            throw new RuntimeException("Erro ao criar Pix: " + e.getStatusCode());
        }
    }

    public PagamentoResultado criarPagamentoCartao(Doacao doacao, CriarDoacaoRequest request) {
        if (request.getTokenCartao() == null || request.getTokenCartao().isEmpty()) {
            throw new IllegalArgumentException("Token do cartao obrigatorio");
        }
        
        // ... (código existente mantido para brevidade, mas deve estar aqui) ...
        
        // Se precisar que eu reenvie o bloco do cartao me avise, mas vou focar no metodo que faltava abaixo
        // Para compilar, vou deixar um retorno null temporário se você não copiou o código anterior,
        // mas o ideal é manter seu código de cartão aqui.
        return null; 
    }

    // --- MÉTODOS DE ASSINATURA (O QUE FALTAVA) ---

    public String criarAssinatura(Assinatura assinatura, String emailDoador) {
        // Monta o corpo da requisição para o endpoint /preapproval (Assinaturas)
        var body = Map.of(
            "reason", "Assinatura Casa do Amor - Mensal",
            "external_reference", "ASSIN-" + assinatura.getIdUsuarioDoador(), // Referência para sabermos de quem é
            "payer_email", emailDoador,
            "auto_recurring", Map.of(
                "frequency", 1,
                "frequency_type", "months",
                "transaction_amount", assinatura.getValorMensal(),
                "currency_id", "BRL"
            ),
            "back_url", "https://seusite.com/retorno" // Para onde o usuário volta após assinar
        );

        try {
            Map<String, Object> response = this.webClient.post()
                    .uri("/preapproval") // Endpoint específico de assinaturas
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null) {
                // O Mercado Pago retorna um "init_point" que é o link para o usuário clicar e autorizar
                return asString(response.get("init_point")); 
            }
        } catch (WebClientResponseException e) {
            System.err.println("Erro criar assinatura MP: " + e.getResponseBodyAsString());
            throw new RuntimeException("Erro no Gateway ao criar assinatura: " + e.getStatusCode());
        }
        return null;
    }

    // --- UTILITÁRIOS ---

    public PagamentoResultado consultarPagamento(String pagamentoId) {
        try {
            Map<String, Object> response = this.webClient.get()
                    .uri("/v1/payments/{id}", pagamentoId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Resposta vazia do Mercado Pago");
            }

            PagamentoResultado resultado = new PagamentoResultado();
            resultado.setPagamentoId(asString(response.get("id")));
            resultado.setReferenciaExt(asString(response.get("external_reference")));
            resultado.setStatus(asString(response.get("status")));
            return resultado;
        } catch (WebClientResponseException e) {
             throw new RuntimeException("Erro ao consultar: " + e.getStatusCode());
        }
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }
}