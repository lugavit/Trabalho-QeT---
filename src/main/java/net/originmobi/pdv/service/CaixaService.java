package net.originmobi.pdv.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import net.originmobi.pdv.singleton.Aplicacao;

@Service
public class CaixaService {

	private static final Logger logger = LoggerFactory.getLogger(CaixaService.class);

	private final CaixaRepository caixas;
	private final UsuarioService usuarios;
	private final CaixaLancamentoService lancamentos;

	public CaixaService() {
		this(null, null, null);
	}

	@Autowired
	public CaixaService(CaixaRepository caixas, UsuarioService usuarios, CaixaLancamentoService lancamentos) {
		this.caixas = caixas;
		this.usuarios = usuarios;
		this.lancamentos = lancamentos;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Long cadastro(Caixa caixa) {
		if (caixa.getTipo().equals(CaixaTipo.CAIXA) && caixaIsAberto()) {
			throw new IllegalStateException("Existe caixa de dias anteriores em aberto, favor verifique");
		}

		Double vlAbertura = caixa.getValor_abertura() == null ? 0.0 : caixa.getValor_abertura();
		caixa.setValor_abertura(vlAbertura);

		if (caixa.getValor_abertura() < 0) {
			throw new IllegalArgumentException("Valor informado é inválido");
		}

		Aplicacao aplicacao = Aplicacao.getInstancia();
		Usuario usuarioAtual = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

		String descricaoDefinida = resolverDescricao(caixa);
		LocalDate dataAtual = LocalDate.now();

		caixa.setDescricao(descricaoDefinida);
		caixa.setUsuario(usuarioAtual);
		caixa.setData_cadastro(Date.valueOf(dataAtual));

		if (caixa.getTipo().equals(CaixaTipo.BANCO)) {
			logger.debug("Agência informada: {} | Conta informada: {}", caixa.getAgencia(), caixa.getConta());
			caixa.setAgencia(caixa.getAgencia().replaceAll("\\D", ""));
			caixa.setConta(caixa.getConta().replaceAll("\\D", ""));
		}

		try {
			caixas.save(caixa);
		} catch (Exception e) {
			logger.error("Erro ao salvar caixa no processo de abertura", e);
			throw new IllegalStateException("Erro no processo de abertura, chame o suporte técnico", e);
		}

		if (caixa.getValor_abertura() > 0) {
			registrarLancamentoAbertura(caixa, usuarioAtual);
		} else {
			caixa.setValor_total(0.0);
		}

		return caixa.getCodigo();
	}

	private String resolverDescricao(Caixa caixa) {
		if (caixa.getTipo().equals(CaixaTipo.CAIXA)) {
			return caixa.getDescricao().isEmpty() ? "Caixa diário" : caixa.getDescricao();
		} else if (caixa.getTipo().equals(CaixaTipo.COFRE)) {
			return caixa.getDescricao().isEmpty() ? "Cofre" : caixa.getDescricao();
		} else if (caixa.getTipo().equals(CaixaTipo.BANCO)) {
			return caixa.getDescricao().isEmpty() ? "Banco" : caixa.getDescricao();
		}
		return caixa.getDescricao();
	}

	private void registrarLancamentoAbertura(Caixa caixa, Usuario usuarioAtual) {
		try {
			String observacao = caixa.getTipo().equals(CaixaTipo.CAIXA) ? "Abertura de caixa"
					: caixa.getTipo().equals(CaixaTipo.COFRE) ? "Abertura de cofre" : "Abertura de banco";

			CaixaLancamento lancamento = new CaixaLancamento(observacao, caixa.getValor_abertura(),
					TipoLancamento.SALDOINICIAL, EstiloLancamento.ENTRADA, caixa, usuarioAtual);

			lancamentos.lancamento(lancamento);
		} catch (Exception e) {
			logger.error("Erro ao gerar lançamento inicial na abertura do caixa", e);
			throw new IllegalStateException("Erro no processo, chame o suporte", e);
		}
	}

	public String fechaCaixa(Long caixa, String senha) {
		Aplicacao aplicacao = Aplicacao.getInstancia();
		Usuario usuarioAtual = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

		BCryptPasswordEncoder decode = new BCryptPasswordEncoder();

		if (senha.equals("")) {
			return "Favor, informe a senha";
		}

		if (decode.matches(senha, usuarioAtual.getSenha())) {
			Optional<Caixa> caixaAtual = caixas.findById(caixa);

			if (!caixaAtual.isPresent()) {
				throw new IllegalStateException("Caixa não encontrado");
			}

			if (caixaAtual.map(c -> c.getData_fechamento()).isPresent()) {
				throw new IllegalStateException("Caixa já esta fechado");
			}

			Double valorTotal = caixaAtual.map(c -> c.getValor_total()).orElse(0.0);

			Timestamp dataHoraAtual = new Timestamp(System.currentTimeMillis());
			caixaAtual.get().setData_fechamento(dataHoraAtual);
			caixaAtual.get().setValor_fechamento(valorTotal);

			try {
				caixas.save(caixaAtual.get());
			} catch (Exception e) {
				logger.error("Erro ao fechar o caixa de código {}", caixa, e);
				throw new IllegalStateException("Ocorreu um erro ao fechar o caixa, chame o suporte", e);
			}

			return "Caixa fechado com sucesso";
		} else {
			return "Senha incorreta, favor verifique";
		}
	}

	public boolean caixaIsAberto() {
		return caixas.caixaAberto().isPresent();
	}

	public List<Caixa> listaTodos() {
		return caixas.findByCodigoOrdenado();
	}

	public List<Caixa> listarCaixas(CaixaFilter filter) {
		if (filter.getData_cadastro() != null && !filter.getData_cadastro().equals("")) {
			filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
			return caixas.buscaCaixasPorDataAbertura(Date.valueOf(filter.getData_cadastro()));
		}

		return caixas.listaCaixasAbertos();
	}

	public Optional<Caixa> caixaAberto() {
		return caixas.caixaAberto();
	}

	public List<Caixa> caixasAbertos() {
		return caixas.caixasAbertos();
	}

	public Optional<Caixa> busca(Long codigo) {
		return caixas.findById(codigo);
	}

	public Optional<Caixa> buscaCaixaUsuario(String usuario) {
		Usuario usu = usuarios.buscaUsuario(usuario);
		return Optional.ofNullable(caixas.findByCaixaAbertoUsuario(usu.getCodigo()));
	}

	public List<Caixa> listaBancos() {
		return caixas.buscaBancos(CaixaTipo.BANCO);
	}

	public List<Caixa> listaCaixasAbertosTipo(CaixaTipo tipo) {
		return caixas.buscaCaixaTipo(tipo);
	}

	public List<Caixa> listaBancosAbertosTipoFilterBanco(CaixaTipo tipo, BancoFilter filter) {
		if (filter.getData_cadastro() != null && !filter.getData_cadastro().equals("")) {
			filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
			return caixas.buscaCaixaTipoData(tipo, Date.valueOf(filter.getData_cadastro()));
		}

		return caixas.buscaCaixaTipo(CaixaTipo.BANCO);
	}
}