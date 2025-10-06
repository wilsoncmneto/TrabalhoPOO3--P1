package org.provapoo3.model;

public class fornecedor {
    private String cnpj;
    private String razaoSocial;
    private String telefone;
    private String email;
    private String cidade;
    private String estado;

    public fornecedor(String cnpj, String razaoSocial, String telefone, String email, String cidade, String estado) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.telefone = telefone;
        this.email = email;
        this.cidade = cidade;
        this.estado = estado;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "fornecedor{" +
                "cnpj='" + cnpj + '\'' +
                ", razaoSocial='" + razaoSocial + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
