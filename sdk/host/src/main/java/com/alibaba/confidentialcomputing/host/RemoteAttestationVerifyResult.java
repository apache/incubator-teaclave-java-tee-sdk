package com.alibaba.confidentialcomputing.host;

class RemoteAttestationVerifyResult {
    private volatile int status;
    private volatile int versionCheck;
    private volatile int verifyFlag;

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
