package com.casadoamor.doacao.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.casadoamor.doacao.service.DoacaoService;

public class PagamentoWebhookController {

  @Autowired
  private DoacaoService doacaoService;

  public ResponseEntity<String> receberWebhook(
    @RequestBody String payload,
    @RequestHeader(name = "x-signature", required = false) String signature,
    @RequestHeader(name = "x-request-id", required = false) String requestId
  ) {
    try {
      doacaoService.processarWebhook(payload, signature, requestId);
      return ResponseEntity.ok("OK");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Erro");
    }
  }

}
