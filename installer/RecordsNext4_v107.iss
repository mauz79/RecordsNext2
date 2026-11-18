; RecordsNext by mauz79 4.0.0 — Inno Setup 7
; Script sorgente pubblico. Il builder RN4_Crea-Installer-v107.py lo copia
; sotto build/ e aggiunge la sezione [Files] per visualizzatori, se presenti.
; Da compilare SOLO con il payload preparato dal builder (nessun dato utente).
[Setup]
AppId=it.alterlega.recordsnext.mauz79.4
AppName=RecordsNext by mauz79
AppVersion=4.0.0
AppVerName=RecordsNext by mauz79 4.0
AppPublisher=mauz79
DefaultDirName={localappdata}\Programs\RecordsNext4
DefaultGroupName=RecordsNext by mauz79
DisableDirPage=no
UsePreviousAppDir=yes
PrivilegesRequired=lowest
WizardStyle=modern
Compression=lzma2
SolidCompression=yes
OutputDir=output
OutputBaseFilename=RecordsNext_4.0.0_SETUP
VersionInfoVersion=4.0.0.0
UninstallDisplayName=RecordsNext by mauz79 4.0
CloseApplications=yes
RestartApplications=no

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
; Il builder aggiunge Italian.isl quando presente nel compilatore installato.

[Tasks]
Name: "desktopicon"; Description: "Crea un collegamento sul desktop"; Flags: unchecked

[Dirs]
Name: "{app}\config"
Name: "{app}\data\database"

[Files]
; SOLO programma/statici/runtime. NON distribuire mai config, data, DB, identita' o report.
Source: "payload\RecordsNext.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\RecordsNext.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\Avvia-RecordsNext4.ps1"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\INSTALLAZIONE_RECORDSNEXT4.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "payload\runtime\ucanaccess\*"; DestDir: "{app}\runtime\ucanaccess"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "payload\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs createallsubdirs
;VISUALIZZATORI_FILES

[Icons]
Name: "{group}\RecordsNext 4.0"; Filename: "{app}\RecordsNext.bat"; WorkingDir: "{app}"
Name: "{autodesktop}\RecordsNext 4.0"; Filename: "{app}\RecordsNext.bat"; WorkingDir: "{app}"; Tasks: desktopicon

[Code]
function NextButtonClick(CurPageID: Integer): Boolean;
var
  InstallDir: String;
begin
  Result := True;
  if CurPageID <> wpSelectDir then Exit;
  InstallDir := AddBackslash(WizardDirValue);
  { Non sovrascrivere la vecchia 3.2 con un setup 4.0: serve migrazione dati. }
  if FileExists(InstallDir + 'unins000.exe') and
     (not FileExists(InstallDir + 'INSTALLAZIONE_RECORDSNEXT4.md')) then
  begin
    MsgBox('Nella directory selezionata esiste una installazione precedente non riconosciuta come RecordsNext 4.0. ' +
           'Per aggiornare dalla 3.2 occorre prima migrare il database e le configurazioni. ' +
           'Scegliere una directory diversa o completare la migrazione guidata.', mbError, MB_OK);
    Result := False;
  end;
end;
