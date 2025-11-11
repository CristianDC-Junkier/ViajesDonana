Unicode true
!include "MUI2.nsh"
!include "FileFunc.nsh"

!define PRODUCT_NAME "Viajes Doñana - Almonte"
!define PRODUCT_PUBLISHER "Ayuntamiento de Almonte"
!define PRODUCT_VERSION "1.4"
!define PRODUCT_GUID "bef8bda6-81cb-47f3-8413-1c278744a73a"
!define PRODUCT_UNINST_KEY "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\${PRODUCT_GUID}"

Name "${PRODUCT_NAME}"
OutFile "dist\Viajes Doñana - Instalador.exe"
InstallDir "$PROGRAMFILES\Viajes Doñana - Almonte"
Icon "launcher\Almonte.ico"
UninstallIcon "launcher\Almonte.ico"
RequestExecutionLevel admin

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_LANGUAGE "Spanish"

Var AppDataPath
Var UninstallExe
Var ExistingInstall
Var IsUpdate

Section "Instalar"

    StrCpy $AppDataPath "$APPDATA\ViajesDoñana"
    IfFileExists "$AppDataPath" 0 +2
    Goto data_exists
data_exists:
    CreateDirectory "$AppDataPath"

    ReadRegStr $ExistingInstall HKLM "${PRODUCT_UNINST_KEY}" "UninstallString"
    StrCmp $ExistingInstall "" not_installed installed

installed:
    MessageBox MB_YESNO|MB_ICONQUESTION "Ya existe una versión instalada de ${PRODUCT_NAME}. ¿Desea actualizarla?" IDYES update IDNO cancel
    Goto update

cancel:
    Abort

update:
    RMDir /r "$INSTDIR"
    StrCpy $IsUpdate "1"
    Goto continue

not_installed:
    StrCpy $IsUpdate "0"

continue:

    SetOutPath "$INSTDIR"
    StrCpy $UninstallExe "$INSTDIR\desinstalar.exe"

    File "launcher\Almonte.ico"
    File /r "dist\Viajes Doñana - Almonte\*.*"

    CreateShortCut "$DESKTOP\${PRODUCT_NAME}.lnk" "$INSTDIR\${PRODUCT_NAME}.exe" "" "$INSTDIR\Almonte.ico"
    CreateDirectory "$SMPROGRAMS\Ayuntamiento"
    CreateShortCut "$SMPROGRAMS\Ayuntamiento\${PRODUCT_NAME}.lnk" "$INSTDIR\${PRODUCT_NAME}.exe" "" "$INSTDIR\Almonte.ico"

    WriteUninstaller "$UninstallExe"

    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "DisplayName" "${PRODUCT_NAME}"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "UninstallString" "$UninstallExe"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\${PRODUCT_NAME}.exe"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "Comments" "2025 Ayuntamiento de Almonte - Informática Alcaldía. Todos los derechos reservados."
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "InstallLocation" "$INSTDIR"
    WriteRegStr HKLM "${PRODUCT_UNINST_KEY}" "AppId" "${PRODUCT_GUID}"

    ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
    WriteRegDWORD HKLM "${PRODUCT_UNINST_KEY}" "EstimatedSize" $0

    StrCmp $IsUpdate "1" updated installed_new
updated:
    MessageBox MB_OK "La actualización se ha completado correctamente."
    Goto done
installed_new:
    MessageBox MB_OK "La instalación se ha completado correctamente."
done:

SectionEnd

Section "Uninstall"

    Delete "$DESKTOP\${PRODUCT_NAME}.lnk"
    Delete "$SMPROGRAMS\Ayuntamiento\${PRODUCT_NAME}.lnk"
    RMDir "$SMPROGRAMS\Ayuntamiento"
    RMDir /r "$INSTDIR"
    DeleteRegKey HKLM "${PRODUCT_UNINST_KEY}"

SectionEnd


