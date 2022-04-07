/*
 * Copyright (c) 2009, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include <jni.h>
#include "jni_util.h"
#include "ecc_impl.h"
#include "com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods.h"

#define ILLEGAL_STATE_EXCEPTION "java/lang/IllegalStateException"
#define INVALID_ALGORITHM_PARAMETER_EXCEPTION \
        "java/security/InvalidAlgorithmParameterException"
#define INVALID_PARAMETER_EXCEPTION \
        "java/security/InvalidParameterException"
#define KEY_EXCEPTION   "java/security/KeyException"

/*
 * Declare library specific JNI_Onload entry if static build
 */
JNIEXPORT jint JNICALL JNI_OnLoad_sunec(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_8;
}

/*
 * Throws an arbitrary Java exception.
 */
void ThrowException_C(JNIEnv *env, const char *exceptionName)
{
    jclass exceptionClazz = (*env)->FindClass(env, exceptionName);
    if (exceptionClazz != NULL) {
        (*env)->ThrowNew(env, exceptionClazz, NULL);
    }
}

/*
 * Deep free of the ECParams struct
 */
void FreeECParams_C(ECParams *ecparams, jboolean freeStruct)
{
    // Use B_FALSE to free the SECItem->data element, but not the SECItem itself
    // Use B_TRUE to free both

    SECITEM_FreeItem(&ecparams->fieldID.u.prime, B_FALSE);
    SECITEM_FreeItem(&ecparams->curve.a, B_FALSE);
    SECITEM_FreeItem(&ecparams->curve.b, B_FALSE);
    SECITEM_FreeItem(&ecparams->curve.seed, B_FALSE);
    SECITEM_FreeItem(&ecparams->base, B_FALSE);
    SECITEM_FreeItem(&ecparams->order, B_FALSE);
    SECITEM_FreeItem(&ecparams->DEREncoding, B_FALSE);
    SECITEM_FreeItem(&ecparams->curveOID, B_FALSE);
    if (freeStruct)
        free(ecparams);
}

jbyteArray getEncodedBytes_C(JNIEnv *env, SECItem *hSECItem)
{
    SECItem *s = (SECItem *)hSECItem;

    jbyteArray jEncodedBytes = (*env)->NewByteArray(env, s->len);
    if (jEncodedBytes == NULL) {
        return NULL;
    }
    // Copy bytes from a native SECItem buffer to Java byte array
    (*env)->SetByteArrayRegion(env, jEncodedBytes, 0, s->len, (jbyte *)s->data);
    if ((*env)->ExceptionCheck(env)) { // should never happen
        return NULL;
    }
    return jEncodedBytes;
}

/*
 * Class:     sun_security_ec_ECKeyPairGenerator
 * Method:    isCurveSupported
 * Signature: ([B)Z
 */
JNIEXPORT jboolean
JNICALL Java_com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods_isCurveSupported
  (JNIEnv *env, jclass clazz, jbyteArray encodedParams)
{
    SECKEYECParams params_item;
    ECParams *ecparams = NULL;
    jboolean result = JNI_FALSE;

    // The curve is supported if we can get parameters for it
    params_item.len = (*env)->GetArrayLength(env, encodedParams);
    params_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, encodedParams, 0);
    if (params_item.data == NULL) {
        goto cleanup;
    }

    // Fill a new ECParams using the supplied OID
    if (EC_DecodeParams(&params_item, &ecparams, 0) != SECSuccess) {
        /* bad curve OID */
        goto cleanup;
    }

    // If we make it to here, then the curve is supported
    result = JNI_TRUE;

cleanup:
    {
        if (params_item.data) {
            (*env)->ReleaseByteArrayElements(env, encodedParams,
                (jbyte *) params_item.data, JNI_ABORT);
        }
        if (ecparams) {
            FreeECParams_C(ecparams, TRUE);
        }
    }

    return result;
}

/*
 * Class:     sun_security_ec_ECKeyPairGenerator
 * Method:    generateECKeyPair
 * Signature: (I[B[B)[[B
 */
