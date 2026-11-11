package it.alterlega.recordsnext.app.topflop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * External LCS player-name evidence used ONLY to explain approval proposals.
 * Neither these CSVs nor this class can modify the identity registry, SQLite or
 * FCA/FCM source rows. A decision is always made explicitly by the operator.
 */
public final class TopFlopIdentityEvidence {
    private record Identity(String name, String birth) {}
    private final Map<Identity, Identity> aliases = new LinkedHashMap<>();
    private final Set<Identity> checked = new HashSet<>();
    private final Set<Identity> conflictingAliases = new HashSet<>();
    /** Separate historical evidence: never used to approve or silently override current LCS. */
    private final Map<Identity, Map<String, Identity>> historicalAliases = new LinkedHashMap<>();
    private final Set<Identity> historicalConflicts = new HashSet<>();
    private int historicalRows;
    private int aliasRows;
    private int checkedRows;

    public record Assessment(String category, String explanation, String lcsEvidence,
                             boolean requiresIndividualReview) {}

    public static TopFlopIdentityEvidence load(Path directory) throws IOException {
        TopFlopIdentityEvidence evidence = new TopFlopIdentityEvidence();
        Path aliasFile = directory.resolve("LCS_GiocatoriAccoppiamenti.csv");
        Path checkFile = directory.resolve("LCS_ControlloGiocatori.csv");
        if (Files.exists(aliasFile)) {
            for (List<String> row : parse(aliasFile)) {
                if (row.size() < 4 || row.get(0).equalsIgnoreCase("Nome Giocatore")) continue;
                Identity original = identity(row.get(0),row.get(1));
                Identity replacement = identity(row.get(2),row.get(3));
                if (original.name().isBlank() || replacement.name().isBlank()) continue;
                Identity previous = evidence.aliases.putIfAbsent(original,replacement);
                if (previous != null && !previous.equals(replacement)) evidence.conflictingAliases.add(original);
                evidence.aliasRows++;
            }
        }
        // Keep the three older versions separate. The 2020 file may be byte-identical
        // to the current export: it is still useful as a dated provenance record.
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2015_old.csv"), "2015 old");
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2019.csv"), "2019");
        evidence.loadHistorical(directory.resolve("LCS_GiocatoriAccoppiamenti_2020.csv"), "2020");
        evidence.reconcileHistoricalDifferences();
        if (Files.exists(checkFile)) {
            for (List<String> row : parse(checkFile)) {
                if (row.size() < 2 || row.get(0).equalsIgnoreCase("NomeGiocatore")) continue;
                Identity person = identity(row.get(0),row.get(1));
                if (!person.name().isBlank()) { evidence.checked.add(person); evidence.checkedRows++; }
            }
        }
        return evidence;
    }
    public int aliasRows() { return aliasRows; }
    public int checkedRows() { return checkedRows; }
    private void loadHistorical(Path file, String version) throws IOException {
        if (!Files.isRegularFile(file)) return;
        for (List<String> row : parse(file)) {
            if (row.size() < 4 || row.get(0).equalsIgnoreCase("Nome Giocatore")) continue;
            Identity original=identity(row.get(0),row.get(1));
            Identity replacement=identity(row.get(2),row.get(3));
            if(original.name().isBlank()||replacement.name().isBlank())continue;
            Map<String,Identity> versions=historicalAliases.computeIfAbsent(original,k->new LinkedHashMap<>());
            Identity previous=versions.putIfAbsent(version,replacement);
            if(previous!=null && !previous.equals(replacement)) historicalConflicts.add(original);
            historicalRows++;
        }
    }
    private void reconcileHistoricalDifferences() {
        for (var entry : historicalAliases.entrySet()) {
            Set<Identity> targets=new HashSet<>(entry.getValue().values());
            Identity latest=aliases.get(entry.getKey());
            if(latest!=null) targets.add(latest);
            if(targets.size()>1) historicalConflicts.add(entry.getKey());
        }
    }
    public int historicalRows() { return historicalRows; }
    public int historicalConflicts() { return historicalConflicts.size(); }
    public int historicalOnly() {
        int count=0;
        for(Identity key:historicalAliases.keySet()) if(!aliases.containsKey(key))count++;
        return count;
    }

