package com.alibaba.confidentialcomputing.test.enclave;

import com.alibaba.confidentialcomputing.test.common.SM3Service;
import com.google.auto.service.AutoService;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;

@AutoService(SM3Service.class)
public class SM3ServiceImpl implements SM3Service {
    @Override
    public byte[] sm3Service(String plainText) throws Exception {
        byte[] messages = plainText.getBytes();
        Digest md = new SM3Digest();
        md.update(messages, 0, messages.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        return digest;
    }
}
