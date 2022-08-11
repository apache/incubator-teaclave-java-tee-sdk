package com.alibaba.confidentialcomputing.benchmark.guomi.enclave;

import com.alibaba.confidentialcomputing.benchmark.guomi.common.SMService;
import com.google.auto.service.AutoService;

@AutoService(SMService.class)
public class SMServiceImpl implements SMService {

    @Override
    public String sm2Service(String plaintext, int weight) throws Exception {
        String result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM2ServiceImpl().sm2Service(plaintext);
        }
        return result;
    }

    @Override
    public byte[] sm3Service(String plainText, int weight) throws Exception {
        byte[] result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM3ServiceImpl().sm3Service(plainText);
        }
        return result;
    }

    @Override
    public String sm4Service(String plaintext, int weight) throws Exception {
        String result = null;
        for (int i = 0x0; i < weight; i++) {
            result = new SM4ServiceImpl().sm4Service(plaintext);
        }
        return result;
    }
}
