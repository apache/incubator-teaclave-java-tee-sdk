package com.alibaba.confidentialcomputing.benchmark.guomi.enclave;

import com.alibaba.confidentialcomputing.benchmark.guomi.common.SMService;
import com.google.auto.service.AutoService;

@AutoService(SMService.class)
public class SMServiceImpl implements SMService {

    @Override
    public String sm2Service(String plaintext) throws Exception {
        return new SM2ServiceImpl().sm2Service(plaintext);
    }

    @Override
    public byte[] sm3Service(String plainText) throws Exception {
        return SM3ServiceImpl.sm3Service(plainText);
    }

    @Override
    public String sm4Service(String plaintext) throws Exception {
        return new SM4ServiceImpl().sm4Service(plaintext);
    }
}
