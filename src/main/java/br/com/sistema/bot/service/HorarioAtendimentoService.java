package br.com.sistema.bot.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class HorarioAtendimentoService {

    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final LocalTime ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime FECHAMENTO_SUPORTE = LocalTime.of(21, 0);
    private static final LocalTime FECHAMENTO_FINANCEIRO = LocalTime.of(18, 0);

    // ====================================================
    // isSuporteTecnicoDisponivel - Domingo a domingo, 09h às 21h
    // ====================================================
    public boolean isSuporteTecnicoDisponivel() {
        LocalTime agora = ZonedDateTime.now(ZONA_BRASIL).toLocalTime();
        return agora.isAfter(ABERTURA) && agora.isBefore(FECHAMENTO_SUPORTE);
    }

    // ====================================================
    // isFinanceiroComercialDisponivel - Segunda a sábado, 09h às 18h
    // ====================================================
    public boolean isFinanceiroComercialDisponivel() {
        ZonedDateTime agora = ZonedDateTime.now(ZONA_BRASIL);
        DayOfWeek dia = agora.getDayOfWeek();
        LocalTime hora = agora.toLocalTime();
        boolean diaUtil = dia != DayOfWeek.SUNDAY;
        boolean horario = hora.isAfter(ABERTURA) && hora.isBefore(FECHAMENTO_FINANCEIRO);
        return diaUtil && horario;
    }
}
