package com.dataweave.backend.security;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinCred;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class WindowsCredentialVault implements CredentialVault {

    private static final String PREFIX = "DataWeave/";

    @Override
    public void store(String key, char[] secret) {
        WinCred.CREDENTIAL credential = new WinCred.CREDENTIAL();
        credential.Type = WinCred.CRED_TYPE_GENERIC;
        credential.TargetName = PREFIX + key;
        byte[] blob = new String(secret).getBytes(StandardCharsets.UTF_16LE);
        credential.CredentialBlob = new Memory(blob.length);
        credential.CredentialBlob.write(0, blob, 0, blob.length);
        credential.CredentialBlobSize = blob.length;
        credential.Persist = WinCred.CRED_PERSIST_LOCAL_MACHINE;
        if (!Advapi32.INSTANCE.CredWriteW(credential, 0)) {
            throw new IllegalStateException("CredWrite failed");
        }
    }

    @Override
    public Optional<char[]> load(String key) {
        PointerByReference pref = new PointerByReference();
        if (!Advapi32.INSTANCE.CredReadW(PREFIX + key, WinCred.CRED_TYPE_GENERIC, 0, pref)) {
            return Optional.empty();
        }
        WinCred.CREDENTIAL cred = new WinCred.CREDENTIAL(pref.getValue());
        cred.read();
        byte[] blob = cred.CredentialBlob.getByteArray(0, cred.CredentialBlobSize);
        return Optional.of(new String(blob, StandardCharsets.UTF_16LE).toCharArray());
    }

    @Override
    public void delete(String key) {
        Advapi32.INSTANCE.CredDeleteW(PREFIX + key, WinCred.CRED_TYPE_GENERIC);
    }
}
