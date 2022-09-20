package org.apache.teaclave.javasdk.test.enclave;

import com.google.auto.service.AutoService;
import org.apache.teaclave.javasdk.test.common.SMSignAndVerify;
import org.bouncycastle.jcajce.spec.SM2ParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;

@AutoService(SMSignAndVerify.class)
public class SMSignAndVerifyImpl implements SMSignAndVerify {

    private final Provider PROVIDER;

    public SMSignAndVerifyImpl() {
        Provider provider = Security.getProvider("BC");
        if (provider != null) {
            PROVIDER = provider;
        } else {
            PROVIDER = new BouncyCastleProvider();
        }
        Security.addProvider(PROVIDER);
    }

    @Override
    public Boolean smSignAndVerify(String plaintext) throws Exception {
        Security.addProvider(PROVIDER);
        ECParameterSpec sm2p256v1 = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", PROVIDER);
        generator.initialize(sm2p256v1);
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Signature signature = Signature.getInstance("SM3WithSM2", PROVIDER);
        signature.initSign(privateKey);
        signature.setParameter(new SM2ParameterSpec("1234567812345678".getBytes()));
        signature.update(plaintext.getBytes());
        byte[] sign = signature.sign();

        signature.initVerify(publicKey);
        signature.update(plaintext.getBytes());
        return signature.verify(sign);
    }
}