JNIEXPORT jobjectArray
JNICALL Java_com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods_generateECKeyPair
  (JNIEnv *env, jclass clazz, jint keySize, jbyteArray encodedParams, jbyteArray seed)
{
    ECPrivateKey *privKey = NULL; // contains both public and private values
    ECParams *ecparams = NULL;
    SECKEYECParams params_item;
    jint jSeedLength;
    jbyte* pSeedBuffer = NULL;
    jobjectArray result = NULL;
    jclass baCls = NULL;
    jbyteArray jba;

    // Initialize the ECParams struct
    params_item.len = (*env)->GetArrayLength(env, encodedParams);
    params_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, encodedParams, 0);
    if (params_item.data == NULL) {
        goto cleanup;
    }

    // Fill a new ECParams using the supplied OID
    if (EC_DecodeParams(&params_item, &ecparams, 0) != SECSuccess) {
        /* bad curve OID */
        ThrowException_C(env, INVALID_ALGORITHM_PARAMETER_EXCEPTION);
        goto cleanup;
    }

    // Copy seed from Java to native buffer
    jSeedLength = (*env)->GetArrayLength(env, seed);
    pSeedBuffer = (jbyte*)malloc(jSeedLength * sizeof(jbyte));

    (*env)->GetByteArrayRegion(env, seed, 0, jSeedLength, pSeedBuffer);

    // Generate the new keypair (using the supplied seed)
    if (EC_NewKey(ecparams, &privKey, (unsigned char *) pSeedBuffer,
        jSeedLength, 0) != SECSuccess) {
        ThrowException_C(env, KEY_EXCEPTION);
        goto cleanup;
    }

    jboolean isCopy;
    baCls = (*env)->FindClass(env, "[B");
    if (baCls == NULL) {
        goto cleanup;
    }
    result = (*env)->NewObjectArray(env, 2, baCls, NULL);
    if (result == NULL) {
        goto cleanup;
    }
    jba = getEncodedBytes_C(env, &(privKey->privateValue));
    if (jba == NULL) {
        result = NULL;
        goto cleanup;
    }
    (*env)->SetObjectArrayElement(env, result, 0, jba); // big integer
    if ((*env)->ExceptionCheck(env)) { // should never happen
        result = NULL;
        goto cleanup;
    }

    jba = getEncodedBytes_C(env, &(privKey->publicValue));
    if (jba == NULL) {
        result = NULL;
        goto cleanup;
    }
    (*env)->SetObjectArrayElement(env, result, 1, jba); // encoded ec point
    if ((*env)->ExceptionCheck(env)) { // should never happen
        result = NULL;
        goto cleanup;
    }

cleanup:
    {
        if (params_item.data) {
            (*env)->ReleaseByteArrayElements(env, encodedParams,
                (jbyte *) params_item.data, JNI_ABORT);
        }
        if (ecparams) {
            FreeECParams_C(ecparams, TRUE);
        }
        if (privKey) {
            FreeECParams_C(&privKey->ecParams, FALSE);
            SECITEM_FreeItem(&privKey->version, B_FALSE);
            SECITEM_FreeItem(&privKey->privateValue, B_FALSE);
            SECITEM_FreeItem(&privKey->publicValue, B_FALSE);
            free(privKey);
        }

        if (pSeedBuffer) {
            free(pSeedBuffer);
        }
    }

    return result;
}

/*
 * Class:     sun_security_ec_ECDSASignature
 * Method:    signDigest
 * Signature: ([B[B[B[B)[B
 */
