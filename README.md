# Gestão de Medicamentos (Java + JavaFX)

Aplicação **JavaFX** para gerenciar o estoque de medicamentos de uma farmácia.
Inclui **CRUD**, **persistência em CSV**, e **relatórios com Stream API** (filter, map, groupingBy, reducing, sorted).

> Projeto acadêmico — focado em clareza e requisitos funcionais/técnicos.

---

## ✅ Requisitos do Ambiente

* **JDK 17+** (recomendado: 17)
* **JavaFX 17+**
* IDE recomendada: **IntelliJ IDEA** (Community já serve)
* (Opcional) **Maven** ou **Gradle** para facilitar a execução

> O projeto usa `module-info.java`. Se rodar sem Maven/Gradle, será necessário configurar **module-path** do JavaFX manualmente.

---

## 📦 Estrutura do Projeto

```
projeto/
├── dados/
│   ├── medicamentos.csv          # base de dados principal (gerado/atualizado pelo app)
│   └── fornecedores.csv          # gerado a partir dos medicamentos (deduplicado por CNPJ)
├── src/
│   └── main/
│       ├── java/
│       │   └── org/provapoo3/
│       │       ├── Main.java
│       │       ├── controller/
│       │       │   └── MainController.java
│       │       ├── model/
│       │       │   ├── Medicamento.java
│       │       │   └── Fornecedor.java
│       │       └── module-info.java
│       └── resources/
│           └── org/provapoo3/view/
│               └── main-view.fxml
└── README.md
```

---

## ▶️ Como Executar

### Opção A) IntelliJ (sem Maven)

1. **Instale o JavaFX SDK** (baixe e descompacte o SDK da sua plataforma).

2. Abra o projeto no **IntelliJ**.

3. Vá em **Run → Edit Configurations…** e crie uma configuração para a classe principal:

   * **Main class:** `org.provapoo3.Main`

4. Em **VM options**, **adicione** (ajuste o caminho do seu JavaFX):

   **Windows:**

   ```
   --module-path "C:\caminho\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml
   ```

   **macOS/Linux:**

   ```
   --module-path /Library/Java/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
   ```

5. Rode a aplicação (Shift+F10).

> **Dica:** Se aparecer erro de módulos/exports, confira o `module-info.java` abaixo.

### Opção B) Maven (se houver `pom.xml`)

Se você mantiver um `pom.xml` com JavaFX:

```bash
mvn clean javafx:run
# ou
mvn clean compile exec:java -Dexec.mainClass="org.provapoo3.Main"
```

### Opção C) Gradle (alternativa)

Com plugin JavaFX configurado no `build.gradle`, algo como:

```bash
gradle clean run
```

---

## 🧭 Ponto de Entrada

* **Classe principal:** `org.provapoo3.Main`
* Carrega o FXML: `/org/provapoo3/view/main-view.fxml`

**module-info.java** (exemplo funcional):

```java
module org.provapoo3 {
    requires javafx.controls;
    requires javafx.fxml;

    exports org.provapoo3;
    exports org.provapoo3.controller;
    exports org.provapoo3.model;

    opens org.provapoo3 to javafx.fxml;
    opens org.provapoo3.controller to javafx.fxml;
    opens org.provapoo3.model to javafx.base;
}
```

---

## 🗃️ Persistência (CSV)

* **`dados/medicamentos.csv`** (criado automaticamente com cabeçalho):

  ```
  codigo;nome;descricao;principioAtivo;dataValidade;quantidadeEstoque;preco;controlado;forn_cnpj;forn_razao;forn_telefone;forn_email;forn_cidade;forn_estado
  ```

  * separador: **ponto-e-vírgula (;)**
  * **datas** no formato ISO: `yyyy-MM-dd`
  * **preço** salvo com **2 casas decimais**
  * arquivo é **ordenado por Nome, depois Código** ao salvar (organização)

* **`dados/fornecedores.csv`**: gerado a partir dos medicamentos (um por **CNPJ**), com cabeçalho:

  ```
  cnpj;razaoSocial;telefone;email;cidade;estado
  ```

* O sistema **carrega** o CSV ao iniciar e **salva** após cada operação de **Salvar** ou **Excluir**.

---

## 🧩 Funcionalidades

### CRUD de Medicamentos

* **Cadastrar**: preencha os campos de Medicamento e Fornecedor → **Salvar**
* **Buscar por Código**:

  * Digite o código (7 alfanuméricos) e clique em **Buscar por Código**
  * O formulário é preenchido e **ambas as tabelas** (Medicamentos e Fornecedores) são **filtradas** para o resultado
* **Excluir por Código**:

  * Digite o código e clique em **Excluir por Código**
