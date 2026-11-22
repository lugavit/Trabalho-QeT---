package net.originmobi.pdv.singleton;

import org.springframework.security.core.context.SecurityContextHolder;

public class Aplicacao {

	private static Aplicacao aplicacao;

	public static synchronized Aplicacao getInstancia() {
		if (aplicacao == null) {
			aplicacao = new Aplicacao();
		}
		return aplicacao;
	}

	public String getUsuarioAtual() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
}
