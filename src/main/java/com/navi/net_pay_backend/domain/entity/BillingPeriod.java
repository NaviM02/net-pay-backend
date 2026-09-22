package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class BillingPeriod {

    private static final String[] MONTHS = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private Long id;
    private Integer year;
    private Integer month;
    private LocalDate startsOn;
    private LocalDate endsOn;
    private Boolean closed;

    public static BillingPeriod of(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        return BillingPeriod.builder()
                .year(year)
                .month(month)
                .startsOn(start)
                .endsOn(start.plusMonths(1).minusDays(1))
                .closed(false)
                .build();
    }

    public BillingPeriod next() {
        LocalDate nextStart = startsOn.plusMonths(1);
        return of(nextStart.getYear(), nextStart.getMonthValue());
    }

    public String getLabel() {
        if (month == null || month < 1 || month > 12) {
            return year != null ? String.valueOf(year) : null;
        }
        return MONTHS[month - 1] + " " + year;
    }
}
