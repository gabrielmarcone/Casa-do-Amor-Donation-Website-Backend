package com.casadoamor.doacao.config;

import com.mercadopago.MercadoPagoConfig;

public class MercadoPagoConfigManager{

  private static MercadoPagoProp propriedades;

  public static void configuracoes(MercadoPagoProp prop){
    propriedades = prop;
    MercadoPagoConfig.setAccessToken(propriedades.getAccessToken());
  }

  public static MercadoPagoProp getPropriedades() {
    return propriedades;
  }


}
