package com.navi.net_pay_backend.domain.service;

public interface PasswordHashService {

    String hash(String data);

    boolean compare(String data, String hashedData);
}