    /** Assessment of one duplicated Gazzetta code; never an authorization to merge. */
    public Assessment classify(TopFlopPlayerIdentities.Collision collision) {
        List<TopFlopPlayerIdentities.Source> sources = collision.members();
        Set<String> names = new HashSet<>();
        Set<String> dates = new HashSet<>();
        Set<Identity> resolved = new HashSet<>();
        boolean anyAlias = false;
        boolean aliasConflict = false;
        boolean allKnownDates = true;
        boolean controlMatch = false;
        for (var source : sources) {
            Identity orig = identity(source.name(),source.birth());
            names.add(orig.name());
            if (orig.birth().isBlank() || orig.birth().startsWith("1900-")) allKnownDates = false;
            else dates.add(orig.birth());
            var canonical = canonical(orig);
            resolved.add(canonical);
            if (!canonical.equals(orig)) anyAlias = true;
            if (conflictingAliases.contains(orig)) aliasConflict = true;
            if (checked.contains(canonical)) controlMatch = true;
        }
        boolean historicalConflict=false;
        boolean historicalMatch=false;
        for(var source : sources) {
            Identity orig=identity(source.name(),source.birth());
            if(historicalConflicts.contains(orig)) historicalConflict=true;
            if(historicalAliases.containsKey(orig)) historicalMatch=true;
        }
        String lcs = aliasConflict ? "LCS: correzioni discordanti, verificare" :
            anyAlias ? ("LCS Accoppiamenti: alias documentati" +
                       (controlMatch ? "; riscontro ControlloGiocatori" : "")) :
            (controlMatch ? "LCS ControlloGiocatori: anagrafica presente" : "Nessuna correzione LCS pertinente");
        if (historicalConflict) return new Assessment("FONTI LCS STORICHE DISCORDANTI",
            "Le versioni 2015/2019/2020 riportano correzioni diverse per almeno una anagrafica: esaminare individualmente.",
            "LCS: correzioni discordanti tra versioni; leggere il dettaglio prima di decidere.",true);
        if (aliasConflict) return new Assessment("LCS IN CONFLITTO",
            "La stessa anagrafica ha correzioni LCS discordanti. Non fondere in blocco.",lcs,true);
        if(historicalMatch && !anyAlias) lcs="Riscontri LCS anche nelle versioni 2015/2019/2020; nessuna fusione automatica";
        if (anyAlias && resolved.size() == 1 && noSeasonDuplicates(sources)) {
            return new Assessment("VARIANTE DOCUMENTATA LCS",
                "Le anagrafiche convergono nella stessa forma corretta nel CSV LCS. Richiede comunque approvazione.",lcs,false);
        }
        boolean similar = similarNames(names);
        boolean sameBirth = dates.size() == 1 && allKnownDates;
        if (sameBirth && similar) return new Assessment("VARIANTE ORTOGRAFICA",
            "I nomi sono simili e la data di nascita e' identica. Proposta da approvare.",lcs,false);
        if (sameBirth) return new Assessment("NOMI DIVERSI, STESSA DATA",
            "La data coincide, ma i nomi differiscono troppo: controllare che sia la stessa persona.",lcs,true);
        if (dates.size() == 2 && swappedDayMonth(dates) && similar)
            return new Assessment("DATA GIORNO/MESE INVERTITI",
                "Nomi simili; le date sembrano avere giorno e mese scambiati.",lcs,false);
        if (dates.size() > 1 && similar) return new Assessment("NASCITA DISCORDANTE",
            "I nomi suggeriscono la stessa persona, ma le date di nascita differiscono.",lcs,true);
        if (dates.size() > 1 && !similar) return new Assessment("POSSIBILI PERSONE DIVERSE",
            "Nomi e date differenti: probabile riutilizzo del codice Gazzetta. Esaminare individualmente.",lcs,true);
        if (similar) return new Assessment("VARIANTE DA VERIFICARE",
            "Nomi simili; nascita incompleta o non utilizzabile come conferma.",lcs,true);
        return new Assessment("CODICE DA VERIFICARE",
            "Identita' non dimostrabile dai nomi e dalle date disponibili.",lcs,true);
    }
    public String evidenceDetails(TopFlopPlayerIdentities.Collision collision) {
        StringBuilder b=new StringBuilder();
        Set<Identity> seen=new HashSet<>();
        for(var source:collision.members()) {
            Identity orig=identity(source.name(),source.birth());
            if(!seen.add(orig))continue;
            Identity latest=aliases.get(orig);
            if(latest!=null) {
                b.append("LCS corrente: ").append(orig.name()).append(" [").append(orig.birth())
                 .append("] -> ").append(latest.name()).append(" [").append(latest.birth()).append("]\n");
            }
            Map<String,Identity> historical=historicalAliases.get(orig);
            if(historical!=null) {
                for(var entry:historical.entrySet()) {
                    b.append("LCS ").append(entry.getKey()).append(": ")
                     .append(orig.name()).append(" [").append(orig.birth()).append("] -> ")
                     .append(entry.getValue().name()).append(" [").append(entry.getValue().birth()).append("]\n");
                }
            }
            if(historicalConflicts.contains(orig))
                b.append("ATTENZIONE: DESTINAZIONI LCS STORICHE DISCORDANTI; approvazione individuale.\n");
        }
        return b.length()==0 ? "Nessun accoppiamento LCS specifico per queste anagrafiche." : b.toString();
    }
    private Identity canonical(Identity original) {
        Identity current = original;
        Set<Identity> seen = new HashSet<>();
        while (seen.add(current) && !conflictingAliases.contains(current)) {
            Identity next = aliases.get(current);
            if (next == null || next.equals(current)) break;
            current = next;
        }
        return current;
    }
    private static boolean noSeasonDuplicates(List<TopFlopPlayerIdentities.Source> group) {
        return group.stream().map(TopFlopPlayerIdentities.Source::season).distinct().count()==group.size();
    }
    private static boolean swappedDayMonth(Set<String> dates) {
        var v = List.copyOf(dates);
        if (v.size()!=2 || v.get(0).length()!=10 || v.get(1).length()!=10) return false;
        String a=v.get(0), b=v.get(1);
        return a.substring(0,4).equals(b.substring(0,4)) &&
            a.substring(5,7).equals(b.substring(8,10)) && a.substring(8,10).equals(b.substring(5,7));
    }
    private static boolean similarNames(Set<String> names) {
        List<String> n = new ArrayList<>(names);
        if(n.size()<2) return true;
        for(int i=1;i<n.size();i++) {
            boolean near=false;
            for(int j=0;j<i;j++) if(similar(n.get(i),n.get(j))) {near=true;break;}
            if(!near) return false;
        }
        return true;
    }
    private static boolean similar(String a,String b) {
        if(a.equals(b))return true;
        if(a.isEmpty()||b.isEmpty())return false;
        int len = Math.max(a.length(),b.length());
        if (levenshtein(a,b) <= Math.max(1,(int)Math.floor(len*0.16)))return true;
        Set<String> as=new HashSet<>(List.of(a.split(" ")));
        Set<String> bs=new HashSet<>(List.of(b.split(" ")));
        int shared=0;
        for(String token:as)if(bs.contains(token))shared++;
        return shared>=2 && shared*1.0/Math.max(as.size(),bs.size())>=0.5;
    }
    private static int levenshtein(String a,String b) {
        if(Math.abs(a.length()-b.length())>Math.max(3,(int)(Math.max(a.length(),b.length())*0.25)))return 999;
        int[] prev=new int[b.length()+1],curr=new int[b.length()+1];
        for(int j=0;j<=b.length();j++)prev[j]=j;
        for(int i=1;i<=a.length();i++){
            curr[0]=i;
            for(int j=1;j<=b.length();j++)curr[j]=Math.min(Math.min(curr[j-1]+1,prev[j]+1),prev[j-1]+(a.charAt(i-1)==b.charAt(j-1)?0:1));
            int[] t=prev;prev=curr;curr=t;
        }
        return prev[b.length()];
    }
    private static Identity identity(String name,String birth) {
        return new Identity(normalize(name),normalizeDate(birth));
    }
    private static String normalize(String name) {
        if(name==null)return "";
        return Normalizer.normalize(name,Normalizer.Form.NFD).replaceAll("\\p{M}","")
            .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+"," ").trim().replaceAll(" +"," ");
    }
    private static String normalizeDate(String raw) {
        if(raw==null)return "";
        String s=raw.trim();
        if(s.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*"))return s.substring(0,10);
        if(s.matches("[0-9]{2}[-/][0-9]{2}[-/][0-9]{4}"))return s.substring(6)+"-"+s.substring(3,5)+"-"+s.substring(0,2);
        return "";
    }
    private static List<List<String>> parse(Path file) throws IOException {
        byte[] bytes=Files.readAllBytes(file);
        String txt;
        try {
            var decoder=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT);
            txt=decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch(CharacterCodingException ex) {txt=new String(bytes,Charset.forName("windows-1252"));}
        if(txt.startsWith("\uFEFF"))txt=txt.substring(1);
        List<List<String>> rows=new ArrayList<>();
        List<String> cells=new ArrayList<>();
        StringBuilder item=new StringBuilder();
        boolean quoted=false;
        for(int i=0;i<txt.length();i++) {
            char ch=txt.charAt(i);
            if(ch=='"') {if(quoted && i+1<txt.length() && txt.charAt(i+1)=='"') {item.append('"');i++;} else quoted=!quoted;}
            else if(ch==';'&&!quoted){cells.add(item.toString().trim());item.setLength(0);}
            else if((ch=='\n'||ch=='\r')&&!quoted){
                if(ch=='\r'&&i+1<txt.length()&&txt.charAt(i+1)=='\n')i++;
                cells.add(item.toString().trim());item.setLength(0);
                if(cells.stream().anyMatch(v->!v.isBlank()))rows.add(List.copyOf(cells));
                cells=new ArrayList<>();
            } else item.append(ch);
        }
        cells.add(item.toString().trim());
        if(cells.stream().anyMatch(v->!v.isBlank()))rows.add(List.copyOf(cells));
        return rows;
    }
}
