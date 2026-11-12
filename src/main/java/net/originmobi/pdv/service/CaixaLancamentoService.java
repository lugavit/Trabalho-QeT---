package net.originmobi.pdv.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.repository.CaixaLancamentoRepository;

@Service
public class CaixaLancamentoService {

	private CaixaLancamentoRepository caixaLancamento;
	UsuarioService usuarios;

	public CaixaLancamentoService() {
		// Construtor padrão
	}

	@Autowired
	public CaixaLancamentoService(CaixaLancamentoRepository caixaLancamento, UsuarioService usuarios) {
		this.caixaLancamento = caixaLancamento;
		this.usuarios = usuarios;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public String lancamento(CaixaLancamento lancamento) {
		Timestamp dataHoraAtual = new Timestamp(System.currentTimeMillis());

		try {
			lancamento.setData_cadastro(dataHoraAtual);

			if (!lancamento.getCaixa().isPresent()
					&& lancamento.getCaixa().map(Caixa::getData_fechamento).isPresent()) {
				throw new IllegalStateException("Nenhum caixa aberto");
			}

			if (lancamento.getEstilo().equals(EstiloLancamento.SAIDA)) {
				Optional<Double> vlTotalCaixa = lancamento.getCaixa().map(Caixa::getValor_total);

				if (lancamento.getValor() > vlTotalCaixa.get()) {
					return "Saldo insuficiente para realizar esta operação";
				}
			}

			if (lancamento.getEstilo().equals(EstiloLancamento.SAIDA) && lancamento.getValor() > 0) {
				Double valorNegativo = (lancamento.getValor() * -1);
				lancamento.setValor(valorNegativo);
			}

			ajustarObservacaoSeVazia(lancamento);

		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		try {
			caixaLancamento.save(lancamento);
		} catch (Exception e) {
			throw new IllegalStateException("Erro ao realizar lançamento, chame o suporte", e);
		}

		return "Lançamento realizado com sucesso";
	}

	private void ajustarObservacaoSeVazia(CaixaLancamento lancamento) {
		if (lancamento.getObservacao().isEmpty()) {
			String observacao = "";
			if (lancamento.getTipo().equals(TipoLancamento.SANGRIA)) {
				observacao = "Sangria de caixa";
			} else if (lancamento.getTipo().equals(TipoLancamento.SUPRIMENTO)) {
				observacao = "Suprimento de caixa";
			}
			lancamento.setObservacao(observacao);
		}
	}

	public List<CaixaLancamento> lancamentosDoCaixa(Caixa caixa) {
		return caixaLancamento.findByCaixaEquals(caixa);
	}
}