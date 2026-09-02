; RecordsNext 3.1.0 - installer pubblico
#define MyAppName "RecordsNext"
#define MyAppVersion "3.1.0"
#define MyAppPublisher "mauz79"
#define MyAppExeName "RecordsNext.bat"

[Setup]
AppId={{6B53BE8C-6F16-49C0-A6C8-2B9F2B867310}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={code:GetDefaultDir}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
DisableDirPage=no
OutputDir=D:\DEV_APPS\downloads
OutputBaseFilename=RecordsNext_3.1.0_SETUP
Compression=lzma2/ultra64
SolidCompression=yes
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=admin
WizardStyle=modern
SetupLogging=yes
Uninstallable=yes
CloseApplications=yes
RestartApplications=no
UsePreviousAppDir=no
AllowNoIcons=yes
CreateAppDir=yes

[Files]
Source: "D:\DEV_APPS\RecordsNext2.0\release\RecordsNext_3.1.0_FULL\payload\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Dirs]
Name: "{app}\data"
Name: "{app}\data\database"
Name: "{app}\data\calendars"
Name: "{app}\data\reports"
Name: "{app}\data\records-archive"
Name: "{app}\data\site-export-staging"
Name: "{app}\data\consolidation"

[Tasks]
Name: "desktopicon"; Description: "Crea un collegamento sul desktop"; GroupDescription: "Collegamenti:"; Flags: unchecked

[Icons]
Name: "{group}\RecordsNext"; Filename: "{app}\RecordsNext.bat"; WorkingDir: "{app}"
Name: "{autodesktop}\RecordsNext"; Filename: "{app}\RecordsNext.bat"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\RecordsNext.bat"; Description: "Avvia RecordsNext"; Flags: postinstall nowait skipifsilent

[Code]
function GetDefaultDir(Param: String): String;
begin
  { Percorso solo proposto: l'utente puo' cambiarlo liberamente nel wizard. }
  if DirExists('C:\FCM') then
    Result := 'C:\FCM\plugin\RecordsNext'
  else
    Result := ExpandConstant('{autopf}\FCM\plugin\RecordsNext');
end;

function InitializeSetup(): Boolean;
var
  ResultCode: Integer;
  TempFile: String;
  Lines: TArrayOfString;
  VersionLine: String;
  MajorVersion: Integer;
  P1, P2: Integer;
begin
  Result := False;

  TempFile := ExpandConstant('{tmp}\recordsnext_java_version.txt');

  if not Exec(
    ExpandConstant('{cmd}'),
    '/C java -version 2> "' + TempFile + '"',
    '',
    SW_HIDE,
    ewWaitUntilTerminated,
    ResultCode
  ) then
  begin
    MsgBox(
      'Java non trovato.' + #13#10 + #13#10 +
      'RecordsNext 3.1 richiede Java 21 o superiore.',
      mbError,
      MB_OK
    );
    exit;
  end;

  if ResultCode <> 0 then
  begin
    MsgBox(
      'Java non trovato o non avviabile.' + #13#10 + #13#10 +
      'RecordsNext 3.1 richiede Java 21 o superiore.',
      mbError,
      MB_OK
    );
    exit;
  end;

  if not LoadStringsFromFile(TempFile, Lines) or (GetArrayLength(Lines) = 0) then
  begin
    MsgBox(
      'Impossibile determinare la versione di Java.' + #13#10 + #13#10 +
      'RecordsNext 3.1 richiede Java 21 o superiore.',
      mbError,
      MB_OK
    );
    exit;
  end;

  VersionLine := Lines[0];

  { Accetta righe tipo: java version "21.0.1" ... oppure openjdk version "21..." }
  P1 := Pos('"', VersionLine);
  if P1 = 0 then
  begin
    MsgBox(
      'Versione Java non riconosciuta:' + #13#10 + VersionLine,
      mbError,
      MB_OK
    );
    exit;
  end;

  Delete(VersionLine, 1, P1);

  P2 := Pos('.', VersionLine);
  if P2 = 0 then
    P2 := Pos('"', VersionLine);

  if P2 = 0 then
  begin
    MsgBox(
      'Versione Java non riconosciuta.',
      mbError,
      MB_OK
    );
    exit;
  end;

  MajorVersion := StrToIntDef(Copy(VersionLine, 1, P2 - 1), 0);

  if MajorVersion < 21 then
  begin
    MsgBox(
      'Java ' + IntToStr(MajorVersion) + ' non compatibile.' + #13#10 + #13#10 +
      'RecordsNext 3.1 richiede Java 21 o superiore.',
      mbError,
      MB_OK
    );
    exit;
  end;

  Result := True;
end;
