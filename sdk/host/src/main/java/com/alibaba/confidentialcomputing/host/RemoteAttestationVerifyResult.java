package com.alibaba.confidentialcomputing.host;

final class RemoteAttestationVerifyResult {
    private final int status;
    private final int versionCheck;
    private final int verifyFlag;

    RemoteAttestationVerifyResult(int status, int versionCheck, int verifyFlag) {
        this.status = status;
        this.versionCheck = versionCheck;
        this.verifyFlag = verifyFlag;
    }

    int getStatus() {
        return this.status;
    }

    int getVersionCheck() {
        return this.versionCheck;
    }

    int getVerifyFlag() {
        return this.verifyFlag;
    }
}
