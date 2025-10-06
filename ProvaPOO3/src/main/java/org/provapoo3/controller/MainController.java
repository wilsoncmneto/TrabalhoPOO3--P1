package org.provapoo3.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.provapoo3.model.Fornecedor;
import org.provapoo3.model.Medicamento;
import org.provapoo3.persistence.CsvMedicamentoRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

public class MainController {

    // Medicamento
    @FXML private TextField txtCodigo, txtNome, txtDescricao, txtPrincipio, txtQuantidade, txtPreco;
    @FXML private DatePicker dateValidade;
    @FXML private CheckBox chkControlado;

    // Fornecedor
    @FXML private TextField txtCnpj, txtRazao, txtTelefone, txtEmail, txtCidade, txtEstado;

    // Tabela
    @FXML private TableView<Medicamento> tblMedicamentos;
    @FXML private TableColumn<Medicamento, String>  colCodigo, colNome, colPrincipio, colFornecedor;
    @FXML private TableColumn<Medicamento, LocalDate> colValidade;
    @FXML private TableColumn<Medicamento, Integer> colQtd;
    @FXML private TableColumn<Medicamento, BigDecimal> colPreco;
    @FXML private TableColumn<Medicamento, Boolean> colControlado;

    // Repositório CSV (persistência)
    private CsvMedicamentoRepository repo;

    // INICIALIZAÇÃO 
    @FXML
    public void initialize() {
        // CSV 
        this.repo = new CsvMedicamentoRepository(Path.of("dados/medicamentos.csv"));

        // Ligações das colunas 
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPrincipio.setCellValueFactory(new PropertyValueFactory<>("principioAtivo"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidadeEstoque"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colControlado.setCellValueFactory(new PropertyValueFactory<>("controlado"));
        colFornecedor.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFornecedor() != null ? c.getValue().getFornecedor().getRazaoSocial() : ""
                ));

        atualizarTabela(); 
    }

    private void atualizarTabela() {
        tblMedicamentos.setItems(FXCollections.observableArrayList(repo.findAll()));
    }

    // AÇÕES DA TELA

    // Cadastrar novo medicamento 
    @FXML
    private void onSalvar() {
        try {
            // 1) Monta objetos a partir dos campos
            Fornecedor fornecedor = montarFornecedorDosCampos();
            Medicamento med = montarMedicamentoDosCampos(fornecedor);

            // 2) Validação
            validarFornecedor(fornecedor);
            validarMedicamento(med);

            // 3) Regra simples: impedir duplicidade de código
            if (repo.findByCodigo(med.getCodigo()).isPresent()) {
                throw new IllegalArgumentException("Já existe medicamento com esse código.");
            }

            // 4) Persistir (salva imediatamente no CSV — requisito de salvar após operação)
            repo.save(med);

            limparCampos();
            atualizarTabela();
            info("Sucesso", "Medicamento cadastrado.");
        } catch (IllegalArgumentException ex) {
            erro("Validação", ex.getMessage());
        } catch (Exception ex) {
            erro("Erro", ex.getMessage());
        }
    }

    // Excluir por código
    @FXML
    private void onExcluir() {
        try {
            validarCodigo7(txtCodigo.getText());
            boolean ok = repo.deleteByCodigo(txtCodigo.getText());
            if (ok) {
                atualizarTabela();
                info("Sucesso", "Medicamento excluído.");
            } else {
                info("Aviso", "Código não encontrado.");
            }
        } catch (IllegalArgumentException ex) {
            erro("Validação", ex.getMessage());
        } catch (Exception ex) {
            erro("Erro", ex.getMessage());
        }
    }

    // Consultar por código
    @FXML
    private void onBuscar() {
        try {
            validarCodigo7(txtCodigo.getText());
            var medOpt = repo.findByCodigo(txtCodigo.getText());
            if (medOpt.isEmpty()) {
                info("Aviso", "Medicamento não encontrado.");
                return;
            }
            preencherCampos(medOpt.get());
        } catch (IllegalArgumentException ex) {
            erro("Validação", ex.getMessage());
        } catch (Exception ex) {
            erro("Erro", ex.getMessage());
        }
    }

    // Listar todos 
    @FXML
    private void onListar() {
        atualizarTabela();
    }

    // MONTAGEM DOS OBJETOS

    private Fornecedor montarFornecedorDosCampos() {
        return new Fornecedor(
                txtCnpj.getText(),
                txtRazao.getText(),
                txtTelefone.getText(),
                txtEmail.getText(),
                txtCidade.getText(),
                txtEstado.getText()
        );
    }

