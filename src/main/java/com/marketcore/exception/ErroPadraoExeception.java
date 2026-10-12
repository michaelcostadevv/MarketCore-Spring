package com.marketcore.exception;

import java.time.LocalDateTime;

public class ErroPadraoExeception {
    private Integer status;
    private String mensagem;
    private LocalDateTime dataHora;

    public ErroPadraoExeception() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
