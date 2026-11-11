package it.alterlega.recordsnext.app.topflop;

import it.alterlega.recordsnext.app.topflop.TopFlopFacts.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TopFlopSettingsSmokeTest {
    private static void check(boolean b,String reason) {if(!b)throw new AssertionError(reason);}
    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("rn4-settings-test");
        try {
            Path config=root.resolve("config/topflop.json");
            TopFlopSettings base=TopFlopSettings.load(config);
            check(base.realSeasonVotes()==5 && base.fantasyCupMatches()==3,"default threshold");
            TopFlopSettings low=new TopFlopSettings(2,20,1,1,20,3,2,8,20,2,new BigDecimal("0.75"));
            low.save(config);
            check(low.equals(TopFlopSettings.load(config)),"JSON roundtrip");
            PlayerSeason id=new PlayerSeason("2026_2027",100,999);
            TopFlopArchive archive=new TopFlopArchive();
            RealDayKey day1=new RealDayKey(id,3),day2=new RealDayKey(id,4);
            archive.addRealDay(new RealDay(day1,VoteState.VALID,new BigDecimal("8"),Map.of(),Map.of()));
            archive.addRealDay(new RealDay(day2,VoteState.VALID,new BigDecimal("6"),Map.of(),Map.of()));
            var profile=new TopFlopFcaJdbcReader.PlayerProfile(id,"PLAYER",Map.of("GiocatoreA.RUOLO","3"));
            var source=Map.of("FCM.Fantasquadra.Nome","Team","FCM.Giornata.Desc","1a");
            archive.addFantasyFact(new FantasyFact(new FantasyKey(id,1,333,31),day1,
                "Serie A",CompetitionFamily.CAMPIONATO,1,true,Usage.COUNTED,new BigDecimal("10"),
                Map.of(),null,source));
            var loaded=new TopFlopJdbcArchiveAssembler.LoadedSeason(archive,
                Map.of(100,profile),List.of(),
                new TopFlopFcaJdbcReader.SeasonLoad("2026_2027",Map.of(100,profile),2,2,2,0,0),
                new TopFlopFcmJdbcReader.SeasonLoad("2026_2027",1,1,0,1,0,0),0,2,0);
            var high=TopFlopPublicViewsStageExporter.build(loaded,Map.of(),TopFlopSettings.defaults());
            var changed=TopFlopPublicViewsStageExporter.build(loaded,Map.of(),low);
            check(high.records.stream().noneMatch(x->"TF05".equals(x.get("recordId"))),"TF05 default too few votes");
            check(high.records.stream().noneMatch(x->"TF06".equals(x.get("recordId"))),"TF06 default too few matches");
            check(changed.records.stream().anyMatch(x->"TF05".equals(x.get("recordId")) &&
                new BigDecimal(x.get("valore").toString()).compareTo(new BigDecimal("7"))==0),"TF05 configurable mean");
            check(changed.records.stream().anyMatch(x->"TF06".equals(x.get("recordId")) &&
                new BigDecimal(x.get("valore").toString()).compareTo(new BigDecimal("10"))==0),"TF06 configurable mean");
            check(changed.records.stream().noneMatch(x->"TF11".equals(x.get("recordId"))),"unverified still disabled");
            Files.writeString(config,"{\"realSeasonVotes\": -5}");
            boolean rejected=false;
            try {TopFlopSettings.load(config);} catch(IllegalArgumentException ex) {rejected=true;}
            check(rejected,"invalid settings must be rejected");
            System.out.println("TOP_FLOP_SETTINGS_OK roundtrip thresholds TF05 TF06 negative");
        } finally {
            try(var paths=Files.walk(root)) {for(Path x:paths.sorted(java.util.Comparator.reverseOrder()).toList())Files.deleteIfExists(x);}
        }
    }
}