* **Listar Todos**: restaura a visão completa nas duas tabelas

### Relatórios (Stream API)

Botões:

* **Próximos 30 dias** → filtra os medicamentos cuja validade está entre hoje e +30 dias
* **Estoque baixo (< 5)** → filtra medicamentos com quantidade < 5
* **Valor total por fornecedor** → calcula `preço × quantidade` e soma por fornecedor; mostra um **diálogo** com os totais e **filtra as tabelas** para esses fornecedores
* **Controlados** / **Não controlados** → filtra por flag

> Todos os relatórios usam **Streams** (`filter`, `sorted`, `groupingBy`, `mapping`, `reducing`).

---

## 🔒 Validações (Requisitos Técnicos)

* **Código**: exatamente **7 alfanuméricos** (`^[A-Za-z0-9]{7}$`)
* **Nome**: não vazio, mínimo **2** caracteres
* **Data de Validade**: **não** pode ser no passado
* **Quantidade em Estoque**: **≥ 0**
* **Preço**: **> 0**
* **CNPJ**: 14 dígitos + **dígitos verificadores** (implementado cálculo do DV)

Erros de validação são exibidos em **Alert** com mensagens objetivas.

---

## 🖥️ Interface (Resumo)

* **Formulário**: Medicamento (esquerda) | Fornecedor (direita) + Ações (Salvar/Buscar/Excluir/Listar)
* **Relatórios (Stream API)**: bloco dedicado de botões
* **Tabela de Medicamentos**
* **Tabela de Fornecedores** (deduplicados por CNPJ)

> Ao **Buscar**, as tabelas mostram **apenas** o registro e seu fornecedor.
> Ao clicar em **Listar Todos**, tudo volta à listagem completa.

---

## 🧪 Como Testar Rápido

1. Rode o app.
2. Cadastre 2–3 medicamentos (varie validade, estoque, preço; use fornecedores diferentes e repetidos).
3. Clique em:

   * **Próximos 30 dias** → confirme a filtragem
   * **Estoque baixo (<5)** → confirme a filtragem
   * **Valor total por fornecedor** → verifique o diálogo com os totais
   * **Controlados / Não controlados** → confira a mudança na Tabela
4. Use **Buscar por Código** e depois **Listar Todos**.

---

## 🛠️ Solução de Problemas Comuns

* **`Unable to coerce 8 to class javafx.geometry.Insets`**
  Use `Insets` no FXML:

  ```xml
  <padding><Insets top="8" right="8" bottom="8" left="8"/></padding>
  ```

  (não use `padding="8"` direto)

* **FXML não injeta @FXML**
  Confirme que o FXML tem:

  ```xml
  fx:controller="org.provapoo3.controller.MainController"
  ```

  e que os `fx:id` batem com os campos do controller.

* **`module ... does not open ... to javafx.base`**
  No `module-info.java`:

  ```java
  opens org.provapoo3.model to javafx.base;
  opens org.provapoo3.controller to javafx.fxml;
  ```

* **JavaFX não encontrado**
  Configure corretamente o **module-path** do JavaFX nas **VM options**.

* **CSV não cria**
  Verifique se a pasta `dados/` existe. O app tenta criar; se não tiver permissão, crie manualmente.

---

## 🧱 Decisões de Design (resumo)

* Tudo centralizado no `MainController` (validações, persistência e relatórios) por **simplicidade didática**.
* **CSV “puro”** (separador `;`, cabeçalho fixo, datas ISO, preço com 2 casas).
* Fornecedores **deduplicados por CNPJ**.
* Relatórios aplicados sobre a lista em memória (**cache**), sem telas extras.

> Em um projeto maior, separaríamos em camadas (`Service`, `Repository`, `Validators`) e usaríamos uma lib CSV robusta.

---

## 📋 Checklist de Entrega (Professor)

* [x] **CRUD** completo (cadastrar, excluir, consultar por código, listar todos)
* [x] **Persistência** em CSV (`;`), carga ao iniciar e salvamento após operações
* [x] **Validações** (código, nome, validade, quantidade, preço, CNPJ + DV)
* [x] **Relatórios (Stream API)** — pelo menos 2 (implementados 5)
* [x] **Interface JavaFX** funcional
* [x] **README.md** com execução e detalhes técnicos

---

## 📄 Licença

Uso acadêmico / educacional.

---

## 🙋 Dúvidas / Suporte

Se surgir qualquer erro de execução, informe:

* SO (Windows/macOS/Linux)
* Versão do JDK e JavaFX
* Mensagem completa do erro
* Screenshot (se possível)

Assim fica mais fácil de ajudar!
