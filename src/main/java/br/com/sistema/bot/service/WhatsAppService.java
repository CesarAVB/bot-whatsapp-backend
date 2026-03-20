package br.com.sistema.bot.service;

import br.com.sistema.bot.client.WhatsAppClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppClient whatsAppClient;
    private final MensagemHistoricoService mensagemHistoricoService;

    // ====================================================
    // enviarTexto - Envia mensagem de texto para o número informado
    // ====================================================
    public void enviarTexto(String telefone, String mensagem) {
        try {
            whatsAppClient.enviarTexto(telefone, mensagem);
            mensagemHistoricoService.registrarEnviada(telefone, mensagem);
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem de texto para {}", telefone, e);
        }
    }

    // ====================================================
    // enviarDocumento - Envia documento PDF (boleto) para o número informado
    // ====================================================
    public void enviarDocumento(String telefone, String linkPdf, String nomeArquivo, String caption) {
        try {
            whatsAppClient.enviarDocumento(telefone, linkPdf, nomeArquivo, caption);
            mensagemHistoricoService.registrarEnviada(telefone, "[Documento: " + nomeArquivo + "] " + caption);
        } catch (Exception e) {
            log.error("Erro ao enviar documento para {}. Tentando enviar linha digitável como texto.", telefone, e);
        }
    }
}
