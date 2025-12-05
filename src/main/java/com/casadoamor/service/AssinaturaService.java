package com.casadoamor.service;

import com.casadoamor.dao.AssinaturaDAO;
import com.casadoamor.dao.DoadorDAO; // Supondo que você tenha ou use UsuarioDAO para validar
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.dto.CriarDoacaoRequest; // Reaproveitando o DTO ou crie um CriarAssinaturaRequest
import com.casadoamor.enums.StatusAssinatura;
import com.casadoamor.model.Assinatura;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AssinaturaService {

    private final AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private final MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();

    public String criarAssinatura(CriarDoacaoRequest request) {
        // 1. Montar o objeto Assinatura local
        Assinatura assinatura = new Assinatura();
        
        // ATENÇÃO: Aqui você precisa do ID do doador logado ou cadastrado. 
        // Para simplificar, vou assumir que o request traz ou que você busca pelo email.
        // assinatura.setIdUsuarioDoador(...); 
        
        assinatura.setValorMensal(request.getValor());
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDiaCobranca(LocalDate.now().getDayOfMonth()); // RN-003: Mesma data da adesão
        assinatura.setStatusAssinatura(StatusAssinatura.ATIVA); // Começa ativa (ou pendente até o MP confirmar)

        // 2. Chamar a API do Mercado Pago
        // O método criarAssinatura no MPClient (que te passei antes) deve retornar o Link ou ID
        String linkOuIdGateway = mercadoPagoClient.criarAssinatura(assinatura, request.getEmailDoador());
        
        if (linkOuIdGateway == null) {
            throw new RuntimeException("Erro ao comunicar com Mercado Pago para criar assinatura.");
        }

        // 3. Salvar ID do Gateway e Persistir no Banco
        assinatura.setIdAssinaturaGateway(linkOuIdGateway); // Guarda o ID para saber quem é quem depois
        
        // Se você ainda não tem um doador cadastrado no banco, precisaria cadastrar antes de salvar a assinatura
        // assinaturaDAO.salvar(assinatura);

        return linkOuIdGateway; // Retorna o link para o usuário ir pagar/autorizar
    }
}