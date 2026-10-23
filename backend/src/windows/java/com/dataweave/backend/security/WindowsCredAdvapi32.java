package com.dataweave.backend.security;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/** Advapi32 credential APIs (not exposed on the stock JNA Advapi32 interface). */
interface WindowsCredAdvapi32 extends StdCallLibrary {

    WindowsCredAdvapi32 INSTANCE =
            Native.load("Advapi32", WindowsCredAdvapi32.class, W32APIOptions.UNICODE_OPTIONS);

    boolean CredWriteW(WindowsCredentialVault.CREDENTIAL credential, int flags);

    boolean CredReadW(String targetName, int type, int reservedFlag, PointerByReference credential);

    boolean CredDeleteW(String targetName, int type, int flags);

    void CredFree(Pointer buffer);
}
