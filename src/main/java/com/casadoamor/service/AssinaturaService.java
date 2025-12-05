package com.casadoamor.service;

import com.casadoamor.dao.AssinaturaDAO;
import com.casadoamor.dao.DoadorDAO;
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
    private final DoadorDAO doadorDAO = new DoadorDAO();
    private final MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();

    // --- CRIAÇÃO (CREATE) ---
    public String criarAssinatura(CriarDoacaoRequest request) {
        if (request.getValor() == null || request.getValor().compareTo(new BigDecimal("10.00")) < 0) {
            throw new IllegalArgumentException("O valor mínimo para assinatura é de R$ 10,00.");
        }

        Doador doador = doadorDAO.buscarPorEmail(request.getEmailDoador());
        if (doador == null) {
            doador = new Doador();
            doador.setNome(request.getNomeDoador());
            doador.setEmail(request.getEmailDoador());
            doador.setCpf(request.getDocumentoNumero());
            doador.setTelefone(request.getTelefone() != null ? request.getTelefone() : "");
            doador.setDataCadastro(LocalDate.now());
            doadorDAO.salvar(doador);
        }

        Assinatura assinatura = new Assinatura();
        assinatura.setIdUsuarioDoador(doador.getIdUsuario());
        assinatura.setValorMensal(request.getValor());
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDiaCobranca(LocalDate.now().getDayOfMonth());
        assinatura.setStatusAssinatura(StatusAssinatura.ATIVA);

        String linkAprovacao = mercadoPagoClient.criarAssinatura(assinatura, request.getEmailDoador());
        if (linkAprovacao == null) {
            throw new RuntimeException("Erro ao comunicar com Mercado Pago para criar assinatura.");
        }

        assinatura.setIdAssinaturaGateway("PENDENTE");
        assinaturaDAO.salvar(assinatura);

        return linkAprovacao;
    }

    // --- LEITURA (READ) ---
    public List<Assinatura> listarTodas() {
        return assinaturaDAO.listar();
    }

    // --- CANCELAMENTO (DELETE LÓGICO) ---
    // Este método cancela no banco E no gateway de pagamento
    public void cancelarAssinatura(Long idAssinatura) {
        Assinatura assinatura = assinaturaDAO.buscarPorId(idAssinatura);
        
        if (assinatura == null) throw new RuntimeException("Assinatura não encontrada.");
        
        if (assinatura.getStatusAssinatura() == StatusAssinatura.CANCELADA) {
            throw new RuntimeException("Assinatura já está cancelada.");
        }

        // Se tiver ID válido, cancela a cobrança futura no MP
        if (assinatura.getIdAssinaturaGateway() != null && !assinatura.getIdAssinaturaGateway().equals("PENDENTE")) {
            mercadoPagoClient.cancelarAssinatura(assinatura.getIdAssinaturaGateway());
        }

        assinaturaDAO.atualizarStatus(idAssinatura, StatusAssinatura.CANCELADA);
    }

    // --- ATUALIZAÇÃO MANUAL (UPDATE - NOVO!) ---
    // Permite ao admin forçar um status (ex: marcar Inadimplente ou Reativar manualmente)
    public void alterarStatusManual(Long idAssinatura, StatusAssinatura novoStatus) {
        Assinatura assinatura = assinaturaDAO.buscarPorId(idAssinatura);
        if (assinatura == null) {
            throw new RuntimeException("Assinatura não encontrada.");
        }
        // Apenas atualiza no banco local, sem chamar API externa
        assinaturaDAO.atualizarStatus(idAssinatura, novoStatus);
    }
}