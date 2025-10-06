package org.provapoo3.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.provapoo3.model.Fornecedor;
import org.provapoo3.model.Medicamento;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class MainController {

    // ================= MEDICAMENTO (fx:id do FXML) =================
    @FXML private TextField txtCodigo, txtNome, txtDescricao, txtPrincipio, txtQuantidade, txtPreco;
    @FXML private DatePicker dateValidade;
    @FXML private CheckBox chkControlado;

    // ================= FORNECEDOR (fx:id do FXML) =================
    @FXML private TextField txtCnpj, txtRazao, txtTelefone, txtEmail, txtCidade, txtEstado;

    // ================= TABELA DE MEDICAMENTOS =================
    @FXML private TableView<Medicamento> tblMedicamentos;
    @FXML private TableColumn<Medicamento, String>  colCodigo, colNome, colPrincipio, colFornecedor;
    @FXML private TableColumn<Medicamento, LocalDate> colValidade;
    @FXML private TableColumn<Medicamento, Integer> colQtd;
    @FXML private TableColumn<Medicamento, BigDecimal> colPreco;
    @FXML private TableColumn<Medicamento, Boolean> colControlado;

    // ================= TABELA DE FORNECEDORES =================
    @FXML private TableView<Fornecedor> tblFornecedores;
    @FXML private TableColumn<Fornecedor, String> colFCnpj, colFRazao, colFTelefone, colFEmail, colFCidade, colFEstado;

    // ================= CSVs e cache =================
    private final Path arquivoCSV = Path.of("dados/medicamentos.csv");
    private final Path arquivoFornecedoresCSV = Path.of("dados/fornecedores.csv");
    private final List<Medicamento> cache = new ArrayList<>();

    private static final String HEADER_MED =
            "codigo;nome;descricao;principioAtivo;dataValidade;quantidadeEstoque;preco;controlado;" +
                    "forn_cnpj;forn_razao;forn_telefone;forn_email;forn_cidade;forn_estado";
    private static final String HEADER_FORN =
            "cnpj;razaoSocial;telefone;email;cidade;estado";

    @FXML
    public void initialize() {
        // Colunas dos medicamentos
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPrincipio.setCellValueFactory(new PropertyValueFactory<>("principioAtivo"));
        colValidade.setCellValueFactory(new PropertyValueFactory<>("dataValidade"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidadeEstoque"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colControlado.setCellValueFactory(new PropertyValueFactory<>("controlado"));
        colFornecedor.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFornecedor()!=null ? c.getValue().getFornecedor().getRazaoSocial() : ""
                ));

        // Colunas dos fornecedores
        colFCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colFRazao.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colFTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colFEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));
        colFEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        carregarCSV();
        atualizarTabela();
        atualizarTabelaFornecedores();
    }

    private void atualizarTabela() {
        tblMedicamentos.setItems(FXCollections.observableArrayList(cache));
    }
    private void atualizarTabelaFornecedores() {
        tblFornecedores.setItems(FXCollections.observableArrayList(fornecedoresUnicos()));
    }

    // ================= AÇÕES =================
    @FXML
    private void onSalvar() {
        try {
            Fornecedor fornecedor = montarFornecedorDosCampos();
            Medicamento med = montarMedicamentoDosCampos(fornecedor);

            validarFornecedor(fornecedor);
            validarMedicamento(med);

            if (buscarPorCodigo(med.getCodigo()).isPresent())
                throw new IllegalArgumentException("Já existe medicamento com esse código.");

            cache.add(med);
            salvarCSV();
            salvarFornecedoresCSV();

            limparCampos();
            atualizarTabela();
            atualizarTabelaFornecedores();
            info("Sucesso", "Medicamento cadastrado.");
        } catch (IllegalArgumentException e) {
            erro("Validação", e.getMessage());
        } catch (Exception e) {
            erro("Erro", e.getMessage());
        }
    }

    @FXML
    private void onExcluir() {
        try {
            validarCodigo7(txtCodigo.getText());
            boolean removed = cache.removeIf(x -> x.getCodigo().equals(txtCodigo.getText()));
            if (removed) {
                salvarCSV();
                salvarFornecedoresCSV();
                atualizarTabela();
                atualizarTabelaFornecedores();
                info("Sucesso", "Medicamento excluído.");
            } else {
                info("Aviso", "Código não encontrado.");
            }
        } catch (IllegalArgumentException e) {
            erro("Validação", e.getMessage());
        } catch (Exception e) {
            erro("Erro", e.getMessage());
        }
    }

    @FXML
    private void onBuscar() {
        try {
            validarCodigo7(txtCodigo.getText());
            Optional<Medicamento> med = buscarPorCodigo(txtCodigo.getText());
            if (med.isEmpty()) { info("Aviso", "Medicamento não encontrado."); return; }
            preencherCampos(med.get());
        } catch (IllegalArgumentException e) {
            erro("Validação", e.getMessage());
        } catch (Exception e) {
            erro("Erro", e.getMessage());
        }
    }

    @FXML
    private void onListar() {
        atualizarTabela();
        atualizarTabelaFornecedores();
    }

    // ================= Cache helpers =================
    private Optional<Medicamento> buscarPorCodigo(String codigo) {
        return cache.stream().filter(m -> m.getCodigo().equals(codigo)).findFirst();
    }
    private List<Fornecedor> fornecedoresUnicos() {
        LinkedHashMap<String, Fornecedor> map = new LinkedHashMap<>();
        for (Medicamento m : cache) {
            Fornecedor f = m.getFornecedor();
            if (f != null && f.getCnpj()!=null && !f.getCnpj().isBlank()) {
                map.putIfAbsent(f.getCnpj(), f);
            }
        }
        return new ArrayList<>(map.values());
    }

    // ================= Montagem dos objetos =================
    private Fornecedor montarFornecedorDosCampos() {
        return new Fornecedor(
                txtCnpj.getText(), txtRazao.getText(), txtTelefone.getText(), txtEmail.getText(),
                txtCidade.getText(), txtEstado.getText()
        );
    }
    private Medicamento montarMedicamentoDosCampos(Fornecedor f) {
        return new Medicamento(
                txtCodigo.getText(), txtNome.getText(), txtDescricao.getText(), txtPrincipio.getText(),
                dateValidade.getValue(),
                parseInt(txtQuantidade.getText(), "Quantidade inválida."),
                parsePreco(txtPreco.getText(), "Preço inválido."),
                chkControlado.isSelected(), f
        );
    }

    // ================= Validações =================
    private void validarMedicamento(Medicamento m) {
        validarCodigo7(m.getCodigo());
        validarNaoVazioMin(m.getNome(), 2, "Nome inválido (mín. 2).");
        validarValidadeFutura(m.getDataValidade());
        validarQuantidadeNaoNegativa(m.getQuantidadeEstoque());
        validarPrecoPositivo(m.getPreco());
        if (m.getFornecedor()==null) throw new IllegalArgumentException("Fornecedor é obrigatório.");
    }
    private void validarFornecedor(Fornecedor f) {
        validarCnpj(f.getCnpj());
        validarNaoVazioMin(f.getRazaoSocial(), 2, "Razão social inválida (mín. 2).");
    }

    private void validarCodigo7(String codigo) {
        if (codigo==null || !codigo.matches("^[A-Za-z0-9]{7}$"))
            throw new IllegalArgumentException("Código inválido (7 caracteres alfanuméricos).");
    }
    private void validarNaoVazioMin(String s, int min, String msg) {
        if (s==null || s.trim().length()<min) throw new IllegalArgumentException(msg);
    }
    private void validarValidadeFutura(LocalDate data) {
        if (data==null || data.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Data de validade no passado.");
    }
    private void validarQuantidadeNaoNegativa(int qtd) {
        if (qtd<0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
    }
    private void validarPrecoPositivo(BigDecimal preco) {
        if (preco==null || preco.signum()<=0)
            throw new IllegalArgumentException("Preço deve ser positivo.");
    }
    private void validarCnpj(String cnpj) {
        if (cnpj==null) throw new IllegalArgumentException("CNPJ inválido.");
        String n = cnpj.replaceAll("\\D","");
        if (!n.matches("\\d{14}")) throw new IllegalArgumentException("CNPJ precisa ter 14 dígitos.");
        if (n.chars().distinct().count()==1) throw new IllegalArgumentException("CNPJ inválido.");
        int dv1 = dv(n.substring(0,12), new int[]{5,4,3,2,9,8,7,6,5,4,3,2});
        int dv2 = dv(n.substring(0,12)+dv1, new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2});
        if (n.charAt(12)-'0'!=dv1 || n.charAt(13)-'0'!=dv2)
            throw new IllegalArgumentException("CNPJ com dígitos verificadores inválidos.");
    }
    private int dv(String base, int[] pesos){
        int s=0; for(int i=0;i<pesos.length;i++) s+=(base.charAt(i)-'0')*pesos[i];
        int r=s%11; return (r<2)?0:11-r;
    }

    private int parseInt(String s, String msgErro) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(msgErro); }
    }
    private BigDecimal parsePreco(String s, String msgErro) {
        try { return new BigDecimal(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException(msgErro); }
    }

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

    // ================= CSV =================
    private void carregarCSV() {
        try {
            Files.createDirectories(arquivoCSV.getParent());
            if (Files.notExists(arquivoCSV)) {
                Files.writeString(arquivoCSV, HEADER_MED + System.lineSeparator());
            }
            cache.clear();
            List<String> linhas = Files.readAllLines(arquivoCSV, StandardCharsets.UTF_8);
            for (int i=1;i<linhas.size();i++) {
                String linha = linhas.get(i);
                if (linha.isBlank()) continue;
                String[] t = linha.split(";", -1);
                Fornecedor f = new Fornecedor(t[8], t[9], t[10], t[11], t[12], t[13]);
                Medicamento m = new Medicamento(
                        t[0], t[1], t[2], t[3],
                        LocalDate.parse(t[4]),
                        Integer.parseInt(t[5]),
                        new BigDecimal(t[6]),
                        Boolean.parseBoolean(t[7]),
                        f
                );
                cache.add(m);
            }
        } catch (IOException e) {
            erro("Erro ao carregar CSV", e.getMessage());
        }
    }

    private void salvarCSV() {
        try (BufferedWriter bw = Files.newBufferedWriter(
                arquivoCSV, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
        ) {
            bw.write(HEADER_MED); bw.newLine();
            for (Medicamento m : cache) {
                var f = m.getFornecedor();
                bw.write(String.join(";",
                        m.getCodigo(), m.getNome(), m.getDescricao(), m.getPrincipioAtivo(),
                        m.getDataValidade().toString(),
                        Integer.toString(m.getQuantidadeEstoque()),
                        m.getPreco().toPlainString(),
                        Boolean.toString(m.isControlado()),
                        nz(f.getCnpj()), nz(f.getRazaoSocial()), nz(f.getTelefone()), nz(f.getEmail()),
                        nz(f.getCidade()), nz(f.getEstado())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            erro("Erro ao salvar medicamentos.csv", e.getMessage());
        }
    }

    private void salvarFornecedoresCSV() {
        try (BufferedWriter bw = Files.newBufferedWriter(
                arquivoFornecedoresCSV, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
        ) {
            bw.write(HEADER_FORN); bw.newLine();
            for (Fornecedor f : fornecedoresUnicos()) {
                bw.write(String.join(";", nz(f.getCnpj()), nz(f.getRazaoSocial()), nz(f.getTelefone()),
                        nz(f.getEmail()), nz(f.getCidade()), nz(f.getEstado())));
                bw.newLine();
            }
        } catch (IOException e) {
            erro("Erro ao salvar fornecedores.csv", e.getMessage());
        }
    }

    private String nz(String s){ return (s==null) ? "" : s; }
}
