package com.casadoamor.service;

import com.casadoamor.dao.AssinaturaDAO;
import com.casadoamor.dao.DoadorDAO; // Agora usamos este DAO
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.enums.StatusAssinatura;
import com.casadoamor.model.Assinatura;
import com.casadoamor.model.Doador;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AssinaturaService {

    private final AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private final DoadorDAO doadorDAO = new DoadorDAO(); // DAO para gerenciar o usuário
    private final MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();

    public String criarAssinatura(CriarDoacaoRequest request) {
        
        // 1. Validação RN-002: Valor mínimo
        if (request.getValor() == null || request.getValor().compareTo(new BigDecimal("10.00")) < 0) {
            throw new IllegalArgumentException("O valor mínimo para assinatura é de R$ 10,00.");
        }

        // --- 2. INTEGRAÇÃO DO DOADOR (Lógica "Find or Create") ---
        // Verifica se o doador já existe pelo email
        Doador doador = doadorDAO.buscarPorEmail(request.getEmailDoador());

        if (doador == null) {
            // Se não existe, cria um novo cadastro de Doador na hora
            doador = new Doador();
            doador.setNome(request.getNomeDoador());
            doador.setEmail(request.getEmailDoador());
            // Usa o documento do pagamento como CPF do cadastro
            doador.setCpf(request.getDocumentoNumero()); 
            // Como o request de doação simplificado não tem telefone, deixamos null ou vazio
            doador.setTelefone(""); 
            doador.setDataCadastro(LocalDate.now());

            // Salva no banco e o objeto 'doador' volta preenchido com o novo ID
            doadorDAO.salvar(doador); 
        }

        // 3. Montar o objeto Assinatura local vinculado ao ID real
        Assinatura assinatura = new Assinatura();
        
        // AGORA SIM: Usamos o ID do doador encontrado ou recém-criado
        assinatura.setIdUsuarioDoador(doador.getIdUsuario()); 

        assinatura.setValorMensal(request.getValor());
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDiaCobranca(LocalDate.now().getDayOfMonth()); // RN-003
        assinatura.setStatusAssinatura(StatusAssinatura.ATIVA);

        // 4. Chamar a API do Mercado Pago
        String linkAprovacao = mercadoPagoClient.criarAssinatura(assinatura, request.getEmailDoador());
        
        if (linkAprovacao == null) {
            throw new RuntimeException("Erro ao comunicar com Mercado Pago para criar assinatura.");
        }

        // 5. Salvar ID do Gateway e persistir no banco
        assinatura.setIdAssinaturaGateway("PENDENTE"); // Será atualizado via Webhook futuramente
        
        assinaturaDAO.salvar(assinatura);

        return linkAprovacao;
    }

    public List<Assinatura> listarTodas() {
        return assinaturaDAO.listar();
    }

    public void cancelarAssinatura(Long idAssinatura) {
        Assinatura assinatura = assinaturaDAO.buscarPorId(idAssinatura);
        
        if (assinatura == null) {
            throw new RuntimeException("Assinatura não encontrada.");
        }
        
        if (assinatura.getStatusAssinatura() == StatusAssinatura.CANCELADA) {
            throw new RuntimeException("Assinatura já está cancelada.");
        }

        if (assinatura.getIdAssinaturaGateway() != null && !assinatura.getIdAssinaturaGateway().equals("PENDENTE")) {
            mercadoPagoClient.cancelarAssinatura(assinatura.getIdAssinaturaGateway());
        }

        assinaturaDAO.atualizarStatus(idAssinatura, StatusAssinatura.CANCELADA);
    }
}