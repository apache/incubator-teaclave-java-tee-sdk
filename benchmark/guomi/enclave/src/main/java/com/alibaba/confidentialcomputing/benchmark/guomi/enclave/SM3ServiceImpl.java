package com.alibaba.confidentialcomputing.benchmark.guomi.enclave;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;

class SM3ServiceImpl {
    static byte[] sm3Service(String plainText) throws Exception {
        byte[] messages = plainText.getBytes();
        Digest md = new SM3Digest();
        md.update(messages, 0, messages.length);
        byte[] digest = new byte[md.getDigestSize()];
        md.doFinal(digest, 0);
        return digest;
    }
}
