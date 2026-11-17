package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Integrated site-scoped Top&Flop/Calciatori exporter.
 * All source data are read from the existing JDBC canonical archive. No Python runtime,
 * no SQLite writes, no identity inference, no standalone exporter or publish shortcut.
 * Profile scoring is reconciled against the ORIGINAL FCM scorecard MODIF on every
 * actually counted position. Unknown split is null, not zero.
 */
public final class TopFlopIntegratedV83 {
    private TopFlopIntegratedV83() {}
    public static final long LIMIT = 1_500_000L;
    private static final long TARGET = 850_000L;
    private static final String MF_FULL = "fcmRecordsNext_TopFlopFull_v83.js";
    private static final String MF_FMT = "fcmRecordsNext_TopFlopFcaFmt_v83.js";
    private static final String MF_PROFILE = "fcmRecordsNext_ProfileManifest_v83.js";
    private static final List<String> EVENTS = List.of("GOLFATTI1","ASSIST","GOLFATTISURIGORE1","RIGSBA",
        "AMM","ESP","AUTOGOL1","GOLSUBITI","GOLSUBITISURIGORE","RIGPAR");
    private static final BigDecimal TOL = new BigDecimal("0.011");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static String js(Object value) { return TopFlopCompactStageJsExporter.Json.write(value); }
    private static Map<String,Object> obj(Object... entries) {
        Map<String,Object> out=new LinkedHashMap<>();
        for(int i=0;i<entries.length;i+=2)out.put((String)entries[i],entries[i+1]);
        return out;
    }
    private static String sha(byte[] b) throws IOException {
        try {
            StringBuilder s=new StringBuilder();
            for(byte v:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format("%02x",v&255));
            return s.toString();
        }catch(Exception e){throw new IOException(e);}
    }
    private static void write(Path out,String file,String content) throws IOException {
        byte[] bytes=content.getBytes(StandardCharsets.UTF_8);
        if(bytes.length==0||bytes.length>LIMIT)throw new IOException("JS supera il limite: "+file+" "+bytes.length);
        Files.write(out.resolve(file),bytes,StandardOpenOption.CREATE_NEW);
    }
    private static List<Map<String,Object>> shards(Path out,String season,List<?> rows,String kind) throws IOException {
        String filePrefix="fcmRecordsNext_"+(kind.equals("Points")?"ProfilePoints":"PlayerIndex")+"_v83_"+season+"_";
        String variable=kind.equals("Points")?"RecordsNextV79Points":"RecordsNextV79Index";
        String head="window."+variable+"=window."+variable+"||{};\nwindow."+variable+"[\""+season+"\"]="+
            "window."+variable+"[\""+season+"\"]||"+
            (kind.equals("Points")?"{\"schemaVersion\":\"4.0-scorecard-points-v83\",\"stagione\":\""+season+"\",\"righe\":[]}":"[]")+";\n";
        String target="window."+variable+"[\""+season+"\"]"+(kind.equals("Points")?".righe":"");
        List<Map<String,Object>> meta=new ArrayList<>();
        List<String> batch=new ArrayList<>();long length=0;int part=1;
        for(Object row:rows){String value=js(row);long n=value.getBytes(StandardCharsets.UTF_8).length;
            if(n+head.length()+100>LIMIT)throw new IOException("Riga troppo grande per shard "+kind);
            if(!batch.isEmpty() && length+n>TARGET){meta.add(flush(out,season,kind,filePrefix,head,target,part++,batch));batch.clear();length=0;}
            batch.add(value);length+=n+1;
        }
        if(!batch.isEmpty()||meta.isEmpty())meta.add(flush(out,season,kind,filePrefix,head,target,part,batch));
        int count=meta.stream().mapToInt(x->((Number)x.get("righe")).intValue()).sum();
        if(count!=rows.size())throw new IOException("Copertura shard errata: "+kind+" "+season);
        return meta;
    }
    private static Map<String,Object> flush(Path out,String season,String kind,String pre,String head,
                                           String target,int part,List<String> batch) throws IOException {
        String name=pre+String.format("%03d",part)+".js";
        String s=head+target+".push(...["+String.join(",",batch)+"]);\n";
        write(out,name,s);byte[] bytes=s.getBytes(StandardCharsets.UTF_8);
        return obj("stagione",season,"part",part,"righe",batch.size(),"nome",name,"byte",bytes.length,"sha256",sha(bytes));
    }
    private static BigDecimal number(String value){
        if(value==null || !value.trim().matches("[+-]?[0-9]+(?:[,.][0-9]+)?"))return null;
        return new BigDecimal(value.trim().replace(',','.'));
    }
    private static BigDecimal ev(Map<String,BigDecimal> data,String field){return data.getOrDefault(field,ZERO);}
    private record Split(BigDecimal bonus,BigDecimal malus,String status){}
    private static Split split(Map<String,BigDecimal> e,BigDecimal observed,Set<String> absent){
        if(!absent.isEmpty())return new Split(null,null,"MISSING_FIELDS");
        if(ev(e,"GOLFATTISURIGORE1").compareTo(ev(e,"GOLFATTI1"))>0)return new Split(null,null,"INVALID_PENALTY_SUBSET");
        if(ev(e,"GOLSUBITISURIGORE").compareTo(ev(e,"GOLSUBITI"))>0)return new Split(null,null,"INVALID_CONCEDED_SUBSET");
        BigDecimal baseBonus=ev(e,"GOLFATTI1").subtract(ev(e,"GOLFATTISURIGORE1")).multiply(BigDecimal.valueOf(3))
            .add(ev(e,"ASSIST")).add(ev(e,"RIGPAR").multiply(BigDecimal.valueOf(3)));
        BigDecimal baseMalus=ev(e,"AMM").multiply(new BigDecimal("0.5")).add(ev(e,"ESP"))
            .add(ev(e,"AUTOGOL1").multiply(BigDecimal.valueOf(2))).add(ev(e,"GOLSUBITI"));
        Map<String,Split> candidates=new LinkedHashMap<>();
        for(int goal:(ev(e,"GOLFATTISURIGORE1").signum()==0?new int[]{3}:new int[]{2,3}))
            for(int missed:(ev(e,"RIGSBA").signum()==0?new int[]{3}:new int[]{2,3})){
                BigDecimal bonus=baseBonus.add(ev(e,"GOLFATTISURIGORE1").multiply(BigDecimal.valueOf(goal)));
                BigDecimal malus=baseMalus.add(ev(e,"RIGSBA").multiply(BigDecimal.valueOf(missed)));
                if(bonus.subtract(malus).subtract(observed).abs().compareTo(TOL)<=0)
                    candidates.put(bonus.stripTrailingZeros()+"/"+malus.stripTrailingZeros(),new Split(bonus,malus,"RECONCILED"));
            }
        if(candidates.size()==1)return candidates.values().iterator().next();
        return new Split(null,null,candidates.isEmpty()?"UNEXPLAINED_MODIF":"AMBIGUOUS_SPLIT");
    }
    private static final class Points {
        final String playerKey,competition;final int team,competitionId;
        int games,pairs,modifValid,verified,errors,nonzero;
        BigDecimal total=ZERO,votes=ZERO,delta=ZERO,bonus=ZERO,malus=ZERO;
        final Map<String,Long> events=new LinkedHashMap<>();
        final Map<String,Integer> unresolved=new LinkedHashMap<>();
        final Set<String> missing=new LinkedHashSet<>();
        Points(String p,int t,int c,String name,Set<String> absent){playerKey=p;team=t;competitionId=c;competition=name;missing.addAll(absent);}
        void add(FantasyFact fact,Set<String> absent) throws IOException {
            games++;total=total.add(fact.fantasyVote());
            Map<String,BigDecimal> e=fact.fantasyEvents();
            for(String name:EVENTS){BigDecimal v=ev(e,name);
                if(v.signum()<0 || v.stripTrailingZeros().scale()>0)throw new IOException("Evento FCA non intero "+playerKey+"/"+name);
                events.merge(name,v.longValueExact(),Long::sum);
            }
            var raw=fact.sourceFields();
            BigDecimal vote=number(raw.get("FCM.Tabellino.Voto"));
            BigDecimal modifier=number(raw.get("FCM.Tabellino.Modif"));
            if(vote==null){reason("VOTO_NON_NUMERICO");return;}
            pairs++;votes=votes.add(vote);
            BigDecimal difference=fact.fantasyVote().subtract(vote);delta=delta.add(difference);
            if(modifier==null || modifier.subtract(difference).abs().compareTo(TOL)>0){reason("MODIF_INCONSISTENT");return;}
            modifValid++;if(modifier.signum()!=0)nonzero++;
            Split r=split(e,modifier,absent);
            if(r.status.equals("RECONCILED")){verified++;bonus=bonus.add(r.bonus);malus=malus.add(r.malus);}
            else reason(r.status);
        }
        void reason(String s){errors++;unresolved.merge(s,1,Integer::sum);}
        Map<String,Object> asRow(){
            boolean complete=games>0&&games==pairs&&games==verified;
            Map<String,Object> mappedEvents=new LinkedHashMap<>();
            for(String name:EVENTS)mappedEvents.put(name,missing.contains(name)?null:events.getOrDefault(name,0L));
            return obj("playerKey",playerKey,"idSquadraInterno",team,"idCompetizioneInterno",competitionId,
              "competizione",competition,"gare",games,"totSum",total,
              "votoFcmSum",pairs==games?votes:null,"pairs",pairs,
              "deltaNetTabellino",pairs==games?delta:null,"bonusPunti",complete?bonus:null,
              "malusPunti",complete?malus:null,"saldoPunti",modifValid==games?delta:null,
              "certified",complete,"ruleSource",complete?List.of("FCM MODIF; coefficienti confermati e varianti rigore ricostruite"):List.of(),
              "ruleMismatches",errors,"modifNonzero",nonzero,"unresolvedReasons",unresolved,
              "pointsMethod","FCM_MODIF_NATIVE_JAVA_v83","events",mappedEvents,
              "missingSourceFields",new ArrayList<>(missing));
        }
    }
    private static Set<String> missingFcaFields(Connection c,String season) throws SQLException {
        String table="raw_"+season+"_fca_punteggio";
        Set<String> found=new HashSet<>();
        try(var s=c.createStatement();var r=s.executeQuery("PRAGMA table_info(\""+table+"\")")){
            while(r.next())found.add(r.getString("name").toUpperCase(Locale.ROOT));
        }
        if(found.isEmpty())throw new SQLException("Tabella FCA assente: "+table);
        Set<String> absent=new LinkedHashSet<>(EVENTS);absent.removeAll(found);return absent;
    }
    private static List<Map<String,Object>> points(String season,TopFlopArchive archive,Set<String> absent) throws IOException {
        Map<String,Points> grouped=new TreeMap<>();Set<String> seen=new HashSet<>();
        for(FantasyFact f:archive.allFantasyFacts()){
            if(f.usage()!=Usage.COUNTED)continue;
            if(f.fantasyVote()==null)throw new IOException("COUNTED senza TOT "+f.key());
            String unique=f.key().player().fcaPlayerId()+":"+f.key().fantasyTeamId()+":"
                +f.key().sourceCompetitionId()+":"+f.key().matchId();
            if(!seen.add(unique))throw new IOException("FCM COUNTED duplicato "+season+" "+unique);
            String player=season+":"+f.key().player().fcaPlayerId();
            int team=f.key().fantasyTeamId(),competition=f.key().sourceCompetitionId();
            String key=player+"/"+team+"/"+competition;
            Points p=grouped.computeIfAbsent(key,k->new Points(player,team,competition,f.competitionName(),absent));
            if(!p.competition.equals(f.competitionName()))throw new IOException("Competizione FCM incoerente: "+key);
            p.add(f,absent);
        }
        return grouped.values().stream().map(Points::asRow).toList();
    }
    private static Map<String,Object> fmtSeason(Connection c,String season,
            TopFlopJdbcArchiveAssembler.LoadedSeason loaded,TopFlopRealFantasyRules rules) throws SQLException {
        var result=TopFlopFcaOfficialFantasy.extract(c,season,loaded,rules.forSeason(season));
        Map<String,Object> players=new LinkedHashMap<>();
        for(Map<String,Object> r:result.rows()){
            String key=(String)r.get("playerKey"),id=key.substring(key.indexOf(':')+1);
            players.put(id,Arrays.asList(r.get("votiRealiValidi"),r.get("fantamediaReale"),r.get("stato")));
        }
        return players;
    }
    private static List<Object> index(String season,TopFlopJdbcArchiveAssembler.LoadedSeason loaded,
                                      Map<String,String> approved){
        List<Object> rows=new ArrayList<>();
        loaded.profiles().values().stream().sorted(Comparator.comparingInt(x->x.player().fcaPlayerId())).forEach(p->{
            String key=season+":"+p.player().fcaPlayerId();var fields=p.originalFields();
            String canon=approved.getOrDefault(key,key);
            rows.add(List.of(key,canon,p.name(),
                clean(fields.get("GiocatoreA.DATADINASCITA")),
                p.player().gazzaCode()==null?"":p.player().gazzaCode().toString(),
                clean(fields.get("GiocatoreA.RUOLO"))));
        });
        return rows;
    }
    private static String clean(String v){return v==null||v.equals("__SQL_NULL__")?"":v;}
    public static void stage(Connection c,Path reports,List<String> seasons,Path output,
                             Map<String,String> approved,TopFlopRealFantasyRules rules) throws Exception {
        if(seasons.isEmpty()||seasons.stream().distinct().count()!=seasons.size())throw new IOException("Stagioni invalide");
        List<Object> full=new ArrayList<>(),fmt=new ArrayList<>();
        List<Map<String,Object>> allPoints=new ArrayList<>(),allIndex=new ArrayList<>();
        for(String season:seasons){
            if(!season.matches("[0-9]{4}_[0-9]{4}"))throw new IOException("Stagione non valida "+season);
            var loaded=TopFlopJdbcArchiveAssembler.loadSeason(c,season);
            var links=reports==null?Map.<TopFlopCanonicalScorecards.MatchTeam,String>of():
                TopFlopCanonicalScorecards.load(reports,season);
            Map<Integer,Integer> roles=new LinkedHashMap<>();
            loaded.profiles().forEach((id,p)->{int role=-1;
                try{role=Integer.parseInt(p.originalFields().get("GiocatoreA.RUOLO"));}catch(RuntimeException ignored){}
                roles.put(id,role);
            });
            var real=TopFlopFullPerformancesV69.real(loaded.archive(),roles);
            var counted=TopFlopFullPerformancesV69.counted(loaded.archive(),links);
            var fileInfos=new ArrayList<TopFlopPublicViewsStageExporter.FileInfo>();
            fileInfos.addAll(TopFlopPublicViewsStageExporter.shard(output,season,"RealPerformances",real));
            fileInfos.addAll(TopFlopPublicViewsStageExporter.shard(output,season,"CountedPerformances",counted));
            full.add(obj("stagione",season,"reali",real.size(),"conteggiati",counted.size(),"files",
                fileInfos.stream().map(f->obj("nome",f.name(),"tipo",f.kind(),"righe",f.rows(),"byte",f.bytes(),"sha256",f.sha256())).toList()));
            Map<String,Object> official=fmtSeason(c,season,loaded,rules);
            String fmtFile="fcmRecordsNext_TopFlopFcaFmt_v83_"+season+".js";
            write(output,fmtFile,"window.fcmRecordsNextFcaFmtV70=window.fcmRecordsNextFcaFmtV70||{};\n"+
                "window.fcmRecordsNextFcaFmtV70[\""+season+"\"]="+js(official)+";\n");
            fmt.add(obj("stagione",season,"nome",fmtFile,"righe",official.size()));
            allPoints.addAll(shards(output,season,points(season,loaded.archive(),missingFcaFields(c,season)),"Points"));
            allIndex.addAll(shards(output,season,index(season,loaded,approved),"Index"));
        }
        write(output,MF_FULL,"window.fcmRecordsNextTopFlopFullV69 = "+js(obj(
            "schemaVersion","4.0-full-performances-v69","eventi",TopFlopFullPerformancesV69.EVENTS,
            "stagioni",full,"limiteByteJs",LIMIT))+";\n");
        write(output,MF_FMT,"window.fcmRecordsNextFcaFmtManifestV70 = "+js(obj(
            "schemaVersion","4.0-real-fmt-v70","stagioni",fmt))+";\n");
        write(output,MF_PROFILE,"window.RecordsNextV79Manifest = "+js(obj(
            "schemaVersion","4.0-profile-v79","seasons",seasons,"points",allPoints,"index",allIndex,
            "rulesStatus","FCM MODIF checked; bonus/malus only for unique reconstruction"))+";\n");
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> readManifest(Path source,String prefix) throws IOException {
        String s=Files.readString(source,StandardCharsets.UTF_8).trim();
        if(!s.startsWith(prefix)||!s.endsWith(";"))throw new IOException("Manifest JS invalido "+source);
        return (Map<String,Object>)MiniJson.parse(s.substring(prefix.length(),s.length()-1));
    }
    @SuppressWarnings("unchecked")
    public static int attach(Path prepared,List<String> scope,Path output) throws IOException {
        Map<String,Object> full=readManifest(prepared.resolve(MF_FULL),"window.fcmRecordsNextTopFlopFullV69 = ");
        Map<String,Object> fmt=readManifest(prepared.resolve(MF_FMT),"window.fcmRecordsNextFcaFmtManifestV70 = ");
        Map<String,Object> profile=readManifest(prepared.resolve(MF_PROFILE),"window.RecordsNextV79Manifest = ");
        Set<String> allowed=new HashSet<>((List<String>)profile.get("seasons"));
        if(!allowed.containsAll(scope)||scope.size()!=new HashSet<>(scope).size())throw new IOException("Cutoff profile non valido");
        List<Object> fullScoped=new ArrayList<>(),fmtScoped=new ArrayList<>();
        List<Map<String,Object>> points=new ArrayList<>(),index=new ArrayList<>();Set<String> names=new LinkedHashSet<>();
        for(Object o:(List<?>)full.get("stagioni")){
            Map<String,Object> row=(Map<String,Object>)o;
            if(!scope.contains(row.get("stagione")))continue;
            fullScoped.add(row);
            for(Object f:(List<?>)row.get("files"))names.add((String)((Map<?,?>)f).get("nome"));
        }
        for(Object o:(List<?>)fmt.get("stagioni")){
            Map<String,Object> row=(Map<String,Object>)o;
            if(scope.contains(row.get("stagione"))){fmtScoped.add(row);names.add((String)row.get("nome"));}
        }
        for(Object o:(List<?>)profile.get("points")){
            Map<String,Object> row=(Map<String,Object>)o;
            if(scope.contains(row.get("stagione"))){points.add(row);names.add((String)row.get("nome"));}
        }
        for(Object o:(List<?>)profile.get("index")){
            Map<String,Object> row=(Map<String,Object>)o;
            if(scope.contains(row.get("stagione"))){index.add(row);names.add((String)row.get("nome"));}
        }
        if(fullScoped.size()!=scope.size()||fmtScoped.size()!=scope.size())throw new IOException("Dati fonte incompleti nel cutoff");
        for(String n:names){
            if(n.contains("/")||n.contains("\\")||!n.endsWith(".js"))throw new IOException("Nome shard non valido: "+n);
            Path src=prepared.resolve(n),dest=output.resolve(n);
            if(!Files.isRegularFile(src)||Files.size(src)>LIMIT||Files.exists(dest))throw new IOException("Shard duplicato/assente/sovradimensionato "+src);
            Files.copy(src,dest);
        }
        full.put("stagioni",fullScoped);fmt.put("stagioni",fmtScoped);
        profile.put("seasons",scope);profile.put("points",points);profile.put("index",index);
        write(output,MF_FULL,"window.fcmRecordsNextTopFlopFullV69 = "+js(full)+";\n");
        write(output,MF_FMT,"window.fcmRecordsNextFcaFmtManifestV70 = "+js(fmt)+";\n");
        write(output,MF_PROFILE,"window.RecordsNextV79Manifest = "+js(profile)+";\n");
        return names.size()+3;
    }
}
