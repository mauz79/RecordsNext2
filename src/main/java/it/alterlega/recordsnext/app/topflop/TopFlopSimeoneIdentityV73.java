package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.config.MiniJson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** v73: user-confirmed Simeone career, followed by cutoff-scoped history regeneration. */
public final class TopFlopSimeoneIdentityV73 {
    private static final String FACADE = "fcmRecordsNext_TopFlopViews.js";
    private static final String PREFIX = "window.fcmRecordsNextTopFlopViews = ";
    private static final int LIMIT = 1_500_000;
    private static final Map<String,Integer> IDS = new LinkedHashMap<>();
    static {
        IDS.put("2016_2017",706);IDS.put("2017_2018",535);IDS.put("2018_2019",573);
        IDS.put("2019_2020",524);IDS.put("2020_2021",569);IDS.put("2021_2022",549);
        IDS.put("2022_2023",528);IDS.put("2023_2024",523);IDS.put("2024_2025",559);
        IDS.put("2025_2026",549);IDS.put("2026_2027",579);
    }
    private TopFlopSimeoneIdentityV73() {}
    static void require(boolean yes,String msg) {if(!yes)throw new IllegalStateException(msg);}
    static String hex(byte[] b) throws Exception {
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(b);
        StringBuilder x=new StringBuilder();for(byte v:hash)x.append(String.format("%02x",v&255));return x.toString();
    }
    @SuppressWarnings("unchecked")
    static Map<String,Object> jsonMap(Object o) {
        require(o instanceof Map,"Oggetto JSON non riconosciuto");
        return (Map<String,Object>)o;
    }
    @SuppressWarnings("unchecked")
    static List<Map<String,Object>> objectList(Object o) {
        require(o instanceof List,"Array JSON non riconosciuto");
        List<Map<String,Object>> result=new ArrayList<>();
        for(Object item:(List<?>)o)result.add(jsonMap(item));
        return result;
    }
    static Map<String,Object> readFacade(Path file) throws Exception {
        String content=Files.readString(file,StandardCharsets.UTF_8).trim();
        require(content.startsWith(PREFIX)&&content.endsWith(";"),"Facade pubblica inattesa");
        Map<String,Object> f=jsonMap(MiniJson.parse(content.substring(PREFIX.length(),content.length()-1)));
        require("4.0-public-views-v1".equals(f.get("schemaVersion")),"Schema facade inatteso");
        return f;
    }
    static List<Map<String,Object>> readHistoryRows(Path js,List<Map<String,Object>> oldInfo) throws Exception {
        List<Map<String,Object>> rows=new ArrayList<>();
        for(Map<String,Object> f:oldInfo) {
            String name=String.valueOf(f.get("nome"));
            require(name.matches("fcmRecordsNext_TopFlopViews\\.History\\.Storico\\.[0-9]{3}\\.js"),
                "Nome shard History non previsto: "+name);
            Path source=js.resolve(name);
            require(Files.isRegularFile(source) && Files.size(source)<=LIMIT,"Shard History invalido: "+name);
            String text=Files.readString(source,StandardCharsets.UTF_8);
            int i=text.indexOf("a.push(...[");int j=text.lastIndexOf("]);})();");
            require(i>=0&&j>i,"Formato History non valido: "+name);
            List<?> decoded=(List<?>)MiniJson.parse("["+text.substring(i+"a.push(...[".length(),j)+"]");
            require(decoded.size()==((Number)f.get("righe")).intValue(),"Conteggio History incoerente: "+name);
            for(Object r:decoded)rows.add(jsonMap(r));
        }
        return rows;
    }
    static List<TopFlopPlayerIdentities.Source> sources(Connection conn) throws Exception {
        List<TopFlopPlayerIdentities.Source> result=new ArrayList<>();
        for(var entry:IDS.entrySet()) {
            String season=entry.getKey(),table="raw_"+season+"_fca_giocatorea";
            try(PreparedStatement ps=conn.prepareStatement(
                "SELECT ID,CODICEGAZZA,NOME,DATADINASCITA FROM \""+table+"\" WHERE ID=?")) {
                ps.setInt(1,entry.getValue());
                try(ResultSet rs=ps.executeQuery()) {
                    require(rs.next(),"Simeone mancante FCA "+season+" ID="+entry.getValue());
                    int id=rs.getInt(1),code=rs.getInt(2);
                    String name=rs.getString(3),dob=rs.getString(4);
                    require(!rs.wasNull()&&code==(season.equals("2016_2017")?145760:100533),
                        "Codice storico diverso da quello verificato: "+season);
                    require((season.equals("2016_2017")?"SIMEONE Giovanni":"SIMEONE Giovanni Pablo").equals(name),
                        "Nome non coincidente: "+season+" / "+name);
                    require(dob!=null&&dob.startsWith("1995-07-05"),"Data nascita errata: "+season);
                    require(!rs.next(),"ID FCA duplicato: "+season);
                    result.add(new TopFlopPlayerIdentities.Source(season,id,code,name,dob));
                }
            }
        }
        return result;
    }
    static void build(Path db,Path config,Path js,Path stage) throws Exception {
        require(Files.isRegularFile(db)&&Files.isRegularFile(config)&&Files.isDirectory(js),"Percorsi RC/sandbox mancanti");
        require(!Files.exists(stage),"Cartella staging gia esistente: "+stage);
        Map<String,Object> facade=readFacade(js.resolve(FACADE));
        List<Map<String,Object>> metadata=objectList(facade.get("stagioni"));
        require(metadata.size()==21,"Il sito non espone esattamente 21 stagioni");
        Set<String> present=new HashSet<>();
        for(Map<String,Object> s:metadata)present.add(String.valueOf(s.get("stagione")));
        require(present.containsAll(IDS.keySet()),"Stagioni Simeone assenti dal sito");
        Map<String,Object> historic=jsonMap(facade.get("storico"));
        List<Map<String,Object>> previous=readHistoryRows(js,objectList(historic.get("file")));
        List<TopFlopPlayerIdentities.Source> src;
        TopFlopPlayerIdentities.Registry registry=TopFlopPlayerIdentities.load(config);
        Class.forName("org.sqlite.JDBC");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+db.toUri()+"?mode=ro")) {
            src=sources(c);
            for(var s:src)require(!registry.approved().containsKey(s.key()),
                "Identita Simeone gia assegnata: "+s.key()+" / "+registry.approved().get(s.key()));
            registry.bindAudit(TopFlopPlayerIdentities.scanAvailable(c));
            String fingerprint=hex(("SIMEONE_CONFIRMED_2026_11_16:"+
                String.join(",",src.stream().map(TopFlopPlayerIdentities.Source::key).toList())).getBytes(StandardCharsets.UTF_8));
            registry.approve(new TopFlopPlayerIdentities.Proposal(fingerprint,src,false,
                "Conferma esplicita utente 2026-11-16 - Giovanni Simeone"));
        }
        String expectedId="rn4p-2016_2017-706";
        for(var s:src)require(expectedId.equals(registry.approved().get(s.key())),
            "Gruppo Simeone non unificato: "+s.key());
        require(previous.stream().noneMatch(p->expectedId.equals(p.get("playerIdentityId"))),
            "Simeone gia presente nella History del sito");
        List<Path> careerFiles=new ArrayList<>(),teamFiles=new ArrayList<>();
        for(Map<String,Object> row:metadata)for(Map<String,Object> f:objectList(row.get("files"))) {
            String name=String.valueOf(f.get("nome")),kind=String.valueOf(f.get("tipo"));
            if(kind.equals("Careers")||kind.equals("Teams")) {
                require(name.matches("fcmRecordsNext_TopFlopViews\\."+kind+"\\.[0-9]{4}_[0-9]{4}\\.[0-9]{3}\\.js"),
                    "Nome shard "+kind+" invalido: "+name);
                Path input=js.resolve(name);
                require(Files.isRegularFile(input)&&Files.size(input)<=LIMIT,
                    "Shard "+kind+" mancante/oversize: "+name);
                if(kind.equals("Careers"))careerFiles.add(input);else teamFiles.add(input);
            }
        }
        Set<String> keys=new HashSet<>();src.forEach(s->keys.add(s.key()));
        List<Map<String,Object>> careers=TopFlopHistoricalViews.readCareers(js,careerFiles);
        careers.removeIf(r->!keys.contains(r.get("playerKey")));
        require(careers.size()==11&&careers.stream().map(r->String.valueOf(r.get("playerKey"))).distinct().count()==11,
            "Le 11 righe Careers Simeone non sono complete nel sito: "+careers.size());
        List<Map<String,Object>> teams=TopFlopHistoricalViews.readTeams(js,teamFiles,metadata);
        teams.removeIf(r->!keys.contains(r.get("playerKey")));
        List<Map<String,Object>> rebuilt=TopFlopHistoricalViews.aggregate(careers,teams,registry.approved());
        require(rebuilt.size()==1,"Non e stata prodotta una scheda Simeone unica");
        require(((Number)rebuilt.get(0).get("numeroStagioni")).intValue()==11,
            "Carriera Simeone incompleta");
        List<Map<String,Object>> all=new ArrayList<>(previous);all.add(rebuilt.get(0));
        require(all.stream().map(x->String.valueOf(x.get("playerIdentityId"))).distinct().count()==all.size(),
            "Identita duplicate nella History");
        all.sort(Comparator.comparing(x->String.valueOf(x.get("playerIdentityId"))));
        Files.createDirectories(stage);
        TopFlopPlayerIdentities.save(stage.resolve("player-identities.json"),registry);
        var result=TopFlopPublicViewsStageExporter.shard(stage,"Storico","History",new ArrayList<Object>(all));
        List<Map<String,Object>> files=new ArrayList<>();
        for(var f:result) {
            String renamed=f.name().replace(".History.Storico.",".History.Storico.v73.");
            Files.move(stage.resolve(f.name()),stage.resolve(renamed));
            files.add(Map.of("nome",renamed,"tipo","History","righe",f.rows(),
                "byte",f.bytes(),"sha256",f.sha256()));
        }
        Map<String,Object> updatedHistory=new LinkedHashMap<>(historic);
        updatedHistory.put("file",files);updatedHistory.put("identita",all.size());
        updatedHistory.put("status","IDENTITA_APPROVATE");
        facade.put("storico",updatedHistory);
        byte[] fac=(PREFIX+TopFlopCompactStageJsExporter.Json.write(facade)+";\n").getBytes(StandardCharsets.UTF_8);
        require(fac.length<=LIMIT,"Nuova facade oltre limite");
        Files.write(stage.resolve(FACADE),fac);
        System.out.println("SIMEONE_V73_STAGE_OK: 11 stagioni, identita="+expectedId+
            " | precedenti="+previous.size()+" | nuove="+all.size()+" | shard="+files.size());
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=4)throw new IllegalArgumentException("<sqlite> <registry> <sandbox-js> <new-stage>");
        build(Path.of(args[0]),Path.of(args[1]),Path.of(args[2]),Path.of(args[3]));
    }
}
