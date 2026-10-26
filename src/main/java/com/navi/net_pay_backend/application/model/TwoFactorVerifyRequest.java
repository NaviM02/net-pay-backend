package com.navi.net_pay_backend.application.model;

public record TwoFactorVerifyRequest(String challengeId, String code) {
}