JNIEXPORT jbyteArray
JNICALL Java_com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods_signDigest
  (JNIEnv *env, jclass clazz, jbyteArray digest, jbyteArray privateKey, jbyteArray encodedParams, jbyteArray seed, jint timing)
{
    jbyte* pDigestBuffer = NULL;
    jint jDigestLength = (*env)->GetArrayLength(env, digest);
    jbyteArray jSignedDigest = NULL;

    SECItem signature_item;
    jbyte* pSignedDigestBuffer = NULL;
    jbyteArray temp;

    jint jSeedLength = (*env)->GetArrayLength(env, seed);
    jbyte* pSeedBuffer = NULL;

    // Copy digest from Java to native buffer
    pDigestBuffer = (jbyte *)malloc(jDigestLength*sizeof(jbyte));
    (*env)->GetByteArrayRegion(env, digest, 0, jDigestLength, pDigestBuffer);
    SECItem digest_item;
    digest_item.data = (unsigned char *) pDigestBuffer;
    digest_item.len = jDigestLength;

    ECPrivateKey privKey;
    privKey.privateValue.data = NULL;

    // Initialize the ECParams struct
    ECParams *ecparams = NULL;
    SECKEYECParams params_item;
    params_item.len = (*env)->GetArrayLength(env, encodedParams);
    params_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, encodedParams, 0);
    if (params_item.data == NULL) {
        goto cleanup;
    }

    // Fill a new ECParams using the supplied OID
    if (EC_DecodeParams(&params_item, &ecparams, 0) != SECSuccess) {
        /* bad curve OID */
        ThrowException_C(env, INVALID_ALGORITHM_PARAMETER_EXCEPTION);
        goto cleanup;
    }

    // Extract private key data
    privKey.ecParams = *ecparams; // struct assignment
    privKey.privateValue.len = (*env)->GetArrayLength(env, privateKey);
    privKey.privateValue.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, privateKey, 0);
    if (privKey.privateValue.data == NULL) {
        goto cleanup;
    }

    // Prepare a buffer for the signature (twice the key length)
    pSignedDigestBuffer = (jbyte *)malloc(ecparams->order.len * 2 * sizeof(jbyte));
    signature_item.data = (unsigned char *) pSignedDigestBuffer;
    signature_item.len = ecparams->order.len * 2;

    // Copy seed from Java to native buffer
    pSeedBuffer = (jbyte *)malloc(jSeedLength*sizeof(jbyte));
    (*env)->GetByteArrayRegion(env, seed, 0, jSeedLength, pSeedBuffer);

    // Sign the digest (using the supplied seed)
    if (ECDSA_SignDigest(&privKey, &signature_item, &digest_item,
        (unsigned char *) pSeedBuffer, jSeedLength, 0, timing) != SECSuccess) {
        ThrowException_C(env, KEY_EXCEPTION);
        goto cleanup;
    }

    // Create new byte array
    temp = (*env)->NewByteArray(env, signature_item.len);
    if (temp == NULL) {
        goto cleanup;
    }

    // Copy data from native buffer
    (*env)->SetByteArrayRegion(env, temp, 0, signature_item.len, pSignedDigestBuffer);
    jSignedDigest = temp;

cleanup:
    {
        if (params_item.data) {
            (*env)->ReleaseByteArrayElements(env, encodedParams,
                (jbyte *) params_item.data, JNI_ABORT);
        }
        if (privKey.privateValue.data) {
            (*env)->ReleaseByteArrayElements(env, privateKey,
                (jbyte *) privKey.privateValue.data, JNI_ABORT);
        }
        if (pDigestBuffer) {
            free(pDigestBuffer);
        }
        if (pSignedDigestBuffer) {
            free(pSignedDigestBuffer);
        }
        if (pSeedBuffer) {
            free(pSeedBuffer);
        }
        if (ecparams) {
            FreeECParams_C(ecparams, TRUE);
        }
    }

    return jSignedDigest;
}

/*
 * Class:     sun_security_ec_ECDSASignature
 * Method:    verifySignedDigest
 * Signature: ([B[B[B[B)Z
 */
JNIEXPORT jboolean
JNICALL Java_com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods_verifySignedDigest
  (JNIEnv *env, jclass clazz, jbyteArray signedDigest, jbyteArray digest, jbyteArray publicKey, jbyteArray encodedParams)
{
    jboolean isValid = FALSE;

    // Copy signedDigest from Java to native buffer
    jbyte* pSignedDigestBuffer = NULL;
    jint jSignedDigestLength = (*env)->GetArrayLength(env, signedDigest);
    pSignedDigestBuffer = (jbyte *)malloc(jSignedDigestLength*sizeof(jbyte));
    (*env)->GetByteArrayRegion(env, signedDigest, 0, jSignedDigestLength,
        pSignedDigestBuffer);
    SECItem signature_item;
    signature_item.data = (unsigned char *) pSignedDigestBuffer;
    signature_item.len = jSignedDigestLength;

    // Copy digest from Java to native buffer
    jbyte* pDigestBuffer = NULL;
    jint jDigestLength = (*env)->GetArrayLength(env, digest);
    pDigestBuffer = (jbyte *)malloc(jDigestLength*sizeof(jbyte));
    (*env)->GetByteArrayRegion(env, digest, 0, jDigestLength, pDigestBuffer);
    SECItem digest_item;
    digest_item.data = (unsigned char *) pDigestBuffer;
    digest_item.len = jDigestLength;

    // Extract public key data
    ECPublicKey pubKey;
    pubKey.publicValue.data = NULL;
    ECParams *ecparams = NULL;
    SECKEYECParams params_item;

    // Initialize the ECParams struct
    params_item.len = (*env)->GetArrayLength(env, encodedParams);
    params_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, encodedParams, 0);
    if (params_item.data == NULL) {
        goto cleanup;
    }

    // Fill a new ECParams using the supplied OID
    if (EC_DecodeParams(&params_item, &ecparams, 0) != SECSuccess) {
        /* bad curve OID */
        ThrowException_C(env, INVALID_ALGORITHM_PARAMETER_EXCEPTION);
        goto cleanup;
    }
    pubKey.ecParams = *ecparams; // struct assignment
    pubKey.publicValue.len = (*env)->GetArrayLength(env, publicKey);
    pubKey.publicValue.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, publicKey, 0);

    if (ECDSA_VerifyDigest(&pubKey, &signature_item, &digest_item, 0)
            != SECSuccess) {
        goto cleanup;
    }

    isValid = TRUE;

