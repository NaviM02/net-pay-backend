package com.navi.net_pay_backend.support;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Synthetic CSV data generated in memory, independent of local documentation and sample files. */
public final class ImportFixtures {
    private ImportFixtures() {}

    public static InputStream input(String fixture) throws IOException {
        StringWriter output = new StringWriter();
        try (CSVPrinter csv = new CSVPrinter(output, CSVFormat.DEFAULT)) {
            switch (fixture) {
                case "services.csv" -> services(csv);
                case "clients.csv" -> clients(csv);
                case "users.csv" -> users(csv);
                case "payments.csv" -> payments(csv);
                case "invalid-clients.csv" -> invalidClients(csv);
                case "invalid-payments.csv" -> invalidPayments(csv);
                default -> throw new IllegalArgumentException("Unknown import fixture: " + fixture);
            }
        }
        return new ByteArrayInputStream(output.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void services(CSVPrinter csv) throws IOException {
        csv.printRecord("name", "speed", "monthlyPrice", "type", "status");
        for (int i = 1; i <= 10; i++) {
            csv.printRecord("Test Plan " + i, (i * 10) + " Mbps", i == 9 ? "0.00" : "99.00",
                    i <= 5 ? "71" : "72", i == 10 ? "50" : "49");
        }
    }

    private static void clients(CSVPrinter csv) throws IOException {
        csv.printRecord("businessName", "contactName", "contactEmail", "contactPhone", "address", "latitude",
                "longitude", "customerSince", "serviceName", "collectorHashId", "status");
        for (int i = 1; i <= 60; i++) {
            csv.printRecord(clientName(i), "Contact " + i, i == 9 ? "null" : clientEmail(i),
                    i == 9 ? null : clientPhone(i), i == 9 ? null : "Test address, house " + i,
                    i == 1 ? "14.8340000" : null, i == 1 ? "-91.5180000" : null,
                    i == 9 ? null : LocalDate.now().minusMonths(2).withDayOfMonth(1),
                    "Test Plan " + ((i - 1) % 9 + 1), null, i % 10 == 0 ? "INACTIVE" : "ACTIVE");
        }
    }

    private static void users(CSVPrinter csv) throws IOException {
        csv.printRecord("fullName", "email", "password", "status");
        for (int i = 1; i <= 12; i++) {
            csv.printRecord(i == 12 ? "Test Administrator" : "Test Collector " + i, "collector" + i + "@example.com",
                    null, i == 10 ? "INACTIVE" : i == 11 ? "SUSPENDED" : "ACTIVE");
        }
    }

    private static void payments(CSVPrinter csv) throws IOException {
        csv.printRecord("importKey", "clientName", "clientEmail", "clientPhone", "collectorName", "collectorEmail",
                "amount", "paidAt", "method", "status", "reference", "comment");
        for (int i = 1; i <= 20; i++) {
            int collector = (i - 1) % 8 + 1;
            boolean unassigned = i == 9 || i == 18;
            boolean byName = i % 4 == 0;
            csv.printRecord("test-payment-" + i, i == 3 || i == 6 ? null : clientName(i),
                    i == 3 || i == 5 ? clientEmail(i) : null, i == 6 ? clientPhone(i) : null,
                    !unassigned && byName ? "Test Collector " + collector : null,
                    !unassigned && !byName ? "collector" + collector + "@example.com" : null,
                    i == 9 || i == 18 ? "5.00" : "50.00", LocalDate.now(),
                    i % 3 == 0 ? "81" : i % 3 == 1 ? "TRANSFER" : "CARD",
                    i == 10 ? "PENDING" : i == 11 ? "FAILED" : i == 14 ? null : "COMPLETED",
                    i % 2 == 0 ? "test-reference-" + i : null, i == 5 ? "Quoted, comment" : null);
        }
    }

    private static void invalidClients(CSVPrinter csv) throws IOException {
        csv.printRecord("businessName", "contactName", "contactEmail", "serviceName", "latitude", "customerSince");
        csv.printRecord("Invalid 1", null, null, "Test Plan 1", null, null);
        csv.printRecord("Invalid 2", "Contact", "invalid-email", "Test Plan 1", null, null);
        csv.printRecord("Invalid 3", "Contact", null, "Plan inexistente", null, null);
        csv.printRecord("Invalid 4", "Contact", null, "Test Plan 1", "120", null);
        csv.printRecord("Invalid 5", "Contact", null, "Test Plan 1", null, "2025-02-30");
        csv.printRecord("Valid", "Contact", null, "Test Plan 1", null, "2025-01-01");
    }

    private static void invalidPayments(CSVPrinter csv) throws IOException {
        csv.printRecord("importKey", "clientName", "collectorEmail", "amount", "paidAt", "method", "status");
        csv.printRecord("invalid-1", "Missing Client", null, "50.00", LocalDate.now(), "CASH", "COMPLETED");
        csv.printRecord("invalid-2", clientName(1), null, "0", LocalDate.now(), "CASH", "COMPLETED");
        csv.printRecord("invalid-3", clientName(1), null, "50.00", "2025-02-30", "CASH", "COMPLETED");
        csv.printRecord("invalid-4", clientName(1), null, "50.00", LocalDate.now(), "UNKNOWN", "COMPLETED");
        csv.printRecord("invalid-5", clientName(1), "collector10@example.com", "50.00", LocalDate.now(), "CASH", "COMPLETED");
        csv.printRecord(null, clientName(1), null, "50.00", LocalDate.now(), "CASH", "COMPLETED");
        csv.printRecord("valid-payment", clientName(1), null, "50.00", LocalDate.now(), "CASH", "COMPLETED");
    }

    private static String clientName(int index) {
        return String.format("Import Test Client %03d", index);
    }

    private static String clientEmail(int index) {
        return String.format("client%03d@example.com", index);
    }

    private static String clientPhone(int index) {
        return String.format("555%05d", index);
    }
}