    private Medicamento montarMedicamentoDosCampos(Fornecedor f) {
        return new Medicamento(
                txtCodigo.getText(),
                txtNome.getText(),
                txtDescricao.getText(),
                txtPrincipio.getText(),
                dateValidade.getValue(),
                parseInt(txtQuantidade.getText(), "Quantidade inválida."),
                parsePreco(txtPreco.getText(), "Preço inválido."),
                chkControlado.isSelected(),
                f
        );
    }

    // VALIDAÇÃO CENTRALIZADA

    private void validarMedicamento(Medicamento m) {
        validarCodigo7(m.getCodigo());                                    // Código: 7 alfanuméricos
        validarNaoVazioMin(m.getNome(), 2, "Nome inválido (mín. 2).");    // Nome: não vazio
        validarValidadeFutura(m.getDataValidade());                       // Data de validade futura
        validarQuantidadeNaoNegativa(m.getQuantidadeEstoque());           // Quantidade >= 0
        validarPrecoPositivo(m.getPreco());                               // Preço > 0
        if (m.getFornecedor() == null)
            throw new IllegalArgumentException("Fornecedor é obrigatório.");
    }

    private void validarFornecedor(Fornecedor f) {
        validarCnpj(f.getCnpj());                                      // CNPJ válido com DV
        validarNaoVazioMin(f.getRazaoSocial(), 2, "Razão social inválida (mín. 2).");
    }

    // HELPERS DE VALIDAÇÃO 

    private void validarCodigo7(String codigo) {
        if (codigo == null || !codigo.matches("^[A-Za-z0-9]{7}$"))
            throw new IllegalArgumentException("Código inválido (7 caracteres alfanuméricos).");
    }

    private void validarNaoVazioMin(String s, int min, String msg) {
        if (s == null || s.trim().length() < min) throw new IllegalArgumentException(msg);
    }

    private void validarValidadeFutura(LocalDate data) {
        if (data == null || data.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Data de validade no passado.");
    }

    private void validarQuantidadeNaoNegativa(int qtd) {
        if (qtd < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
    }

    private void validarPrecoPositivo(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0)
            throw new IllegalArgumentException("Preço deve ser positivo.");
    }

    // Validação de CNPJ com cálculo dos dígitos verificadores
    private void validarCnpj(String cnpj) {
        if (cnpj == null) throw new IllegalArgumentException("CNPJ inválido.");
        String n = cnpj.replaceAll("\\D", "");
        if (!n.matches("\\d{14}")) throw new IllegalArgumentException("CNPJ precisa ter 14 dígitos.");
        if (n.chars().distinct().count() == 1) throw new IllegalArgumentException("CNPJ inválido.");

        int dv1 = dv(n.substring(0, 12), new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
        int dv2 = dv(n.substring(0, 12) + dv1, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});
        if (n.charAt(12) - '0' != dv1 || n.charAt(13) - '0' != dv2)
            throw new IllegalArgumentException("CNPJ com dígitos verificadores inválidos.");
    }

    private int dv(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += (base.charAt(i) - '0') * pesos[i];
        int r = soma % 11;
        return (r < 2) ? 0 : 11 - r;
    }

    // PARSERS COM MENSAGEM CLARA

    private int parseInt(String s, String msgErro) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msgErro); }
    }

    private BigDecimal parsePreco(String s, String msgErro) {
        try { return new BigDecimal(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException(msgErro); }
    }

    // UTIL (preencher/limpar/alerts)

    private void preencherCampos(Medicamento m) {
        txtCodigo.setText(m.getCodigo());
        txtNome.setText(m.getNome());
        txtDescricao.setText(m.getDescricao());
        txtPrincipio.setText(m.getPrincipioAtivo());
        dateValidade.setValue(m.getDataValidade());
        txtQuantidade.setText(String.valueOf(m.getQuantidadeEstoque()));
        txtPreco.setText(m.getPreco().toPlainString());
        chkControlado.setSelected(m.isControlado());

        var f = m.getFornecedor();
        if (f != null) {
            txtCnpj.setText(f.getCnpj());
            txtRazao.setText(f.getRazaoSocial());
            txtTelefone.setText(f.getTelefone());
            txtEmail.setText(f.getEmail());
            txtCidade.setText(f.getCidade());
            txtEstado.setText(f.getEstado());
        }
    }

    private void limparCampos() {
        txtCodigo.clear(); txtNome.clear(); txtDescricao.clear(); txtPrincipio.clear();
        dateValidade.setValue(null);
        txtQuantidade.clear(); txtPreco.clear(); chkControlado.setSelected(false);
        txtCnpj.clear(); txtRazao.clear(); txtTelefone.clear(); txtEmail.clear(); txtCidade.clear(); txtEstado.clear();
    }

    private void info(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
    private void erro(String t, String m) { new Alert(Alert.AlertType.ERROR, m).show(); }
}