cleanup:
    {
        if (params_item.data)
            (*env)->ReleaseByteArrayElements(env, encodedParams,
                (jbyte *) params_item.data, JNI_ABORT);

        if (pubKey.publicValue.data)
            (*env)->ReleaseByteArrayElements(env, publicKey,
                (jbyte *) pubKey.publicValue.data, JNI_ABORT);

        if (ecparams)
            FreeECParams_C(ecparams, TRUE);

        if (pSignedDigestBuffer)
            free(pSignedDigestBuffer);

        if (pDigestBuffer)
            free(pDigestBuffer);
    }

    return isValid;
}

/*
 * Class:     sun_security_ec_ECDHKeyAgreement
 * Method:    deriveKey
 * Signature: ([B[B[B)[B
 */
JNIEXPORT jbyteArray
JNICALL Java_com_alibaba_confidentialcomputing_enclave_substitutes_NativeSunECMethods_deriveKey
  (JNIEnv *env, jclass clazz, jbyteArray privateKey, jbyteArray publicKey, jbyteArray encodedParams)
{
    jbyteArray jSecret = NULL;
    ECParams *ecparams = NULL;
    SECItem privateValue_item;
    privateValue_item.data = NULL;
    SECItem publicValue_item;
    publicValue_item.data = NULL;
    SECKEYECParams params_item;
    params_item.data = NULL;

    // Extract private key value
    privateValue_item.len = (*env)->GetArrayLength(env, privateKey);
    privateValue_item.data =
            (unsigned char *) (*env)->GetByteArrayElements(env, privateKey, 0);
    if (privateValue_item.data == NULL) {
        goto cleanup;
    }

    // Extract public key value
    publicValue_item.len = (*env)->GetArrayLength(env, publicKey);
    publicValue_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, publicKey, 0);
    if (publicValue_item.data == NULL) {
        goto cleanup;
    }

    // Initialize the ECParams struct
    params_item.len = (*env)->GetArrayLength(env, encodedParams);
    params_item.data =
        (unsigned char *) (*env)->GetByteArrayElements(env, encodedParams, 0);
    if (params_item.data == NULL) {
        goto cleanup;
    }

    // Fill a new ECParams using the supplied OID
    if (EC_DecodeParams(&params_item, &ecparams, 0) != SECSuccess) {
        /* bad curve OID */
        ThrowException_C(env, INVALID_ALGORITHM_PARAMETER_EXCEPTION);
        goto cleanup;
    }

    // Prepare a buffer for the secret
    SECItem secret_item;
    secret_item.data = NULL;
    secret_item.len = ecparams->order.len * 2;

    if (ECDH_Derive(&publicValue_item, ecparams, &privateValue_item, B_FALSE,
        &secret_item, 0) != SECSuccess) {
        ThrowException_C(env, ILLEGAL_STATE_EXCEPTION);
        goto cleanup;
    }

    // Create new byte array
    jSecret = (*env)->NewByteArray(env, secret_item.len);
    if (jSecret == NULL) {
        goto cleanup;
    }

    // Copy bytes from the SECItem buffer to a Java byte array
    (*env)->SetByteArrayRegion(env, jSecret, 0, secret_item.len,
        (jbyte *)secret_item.data);

    // Free the SECItem data buffer
    SECITEM_FreeItem(&secret_item, B_FALSE);

cleanup:
    {
        if (privateValue_item.data)
            (*env)->ReleaseByteArrayElements(env, privateKey,
                (jbyte *) privateValue_item.data, JNI_ABORT);

        if (publicValue_item.data)
            (*env)->ReleaseByteArrayElements(env, publicKey,
                (jbyte *) publicValue_item.data, JNI_ABORT);

        if (params_item.data)
            (*env)->ReleaseByteArrayElements(env, encodedParams,
                (jbyte *) params_item.data, JNI_ABORT);

        if (ecparams)
            FreeECParams_C(ecparams, TRUE);
    }

    return jSecret;
}
