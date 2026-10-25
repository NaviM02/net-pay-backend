package com.navi.net_pay_backend.application.model;

import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

public final class ImportValues {
    private ImportValues() {}

    public static String required(Map<String, String> row, String field, int max) {
        String value = text(row, field, max);
        if (value == null) throw new DomainException("import_" + field + "_required");
        return value;
    }

    public static String text(Map<String, String> row, String field, int max) {
        String value = row.get(field);
        if (value != null && value.length() > max) throw new DomainException("import_" + field + "_too_long");
        return value;
    }

    public static String email(Map<String, String> row, String field) {
        String value = text(row, field, 150);
        if (value != null && !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new DomainException("import_" + field + "_invalid");
        }
        return value;
    }

    public static LocalDate date(Map<String, String> row, String field) {
        try {
            return row.get(field) == null ? null : LocalDate.parse(row.get(field));
        } catch (DateTimeParseException ex) {
            throw new DomainException("import_" + field + "_invalid_date");
        }
    }

    public static Long typology(Map<String, String> row, String field, Long defaultValue, Long parent) {
        String raw = row.get(field);
        if (raw == null) return defaultValue;
        try {
            TypologyEnum value;
            try {
                value = TypologyEnum.findById(Long.valueOf(raw));
            } catch (NumberFormatException ex) {
                value = TypologyEnum.valueOf(raw.toUpperCase(Locale.ROOT));
            }
            if (value == null || !parent.equals(value.getParentId()) || value == TypologyEnum.DELETED) {
                throw new IllegalArgumentException();
            }
            return value.getId();
        } catch (IllegalArgumentException ex) {
            throw new DomainException("import_" + field + "_invalid");
        }
    }

    public static BigDecimal decimal(Map<String, String> row, String field, int scale, BigDecimal min, BigDecimal max) {
        if (row.get(field) == null) return null;
        try {
            BigDecimal value = new BigDecimal(row.get(field));
            if (value.scale() > scale || value.compareTo(min) < 0 || value.compareTo(max) > 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new DomainException("import_" + field + "_invalid_number");
        }
    }
}
