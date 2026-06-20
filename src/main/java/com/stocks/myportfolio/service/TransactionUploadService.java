package com.stocks.myportfolio.service;

import org.springframework.web.multipart.MultipartFile;

import com.stocks.myportfolio.dto.response.TransactionUploadResponse;

public interface TransactionUploadService {

    TransactionUploadResponse uploadPdf(MultipartFile file);
}
