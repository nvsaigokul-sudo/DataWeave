package com.dataweave.backend.security;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@ConditionalOnProperty(name = "dataweave.test.mode", havingValue = "false", matchIfMissing = true)
public class WindowsCredentialVault implements CredentialVault {

    private static final String PREFIX = "DataWeave/";
    private static final int CRED_TYPE_GENERIC = 1;
    private static final int CRED_PERSIST_SESSION = 1;

    /** Same-process fallback when Credential Manager read fails (e.g. restricted CI runners). */
    private final Map<String, char[]> sessionSecrets = new ConcurrentHashMap<>();

    @Override
    public void store(String key, char[] secret) {
        sessionSecrets.put(key, secret.clone());
        byte[] blob = (new String(secret) + '\0').getBytes(StandardCharsets.UTF_16LE);
        Memory blobMemory = new Memory(blob.length);
        blobMemory.write(0, blob, 0, blob.length);
        CREDENTIAL credential = new CREDENTIAL();
        credential.Type = CRED_TYPE_GENERIC;
        credential.TargetName = PREFIX + key;
        credential.CredentialBlob = blobMemory;
        credential.CredentialBlobSize = blob.length;
        credential.Persist = CRED_PERSIST_SESSION;
        credential.UserName = "dataweave";
        credential.write();
        if (!WindowsCredAdvapi32.INSTANCE.CredWriteW(credential, 0)) {
            throw new IllegalStateException("CredWrite failed with code " + Kernel32.INSTANCE.GetLastError());
        }
    }

    @Override
    public Optional<char[]> load(String key) {
        Optional<char[]> fromManager = loadFromCredentialManager(key);
        if (fromManager.isPresent()) {
            return fromManager;
        }
        char[] cached = sessionSecrets.get(key);
        return cached == null ? Optional.empty() : Optional.of(cached.clone());
    }

    private Optional<char[]> loadFromCredentialManager(String key) {
        PointerByReference pref = new PointerByReference();
        if (!WindowsCredAdvapi32.INSTANCE.CredReadW(PREFIX + key, CRED_TYPE_GENERIC, 0, pref)) {
            return Optional.empty();
        }
        CREDENTIAL cred = new CREDENTIAL(pref.getValue());
        cred.read();
        try {
            byte[] blob = cred.CredentialBlob.getByteArray(0, cred.CredentialBlobSize);
            String decoded = new String(blob, StandardCharsets.UTF_16LE);
            int end = decoded.indexOf('\0');
            if (end >= 0) {
                decoded = decoded.substring(0, end);
            }
            return Optional.of(decoded.toCharArray());
        } finally {
            WindowsCredAdvapi32.INSTANCE.CredFree(pref.getValue());
        }
    }

    @Override
    public void delete(String key) {
        sessionSecrets.remove(key);
        WindowsCredAdvapi32.INSTANCE.CredDeleteW(PREFIX + key, CRED_TYPE_GENERIC, 0);
    }

    @Structure(alignment = 8)
    public static class CREDENTIAL extends Structure {
        public int Flags;
        public int Type;
        public String TargetName;
        public String Comment;
        public WinBase.FILETIME LastWritten;
        public int CredentialBlobSize;
        public Pointer CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public Pointer Attributes;
        public String TargetAlias;
        public String UserName;

        public CREDENTIAL() {}

        public CREDENTIAL(Pointer memory) {
            super(memory);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "Flags",
                    "Type",
                    "TargetName",
                    "Comment",
                    "LastWritten",
                    "CredentialBlobSize",
                    "CredentialBlob",
                    "Persist",
                    "AttributeCount",
                    "Attributes",
                    "TargetAlias",
                    "UserName");
        }
    }
}
