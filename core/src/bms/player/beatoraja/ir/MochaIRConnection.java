package bms.player.beatoraja.ir;

import bms.model.BMSDecoder;
import bms.model.BMSModel;
import bms.model.Mode;
import bms.player.beatoraja.CourseData;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainLoader;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.TableData;
import bms.player.beatoraja.CourseData.CourseDataConstraint;
import bms.player.beatoraja.MainLoader.VersionChecker;
import bms.player.beatoraja.TableData.TableFolder;
import bms.player.beatoraja.input.BMSPlayerInputDevice.Type;
import bms.player.beatoraja.ir.IRTableData.IRTableFolder;
import bms.player.beatoraja.song.SQLiteSongDatabaseAccessor;
import bms.player.beatoraja.song.SongData;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Logger;

public final class MochaIRConnection implements IRConnection {
   public static final String NAME = "mocha";
   public static final String HOME = "https://mocha-repository.info/";
   private final String irheader = "https://mocha-repository.info/api_columbia088/";
   private final String header = "https://mocha-repository.info/";
   private static final boolean SHOW_MESSAGE = false;
   private int id;
   private String key;
   private final String[] lntype = new String[]{"ln", "cn", "hcn"};
   private final String[] gauge = new String[]{"assist_easy", "easy", "normal", "hard", "ex_hard", "hazard", "normal", "hard", "ex_hard"};
   private final String[] clear = new String[]{
      "no_play", "fail", "assist", "l_assist", "easy", "normal", "hard", "ex_hard", "full_combo", "perfect", "max_full_combo"
   };
   private final String[] random = new String[]{"normal", "mirror", "random", "r_random", "s_random"};
   private final String[] dpoption = new String[]{"normal", "flip"};
   private final String[] device = new String[]{"kb", "con", "midi"};
   private static bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse profilecache;

   public IRResponse<IRPlayerData> register(IRAccount var1) {
      bms.player.beatoraja.ir.MochaIRConnection.LogIn var2 = new bms.player.beatoraja.ir.MochaIRConnection.LogIn();
      var2.email = var1.id;
      var2.password = var1.password;
      var2.name = var1.name;

      try {
         return (bms.player.beatoraja.ir.MochaIRConnection.LogInResponse)this.post(
            "https://mocha-repository.info/api_columbia088/register.php", var2, bms.player.beatoraja.ir.MochaIRConnection.LogInResponse.class
         );
      } catch (Exception var4) {
         return this.createErrorMessage(var4.getMessage());
      }
   }

   public IRResponse<IRPlayerData> login(IRAccount var1) {
      // Use vanilla beatoraja 0.8.8 JAR hash for Mocha-Repository compatibility
      String var2 = "fa8fb06126da0b6cb10944a5ba870b9c7bb5eaab344a92378c3602afc312058c";


      bms.player.beatoraja.ir.MochaIRConnection.LogIn var13 = new bms.player.beatoraja.ir.MochaIRConnection.LogIn();
      var13.email = var1.id;
      var13.password = var1.password;
      var13.type = MainLoader.getIllegalSongCount() > 0 ? 2 : 0;

      try {
         MessageDigest var14 = MessageDigest.getInstance("sha-256");
         var14.update(var2.getBytes());
         var14.update(var1.id.getBytes());
         var13.body = BMSDecoder.convertHexString(var14.digest());
      } catch (NoSuchAlgorithmException var9) {
         var9.printStackTrace();
      }

      try {
         bms.player.beatoraja.ir.MochaIRConnection.LogInResponse var15 = (bms.player.beatoraja.ir.MochaIRConnection.LogInResponse)this.post(
            "https://mocha-repository.info/api_columbia088/login.php", var13, bms.player.beatoraja.ir.MochaIRConnection.LogInResponse.class
         );
         if (var15.success) {
            this.id = var15.profile.id;
            this.key = var15.key;
            var15.data = new IRPlayerData(String.valueOf(var15.profile.id), var15.profile.name, var15.profile.classRank);
            Path var16 = Paths.get("mocha_sync.json");
            if (Files.exists(var16)) {
               TableData var6 = TableData.read(var16);
               bms.player.beatoraja.ir.MochaIRConnection.SendResponse var7 = (bms.player.beatoraja.ir.MochaIRConnection.SendResponse)this.sendProfile(var6);
            }
         }

         return var15;
      } catch (Exception var8) {
         var8.printStackTrace();
         return this.createErrorMessage(var8.getMessage());
      }
   }

   public IRResponse<IRPlayerData[]> getRivals() {
      bms.player.beatoraja.ir.MochaIRConnection.GetPlay var1 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlay();
      var1.key = this.key;

      try {
         if (profilecache == null) {
            profilecache = (bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse)this.post(
               "https://mocha-repository.info/api_columbia088/getprofile.php", var1, bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse.class
            );
         }

         bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse var2 = profilecache;
         if (var2.success) {
            ArrayList var3 = new ArrayList(var2.rivals.length);

            for (bms.player.beatoraja.ir.MochaIRConnection.Rival var7 : var2.rivals) {
               IRPlayerData var8 = new IRPlayerData(Integer.toString(var7.id), var7.name, var7.classRank);
               var3.add(var8);
            }

            var2.data = var3.toArray(new IRPlayerData[var3.size()]);
         }

         return var2;
      } catch (Exception var9) {
         return this.createErrorMessage(var9.getMessage());
      }
   }

   public IRResponse<IRTableData[]> getTableDatas() {
      bms.player.beatoraja.ir.MochaIRConnection.GetPlay var1 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlay();
      var1.key = this.key;

      bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse var2;
      try {
         if (profilecache == null) {
            profilecache = (bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse)this.post(
               "https://mocha-repository.info/api_columbia088/getprofile.php", var1, bms.player.beatoraja.ir.MochaIRConnection.GetProfileResponse.class
            );
         }

         var2 = profilecache;
      } catch (Exception var24) {
         return this.createErrorMessage(var24.getMessage());
      }

      if (var2.success) {
         ArrayList var3 = new ArrayList(var2.collections.length);

         for (bms.player.beatoraja.ir.MochaIRConnection.Collection var7 : var2.collections) {
            ArrayList var8 = new ArrayList(var7.courses.length);

            for (bms.player.beatoraja.ir.MochaIRConnection.Course var12 : var7.courses) {
               CourseData var13 = new CourseData();
               var13.setName(var12.name);
               ArrayList var14 = new ArrayList(var12.charts.length);

               for (bms.player.beatoraja.ir.MochaIRConnection.Chart var18 : var12.charts) {
                  SongData var19 = new SongData();
                  var19.setSha256(var18.hash);
                  var19.setTitle(var18.name);
                  var19.setArtist(var18.artist);
                  var19.setGenre(var18.genre);
                  var19.setUrl(var18.url);
                  var19.setAppendurl(var18.sabunUrl);

                  for (Mode var23 : Mode.values()) {
                     if (var23.hint.equals(var18.modeHint)) {
                        var19.setMode(var23.id);
                        break;
                     }
                  }

                  var14.add(var19);
               }

               var13.setSong((SongData[])var14.toArray(new SongData[var14.size()]));
               ArrayList var31 = new ArrayList(var12.type.length);

               for (String var40 : var12.type) {
                  CourseDataConstraint var42 = CourseDataConstraint.getValue(var40);
                  if (var42 != null) {
                     var31.add(var42);
                  }
               }

               var13.setConstraint((CourseDataConstraint[])var31.toArray(new CourseDataConstraint[var31.size()]));
               var13.setRelease(true);
               var8.add(new IRCourseData(var13));
            }

            ArrayList var25 = new ArrayList(var7.folders.length);

            for (bms.player.beatoraja.ir.MochaIRConnection.Folder var29 : var7.folders) {
               ArrayList var30 = new ArrayList(var29.charts.length);

               for (bms.player.beatoraja.ir.MochaIRConnection.Chart var39 : var29.charts) {
                  SongData var41 = new SongData();
                  var41.setSha256(var39.hash);
                  var41.setTitle(var39.name);
                  var41.setArtist(var39.artist);
                  var41.setGenre(var39.genre);
                  var41.setUrl(var39.url);
                  var41.setAppendurl(var39.sabunUrl);

                  for (Mode var46 : Mode.values()) {
                     if (var46.hint.equals(var39.modeHint)) {
                        var41.setMode(var46.id);
                        break;
                     }
                  }

                  var30.add(new IRChartData(var41));
               }

               IRTableFolder var33 = new IRTableFolder(var29.name, (IRChartData[])var30.toArray(new IRChartData[var30.size()]));
               var25.add(var33);
            }

            var3.add(new IRTableData(var7.name, (IRTableFolder[])var25.toArray(new IRTableFolder[var25.size()]), (IRCourseData[])var8.toArray(new IRCourseData[var8.size()])));
         }

         var2.data = var3.toArray(new IRTableData[var3.size()]);
      }

      return var2;
   }

   public IRResponse<IRScoreData[]> getPlayData(IRPlayerData var1, IRChartData var2) {
      if (var1 != null && var2 != null) {
         bms.player.beatoraja.ir.MochaIRConnection.GetPlay var20 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlay();
         var20.id = this.id;
         var20.key = this.key;
         var20.chartHash = var2.sha256;
         var20.playerId = Integer.parseInt(var1.id);

         bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse var21;
         try {
            var21 = (bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse)this.post(
               "https://mocha-repository.info/api_columbia088/getplay.php", var20, bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse.class
            );
         } catch (Exception var17) {
            return this.createErrorMessage(var17.getMessage());
         }

         if (var21 != null && var21.success) {
            var21.data = new IRScoreData[0];

            try {
               var21.data = new IRScoreData[]{this.createScoreData(var21)};
            } catch (ParseException var16) {
               var16.printStackTrace();
            }
         }

         return var21;
      } else {
         bms.player.beatoraja.ir.MochaIRConnection.GetPlay var3 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlay();
         var3.id = this.id;
         var3.key = this.key;
         if (var2 != null) {
            var3.chartHash = var2.sha256;
            var3.lnType = var2.lntype != -1 ? this.lntype[var2.hasUndefinedLN ? var2.lntype : 0] : "";
         } else if (var1 != null) {
            var3.playerId = Integer.parseInt(var1.id);
         } else {
            var3.playerId = this.id;
         }

         bms.player.beatoraja.ir.MochaIRConnection.ScoreArray var4;
         try {
            var4 = (bms.player.beatoraja.ir.MochaIRConnection.ScoreArray)this.post(
               "https://mocha-repository.info/api_columbia088/getplay.php", var3, bms.player.beatoraja.ir.MochaIRConnection.ScoreArray.class
            );
         } catch (Exception var19) {
            return this.createErrorMessage(var19.getMessage());
         }

         if (var4.success) {
            ArrayList var5 = new ArrayList();
            if (var4 != null && var4.success && var4.chartScores != null) {
               for (bms.player.beatoraja.ir.MochaIRConnection.Score var9 : var4.chartScores) {
                  ScoreData var10 = new ScoreData();
                  var10.setSha256(var9.hash != null ? var9.hash : var2.sha256);
                  var10.setPlayer(var9.djName);
                  if (var9.exScore == var9.perfectGreatCount * 2 + var9.greatCount) {
                     var10.setEpg(var9.perfectGreatCount);
                     var10.setEgr(var9.greatCount);
                     var10.setEgd(var9.goodCount);
                     var10.setEbd(var9.badCount);
                     var10.setLpr(var9.poorCount);
                     var10.setEms(var9.missCount);
                  } else {
                     var10.setEpg(var9.exScore / 2);
                     var10.setEgr(var9.exScore % 2);
                  }

                  var10.setCombo(var9.maxCombo);
                  var10.setMinbp(var9.minMissCount);
                  var10.setAvgjudge((long)var9.avgJudge);
                  int var11 = 0;

                  for (int var12 = 0; var12 < this.random.length; var12++) {
                     if (this.random[var12].equals(var9.randomOption)) {
                        var11 += var12;
                        break;
                     }
                  }

                  for (int var22 = 0; var22 < this.random.length; var22++) {
                     if (this.random[var22].equals(var9.randomOption2)) {
                        var11 += var22 * 10;
                        break;
                     }
                  }

                  for (int var23 = 0; var23 < this.dpoption.length; var23++) {
                     if (this.dpoption[var23].equals(var9.doubleOption)) {
                        var11 += var23 * 100;
                        break;
                     }
                  }

                  var10.setOption(var11);
                  if (var9.randomSeed != -1L) {
                     var10.setSeed(var9.randomSeed);
                  }

                  var10.setNotes(var9.maxScore / 2);
                  String var24 = var9.clearType;

                  for (int var13 = 1; var13 < this.clear.length; var13++) {
                     if (var24.equals(this.clear[var13])) {
                        var10.setClear(var13);
                        break;
                     }
                  }

                  String var25 = var9.lnType;

                  for (int var14 = 0; var14 < this.lntype.length; var14++) {
                     if (this.lntype[var14].equals(var25)) {
                        var10.setMode(var14);
                        break;
                     }
                  }

                  SimpleDateFormat var26 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                  try {
                     var10.setDate(var26.parse(var9.dts).getTime() / 1000L);
                  } catch (ParseException var18) {
                     var18.printStackTrace();
                  }

                  var5.add(new IRScoreData(var10));
               }
            }

            var4.data = (IRScoreData[])var5.toArray(new IRScoreData[var5.size()]);
         }

         return var4;
      }
   }

   public IRResponse<IRScoreData[]> getCoursePlayData(IRPlayerData var1, IRCourseData var2) {
      bms.player.beatoraja.ir.MochaIRConnection.GetPlay var3 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlay();
      var3.courseHash = this.createCourseHash(var2);
      boolean var4 = false;

      for (IRChartData var8 : var2.charts) {
         var4 |= var8.hasUndefinedLN;
      }

      var3.lnType = var2.lntype != -1 ? this.lntype[var4 ? var2.lntype : 0] : "";
      if (var3.courseHash == null) {
         bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse var22 = new bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse();
         var22.success = false;
         var22.reason = "Play gauge isn't selected.";
         return var22;
      } else if (var1 != null) {
         var3.id = this.id;
         var3.key = this.key;
         var3.playerId = Integer.parseInt(var1.id);

         bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse var21;
         try {
            var21 = (bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse)this.post(
               "https://mocha-repository.info/api_columbia088/getcourse.php", var3, bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse.class
            );
         } catch (Exception var17) {
            return this.createErrorMessage(var17.getMessage());
         }

         if (var21 != null && var21.success) {
            var21.data = new IRScoreData[0];

            try {
               var21.data = new IRScoreData[]{this.createScoreData(var21)};
            } catch (ParseException var16) {
               var16.printStackTrace();
            }
         }

         return var21;
      } else {
         var3.id = this.id;
         var3.key = this.key;
         if (var1 != null) {
            var3.playerId = Integer.parseInt(var1.id);
         }

         bms.player.beatoraja.ir.MochaIRConnection.ScoreArray var20;
         try {
            var20 = (bms.player.beatoraja.ir.MochaIRConnection.ScoreArray)this.post(
               "https://mocha-repository.info/api_columbia088/getcourse.php", var3, bms.player.beatoraja.ir.MochaIRConnection.ScoreArray.class
            );
         } catch (Exception var19) {
            return this.createErrorMessage(var19.getMessage());
         }

         if (var20 != null && var20.success) {
            ArrayList var23 = new ArrayList();
            if (var20 != null && var20.success && var20.chartScores != null) {
               for (bms.player.beatoraja.ir.MochaIRConnection.Score var10 : var20.chartScores) {
                  ScoreData var11 = new ScoreData();
                  var11.setPlayer(var10.djName);
                  if (var10.exScore == var10.perfectGreatCount * 2 + var10.greatCount) {
                     var11.setEpg(var10.perfectGreatCount);
                     var11.setEgr(var10.greatCount);
                     var11.setEgd(var10.goodCount);
                     var11.setEbd(var10.badCount);
                     var11.setLpr(var10.poorCount);
                     var11.setEms(var10.missCount);
                  } else {
                     var11.setEpg(var10.exScore / 2);
                     var11.setEgr(var10.exScore % 2);
                  }

                  var11.setCombo(var10.maxCombo);
                  var11.setMinbp(var10.minMissCount);
                  var11.setNotes(var10.maxScore / 2);
                  String var12 = var10.clearType;

                  for (int var13 = 1; var13 < this.clear.length; var13++) {
                     if (var12.equals(this.clear[var13])) {
                        var11.setClear(var13);
                        break;
                     }
                  }

                  String var26 = var10.lnType;

                  for (int var14 = 0; var14 < this.lntype.length; var14++) {
                     if (this.lntype[var14].equals(var26)) {
                        var11.setMode(var14);
                        break;
                     }
                  }

                  SimpleDateFormat var27 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                  try {
                     var11.setDate(var27.parse(var10.dts).getTime() / 1000L);
                  } catch (ParseException var18) {
                     var18.printStackTrace();
                  }

                  var23.add(new IRScoreData(var11));
               }
            }

            var20.data = (IRScoreData[])var23.toArray(new IRScoreData[var23.size()]);
         }

         return var20;
      }
   }

   private IRScoreData createScoreData(bms.player.beatoraja.ir.MochaIRConnection.GetPlayResponse var1) throws ParseException {
      ScoreData var2 = new ScoreData();
      if (var1.exScore == var1.perfectGreatCount * 2 + var1.greatCount) {
         var2.setEpg(var1.perfectGreatCount);
         var2.setEgr(var1.greatCount);
         var2.setEgd(var1.goodCount);
         var2.setEbd(var1.badCount);
         var2.setLpr(var1.poorCount);
         var2.setEms(var1.missCount);
      } else {
         var2.setEpg(var1.exScore / 2);
         var2.setEgr(var1.exScore % 2);
      }

      var2.setCombo(var1.maxCombo);
      var2.setMinbp(var1.minMissCount);
      var2.setAvgjudge((long)var1.avgJudge);
      String var3 = var1.clearType;

      for (int var4 = 0; var4 < this.clear.length; var4++) {
         if (var3.equals(this.clear[var4])) {
            var2.setClear(var4);
            break;
         }
      }

      String var10 = var1.lnType;

      for (int var5 = 0; var5 < this.lntype.length; var5++) {
         if (this.lntype[var5].equals(var10)) {
            var2.setMode(var5);
            break;
         }
      }

      String var11 = var1.gaugeType;

      for (int var6 = 0; var6 < this.gauge.length; var6++) {
         if (this.gauge[var6].equals(var11)) {
            var2.setGauge(var6);
            break;
         }
      }

      String var12 = var1.randomOption;

      for (int var7 = 0; var7 < this.random.length; var7++) {
         if (this.random[var7].equals(var12)) {
            var2.setOption(var7);
            break;
         }
      }

      String var13 = var1.inputMode;
      switch (var13) {
         case "kb":
            var2.setDeviceType(Type.KEYBOARD);
            break;
         case "con":
            var2.setDeviceType(Type.BM_CONTROLLER);
            break;
         case "midi":
            var2.setDeviceType(Type.MIDI);
      }

      int var8 = 0;

      while (var8 < this.device.length && !this.device[var8].equals(var13)) {
         var8++;
      }

      SimpleDateFormat var14 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      var2.setDate(var14.parse(var1.dts).getTime() / 1000L);
      return new IRScoreData(var2);
   }

   public IRResponse<Object> sendPlayData(IRChartData var1, IRScoreData var2) {
      bms.player.beatoraja.ir.MochaIRConnection.PostPlay var3 = new bms.player.beatoraja.ir.MochaIRConnection.PostPlay();
      var3.id = this.id;
      var3.key = this.key;
      bms.player.beatoraja.ir.MochaIRConnection.Chart var4 = new bms.player.beatoraja.ir.MochaIRConnection.Chart();
      var4.hash = var1.sha256;
      var4.md5 = var1.md5;
      var4.name = var1.subtitle.length() > 0 ? var1.title + " " + var1.subtitle : var1.title;
      var4.artist = var1.subartist.length() > 0 ? var1.artist + " " + var1.subartist : var1.artist;
      var4.genre = var1.genre;
      var4.maxBpm = (float)var1.maxbpm;
      var4.minBpm = (float)var1.minbpm;
      var4.noteCount = var1.notes;
      var4.total = var1.total;
      var4.defExRank = var1.judge;
      var4.level = var1.level;
      var4.modeHint = var1.mode != null ? var1.mode.hint : "UNKNOWN";
      var4.url = var1.url;
      var4.sabunUrl = var1.appendurl;
      if (var1.values.containsKey("content_no_keysound")) {
         var4.type = 3;
      }

      if ("Konami".equals(var1.values.get("GROUP"))) {
         var4.type = 10;
      }

      var3.skin = var2.skin != null ? var2.skin : "Unknown";
      var3.chart = var4;
      var3.notes = var2.notes;
      var3.passNotes = var2.passnotes;
      var3.perfectGreatCount = var2.epg + var2.lpg;
      var3.greatCount = var2.egr + var2.lgr;
      var3.goodCount = var2.egd + var2.lgd;
      var3.badCount = var2.ebd + var2.lbd;
      var3.poorCount = var2.epr + var2.lpr;
      var3.missCount = var2.ems + var2.lms;
      var3.avgJudge = var2.avgjudge != Long.MAX_VALUE ? (int)var2.avgjudge : Integer.MAX_VALUE;
      var3.clearType = this.clear[var2.clear.id];
      var3.maxCombo = var2.maxcombo;
      var3.lnType = var1.lntype != -1 ? this.lntype[var1.hasUndefinedLN ? var1.lntype : 0] : "";
      var3.gaugeType = var2.gauge == -1 ? "auto_shift" : this.gauge[var2.gauge];
      var3.randomOption = this.random[var2.option % 10];
      var3.randomSeed = var2.seed;
      if (var1.mode != null && var1.mode.player == 2) {
         var3.randomOption2 = this.random[var2.option / 10 % 10];
         var3.doubleOption = this.dpoption[var2.option / 100 % 10];
      }

      var3.assistOptions = new String[0];
      switch (var2.deviceType) {
         case KEYBOARD:
            var3.inputMode = "kb";
            break;
         case BM_CONTROLLER:
            var3.inputMode = "con";
            break;
         case MIDI:
            var3.inputMode = "midi";
      }

      var4.prepare();
      var3.prepare();

      try {
         return (bms.player.beatoraja.ir.MochaIRConnection.SendResponse)this.post(
            "https://mocha-repository.info/api_columbia088/postplay.php", var3, bms.player.beatoraja.ir.MochaIRConnection.SendResponse.class
         );
      } catch (Exception var6) {
         return this.createErrorMessage(var6.getMessage());
      }
   }

   public IRResponse<Object> sendCoursePlayData(IRCourseData var1, IRScoreData var2) {
      if (var2.gauge == -1) {
         bms.player.beatoraja.ir.MochaIRConnection.SendResponse var14 = new bms.player.beatoraja.ir.MochaIRConnection.SendResponse();
         var14.success = false;
         var14.reason = "Play gauge isn't selected.";
         return var14;
      } else {
         bms.player.beatoraja.ir.MochaIRConnection.PostPlay var3 = new bms.player.beatoraja.ir.MochaIRConnection.PostPlay();
         var3.id = this.id;
         var3.key = this.key;
         bms.player.beatoraja.ir.MochaIRConnection.Course var4 = new bms.player.beatoraja.ir.MochaIRConnection.Course();
         var4.name = var1.name;
         var4.hash = this.createCourseHash(var1);
         if (var4.hash == null) {
            bms.player.beatoraja.ir.MochaIRConnection.SendResponse var15 = new bms.player.beatoraja.ir.MochaIRConnection.SendResponse();
            var15.success = false;
            var15.reason = "sha256 can't calcurated.";
            return var15;
         } else {
            var4.charts = new bms.player.beatoraja.ir.MochaIRConnection.Chart[var1.charts.length];
            boolean var5 = false;

            for (int var6 = 0; var6 < var4.charts.length; var6++) {
               IRChartData var7 = var1.charts[var6];
               bms.player.beatoraja.ir.MochaIRConnection.Chart var8 = new bms.player.beatoraja.ir.MochaIRConnection.Chart();
               var8.hash = var7.sha256;
               var8.md5 = var7.md5;
               var8.name = var7.subtitle.length() > 0 ? var7.title + " " + var7.subtitle : var7.title;
               var8.artist = var7.subartist.length() > 0 ? var7.artist + " " + var7.subartist : var7.artist;
               var8.genre = var7.genre;
               var8.maxBpm = (float)var7.maxbpm;
               var8.minBpm = (float)var7.minbpm;
               var8.noteCount = var7.notes;
               var8.total = var7.total;
               var8.defExRank = var7.judge;
               var8.level = var7.level;

               for (Mode var12 : Mode.values()) {
                  if (var7.mode == var12) {
                     var8.modeHint = var12.hint;
                     var5 |= var12.player == 2;
                  }
               }

               var8.prepare();
               var4.charts[var6] = var8;
            }

            var4.type = new String[var1.constraint.length];

            for (int var16 = 0; var16 < var4.type.length; var16++) {
               var4.type[var16] = var1.constraint[var16].name;
            }

            var3.course = var4;
            var3.notes = var2.notes;
            var3.passNotes = var2.passnotes;
            var3.perfectGreatCount = var2.epg + var2.lpg;
            var3.greatCount = var2.egr + var2.lgr;
            var3.goodCount = var2.egd + var2.lgd;
            var3.badCount = var2.ebd + var2.lbd;
            var3.poorCount = var2.epr + var2.lpr;
            var3.missCount = var2.ems + var2.lms;
            var3.avgJudge = var2.avgjudge != Long.MAX_VALUE ? (int)var2.avgjudge : Integer.MAX_VALUE;
            var3.clearType = this.clear[var2.clear.id];
            var3.maxCombo = var2.maxcombo;
            boolean var17 = false;

            for (IRChartData var21 : var1.charts) {
               var17 |= var21.hasUndefinedLN;
            }

            var3.lnType = var1.lntype != -1 ? this.lntype[var17 ? var1.lntype : 0] : "";
            var3.gaugeType = this.gauge[var2.gauge];
            var3.randomOption = this.random[var2.option % 10];
            if (var5) {
               var3.randomOption2 = this.random[var2.option / 10 % 10];
               var3.doubleOption = this.dpoption[var2.option / 100 % 10];
            }

            var3.assistOptions = new String[0];
            switch (var2.deviceType) {
               case KEYBOARD:
                  var3.inputMode = "kb";
                  break;
               case BM_CONTROLLER:
                  var3.inputMode = "con";
                  break;
               case MIDI:
                  var3.inputMode = "midi";
            }

            var3.prepare();

            try {
               return (bms.player.beatoraja.ir.MochaIRConnection.SendResponse)this.post(
                  "https://mocha-repository.info/api_columbia088/postcourse.php", var3, bms.player.beatoraja.ir.MochaIRConnection.SendResponse.class
               );
            } catch (Exception var13) {
               return this.createErrorMessage(var13.getMessage());
            }
         }
      }
   }

   public IRResponse<Object> sendProfile(TableData var1) {
      bms.player.beatoraja.ir.MochaIRConnection.PostProfile var2 = new bms.player.beatoraja.ir.MochaIRConnection.PostProfile();
      var2.id = this.id;
      var2.key = this.key;
      bms.player.beatoraja.ir.MochaIRConnection.Collection var3 = new bms.player.beatoraja.ir.MochaIRConnection.Collection();
      var3.name = var1.getName();
      ArrayList var4 = new ArrayList();

      for (CourseData var8 : var1.getCourse()) {
         bms.player.beatoraja.ir.MochaIRConnection.Course var9 = new bms.player.beatoraja.ir.MochaIRConnection.Course();
         var9.name = var8.getName();
         var9.hash = this.createCourseHash(new IRCourseData(var8));
         if (var9.hash == null) {
            bms.player.beatoraja.ir.MochaIRConnection.SendResponse var10 = new bms.player.beatoraja.ir.MochaIRConnection.SendResponse();
            var10.success = false;
            var10.reason = "sha256 can't calcurated.";
            return var10;
         }

         var4.add(var9);
      }

      var3.courses = (Course[])var4.toArray(new Course[var4.size()]);
      ArrayList var18 = new ArrayList();

      for (TableFolder var22 : var1.getFolder()) {
         bms.player.beatoraja.ir.MochaIRConnection.Folder var23 = new bms.player.beatoraja.ir.MochaIRConnection.Folder();
         var23.name = var22.getName();
         ArrayList var11 = new ArrayList();

         for (SongData var15 : var22.getSong()) {
            bms.player.beatoraja.ir.MochaIRConnection.Chart var16 = new bms.player.beatoraja.ir.MochaIRConnection.Chart();
            var16.name = var15.getTitle();
            var16.hash = var15.getSha256();
            var11.add(var16);
         }

         var23.charts = (Chart[])var11.toArray(new Chart[var11.size()]);
         var18.add(var23);
      }

      var3.folders = (Folder[])var18.toArray(new Folder[var18.size()]);
      var2.collection = var3;

      try {
         return (bms.player.beatoraja.ir.MochaIRConnection.SendResponse)this.post(
            "https://mocha-repository.info/api_columbia088/postprofile.php", var2, bms.player.beatoraja.ir.MochaIRConnection.SendResponse.class
         );
      } catch (Exception var17) {
         return this.createErrorMessage(var17.getMessage());
      }
   }

   private String createCourseHash(IRCourseData var1) {
      StringBuilder var2 = new StringBuilder();

      for (IRChartData var6 : var1.charts) {
         if (var6.sha256 == null || var6.sha256.length() != 64) {
            return null;
         }

         var2.append(var6.sha256);
      }

      for (CourseDataConstraint var12 : var1.constraint) {
         var2.append(var12.name);
      }

      try {
         MessageDigest var9 = MessageDigest.getInstance("sha-256");
         var9.update(var2.toString().getBytes());
         return BMSDecoder.convertHexString(var9.digest());
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
         return null;
      }
   }

   private Object post(String var1, Object var2, Class var3) throws Exception {
      try {
         Json var4 = new Json();
         var4.setOutputType(OutputType.json);
         var4.setIgnoreUnknownFields(true);
         String var5 = var4.toJson(var2);
         URL var6 = new URL(var1);
         HttpURLConnection var7 = (HttpURLConnection)var6.openConnection();
         var7.setRequestMethod("POST");
         var7.setDoOutput(true);
         var7.setInstanceFollowRedirects(false);
         var7.setRequestProperty("User-Agent", "beatoraja/test");
         var7.connect();

         try (PrintWriter var8 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(var7.getOutputStream(), "utf-8")))) {
            var8.print(var5);
         } catch (IOException var16) {
            var16.printStackTrace();
            return null;
         }

         try {
            Object var10;
            try (BufferedReader var18 = new BufferedReader(new InputStreamReader(var7.getInputStream(), "UTF-8"))) {
               Object var9 = var4.fromJson(var3, var18);
               var10 = var9;
            }

            return var10;
         } catch (Exception var14) {
            throw var14;
         }
      } catch (Exception var17) {
         throw var17;
      }
   }

   public String getSongURL(IRChartData var1) {
      return var1 != null && var1.sha256 != null ? "https://mocha-repository.info/song.php?sha256=" + var1.sha256 : null;
   }

   public String getCourseURL(IRCourseData var1) {
      if (var1 != null) {
         String var2 = this.createCourseHash(var1);
         if (var2 != null) {
            return "https://mocha-repository.info/course.php?sha256=" + var2;
         }
      }

      return null;
   }

   public String getPlayerURL(IRPlayerData var1) {
      return var1 != null && var1.id != null ? "https://mocha-repository.info/player.php?id=" + var1.id : null;
   }

   private static String convertHexString(byte[] var0) {
      StringBuilder var1 = new StringBuilder(var0.length * 2);

      for (byte var5 : var0) {
         var1.append(Character.forDigit(var5 >> 4 & 15, 16));
         var1.append(Character.forDigit(var5 & 15, 16));
      }

      return var1.toString();
   }

   private IRResponse createErrorMessage(String var1) {
      bms.player.beatoraja.ir.MochaIRConnection.LogInResponse var2 = new bms.player.beatoraja.ir.MochaIRConnection.LogInResponse();
      var2.success = false;
      var2.reason = var1;
      return var2;
   }

   static {
      String[] var0 = new String[]{
         "008c13e1e7614f0e5a7fd0894d11f1e370b1e868179399dfe94e50849e88fbc4",
         "0481b2e5006d4a9036d23f0f68b730f646f11c556596ed694bfbf30b2157715b",
         "051a1f7bb5fbd30f63b14c70d1f4587b07ff2840159e5c2ac8aef74ee80b798b",
         "088879daf9e8d227432a09675db6691fd3dbe9bc9f09faa1db16c0e9a407342f",
         "0e86ecb1edb1d4f9d2038eb7213a1ac71ef06af5d16dea87a250bb50a4c8ff4f",
         "0f563fdb40e76614708545b7bc0dcdc344a013347d0d62827233f016de5e9696",
         "109ca1f619c85858720b5b86db1aaf57915e75442cd2ed915580b087ce318187",
         "1255914507d9d4575511283ca16f946585978b0abe82cbc7cc82ec58f6b4ffb1",
         "18977b4c53df395b4d52c2bf9e9582d8c08c103c302b0a663eb54d35a1b62075",
         "195b761f833ea658e682fc739faf09ef786e510863a76c7ef63afaee6561caee",
         "1cb555053cd6574d72c6eb12680b466d70655d5d6906ab19eea3c5451ac42afd",
         "1d1718da778c17836b17a17f4e69a9bb0e6c1f1dbedb9364dd5ef3fa1fc0e3f2",
         "21615dfba2b890b2f3852bad9d9920ea9295ffb59b67a96f6a411c37532ca1ab",
         "25371531e09f38519aabd8d0013fdb6ef46eaff504089ee7a954f4ecd059c364",
         "2a7c473803c1eeb17234de003fc7fd4679d1d595ee97541203f6ee66c5d3f17a",
         "30f9ec8af4d4e75588d6211d3c5358b22b313fbcfcc068f954635fbb7d874902",
         "33e8de8561fada481619273ab4f8e07f3b6e6914719120945f887cb117896a4d",
         "34891d955cab968991b368d56cb5b6ba90dbb50b6d7cbf0f7fdfc1b8a0746875",
         "36d2f49eca6a29570e42514c80bf9f0aa2cf0408d23e62fcdd0b9db7471d1bd2",
         "3ba1cdff2d592da3dcf5b898806244f5325d099e30346949711b4bf87978d7fa",
         "3c7611fc34876e00435730a7e136c78339cfc62b68ce7b13fc8254b3b0a99ed4",
         "40e65c9f1e520a2518fcba4a1ee0a24007cf8acb299cce4caed79facb1865083",
         "42703527ecf9176b5867b923ebc9dc90d55f63758b698721484d9e103433b349",
         "44ecace7bc1bba29728a1a7f360e9b96ad2a0ade3a0a177f96d80342ded62166",
         "45806fc75d45437b4087a727b57f94161a8fdbcd7100e75caa7ea458e676443a",
         "4651fbb127fc7110638f7faa38aaa823ece0f1b36c06aa4058e757fa5661d829",
         "4662ff99e0b0c8d28275ab31d712f67c2956baba323ee9810fc515af8dfb5319",
         "4684f890684bb0a5ced8442272a7476a6256fd9b57151e5e94a58bfa74825c90",
         "4885118068dbe08fa59ca19ea91196cb4faed922ef17de4befed504b2a8a62de",
         "503f98936e5ef9918bf5939447d7126e3dc003c1d77004453e62663a97473d74",
         "5267975bb6630b18dbd4b0c77aa5b7a2e54c36422c5cfd569ba3605d83950996",
         "543d033f57c45cf04927a1fa6f7a57818231afb8c65949deee2b9044eca1b82e",
         "57615f3605409a0c59337a695f2e7aedef9c5b51904f5409880bf78dea049f24",
         "5a0374b49dadb42052239cfa6b675e7cdd84f8def088d5bd7f9b2d608d419779",
         "6416bc12580d60da78b2ee0a8437c3636bfc8158ef6c573171a51c3f34c07567",
         "652b68e4cb7a2629029f350cb41ec713eb60827b37c02293bdb09ff696a423d2",
         "68e0605e4d1660686da40f27efc1a39ebdac945803e05d44383b42a985cae1ba",
         "6a0e923edd2f3fa989c69dfa29b3168da433156c4dfd9cc5c6b81d42da597c97",
         "6acc85ed6448de152ca2830cf484e877a5b767675695aa1a95cf0ab2527f3c81",
         "6bc205746f49450d9dbc9ba4fad2959a3bb38dc898afac317d4e2922baaf9bf8",
         "6c3e888ac7f3e460148fac3d41f16d4ce2ce2893a85ef6fdfa5e6481d43ee80f",
         "6d28469defa4f54c03c3955eb191534b4668ac9544c2240436f1e598ccd16f87",
         "6edc047567e940a776cfeccb960a5ad9bb6e685ceab56be532046ff510830bc8",
         "6ff2b347c6c37a492832be3b5a737ec4b761d5f5264f6edb8fc68bf6bba14ea5",
         "703b9436e834901a9dd5671e8917c71c1a3d67a57b9464e411aa5e01f054cabc",
         "7ae0b585b29f78ca3a7d9d3720e3c87e10745414dd101b40da8ee628d8086f5d",
         "7cd13ff275af2b6b40ab57e8c16cf870aec308e8c299ac3104becfdcc45165fb",
         "7ec33424bb7686bd7616a8b58596a43b361358bfebd79cfdfd5b2df2b7bec81a",
         "7efccbf9bcef2b4922ec1827064f18b9941b979b2f4cf768bdf5f2da6b0d5742",
         "84045f63acae44b540c876b9dafc0e7c5c273f47dc7ef1dad5a00526051e8253",
         "84b7aff662033347fc578b21f3353bfc3731c0f6b912357b5ad5c3bbf84e76ba",
         "89850b05754f72e714e7148c795d1c87cdd68955563b80db7758256d687ee499",
         "8991fa2e9bb8b67c5a66c7e808289a6ad9405558687195b008f84656c7f1a1f7",
         "89ddf7f9b04aa96402340184e2a5e5dfe8c611626c53f2809ac599b61f8084a1",
         "8a346dc2ee417105775e8f1bbba80368e509bf650bf7dff8ad689e29a9bc8e5c",
         "8ca8e9b68e9cfc90821631fe4b8ab9500d134b0a77a5e609e6460d57feebf084",
         "90cae6753c2e52c3a44b9b1c55f70a9c45698a60a7d990ef2144eaf4a1215c92",
         "9461f821c3e4593a5baff295f813447413587ab84f94c631ef031ce37f0c4cab",
         "9489526e53064330adf9d44f70534aa00b42dfbc0e15a5105928916ca4e4fc12",
         "9997f63c53fb7315eb53359847a68d2171e4d09303b5ab128903a617b7b257df",
         "9a73ac01dd01ec509937c3c29ad7246be243cbb020bcc992e64667253b7a3ea8",
         "a0d3ec3208d165d1dfd622dfa9a42f373038505cbf8217ee150f86af1028d194",
         "a2106165e905d4680515b74ce9e9bbac57869cd8660b03333d3498096af415f1",
         "a23a2642f69ecc40f3bbd56fb0c3856b3b4227b25e497c678545a304b5bbaeab",
         "a2dbf18796e652a8e46f6debe95e0127cf9fa6d2fc43f7d0a4f7c541f624a54d",
         "a38f398b6c0883b7409b9c632e52ca5f4df18f97bc86bdcdc84c8b4733ed565a",
         "a3916f1f688a49a9898f6e18bf63e0c0725c1eb49d61ba1ccd644473f5747fbd",
         "a5bdf8c65795ae982e225f3f7684d5813a3831eeeea105cdfcc897bc9735da78",
         "a735d855e6af27d679c4482a32b4475e3625b49ee174d84bfddd85ccea554aba",
         "a7d1c3a1d9fea2097332bcbe30f500d9250b7f50b2c56ae318feb510842e2c9f",
         "a927d10371c5325425ba7c856a7afa31c38a652ff89013986b392b8aaf5b5f0a",
         "a987011f7a7ccf470bdbdd322e8afeb149712ee0088150704285ac49669324a8",
         "a9d7d0afa185d6ff05e87445bbed95aeb2c0d449fe828100daf57e69b1c98a2f",
         "ac69f545a16e1fa65f81de4b17a53b8faa5d1c3756f3207a0c44eb39e0e4cc1d",
         "ad4909acd3d13a2a2e2e369842e464751f5f52e5073cb74f87b39cedcdc62c2c",
         "ae7fe5a807bedb30eeea17d1bbaf07cdc06115f06d24430f3a8542b94d046535",
         "b4e9a04dbd6c0471382ece0775c15d84d4c0b730f948dd5f7c10714e3b76ff0e",
         "b58bbd88bff4dd1f03cddf09a8417d08e4e29acfea47c93748b00f8069fea4d1",
         "b745eaf576827ec502b0ab845c8a48c7592dec3b345019d61f9daa3eeab3e01b",
         "b7bfffa677527eb5090229f3e807a05c7a7c46c1fd8143dbce9ba42e1d5ab234",
         "b92211b6bc29c4ba6e7a44608839ed0bc31d210e198d4d16c413de290a646130",
         "b94bd79bc199d0dd071eca0bb5f1236f48d566e6f27120114af75fa885e490b2",
         "bbf85a60c50850f7ddbe714f06676b0414d25740fa5d9bb5a9e6109e41a0d27e",
         "c0c5e4a448adc9f851d4f3b8c8ff884e6157c4b1c0eb0474f2ab3fcd6ce22299",
         "cc3110b48214912c63c9b58e7333b9e3d9d0dbe3f030f35be30c7d532dca5420",
         "d245d56c5fb2f5ba245d9dec7deea45af0199a51691eafdd5eaa136054c43b9c",
         "d2d1bf0b35db942fb5656dee739d99c7d190d8a1d39f4b4a0084237f833ec896",
         "d5238ace28a4594425213ed34471b87dd0f8c0507e533af34ee0188f615657a3",
         "d561f2b7643d529b7b7916a87ee099b1679d56f3b24bb98e75ba6117c3d7ef6c",
         "d6bb15b687d285a9158ae7307aab559a95e6a3ba1919320cf05557d2395508e3",
         "d82fcdc04c450905aa499f6980de33450f1e8d63ce83f049d37c6422be589802",
         "da40423e3190b561e295025e0733c9799637acf9019a2a69a5fa2b401cd46789",
         "df0c8d6708c3fbeadc675402ac467f84b54cace659d9810a5b5aa67a8c10311a",
         "e1b13637e63afda387771321aa48eec985f92afd5112978fbcfe97444f11796a",
         "e1f000d8f62c1d266999ab5a71c11799d7497057a7b658f98eda56d7afeb90f1",
         "e2ec4c11a6f4326eb74e65b82917f1ad82f7da44619d24ee0f0b797680e8d5c0",
         "e40a0a54ae48bb0bc18caa762f0c62f227db2190c851a6db9ac39f7b226c8a20",
         "e46ac12082a1c9403a4de56bb7e28cde86f0eb399eb97ec1fc155da09c355c98",
         "e73e181826897a995f6be659d57c38d8aed54a65f0d40d7297ba0182649695bf",
         "e93d2562ee85deab062f6f08173825e1a07984776bc1a0d3eed061cb377e0ef5",
         "e9c5659b35d03f907fbd93ac0012a3eeccc30be87b77430171d29b00b04c4149",
         "ea72ce76a0e8b5b0120bb11665ab72be6fd1c81a9aa76dc498dde509f83a7dee",
         "ede4e054659f0d4a38fa50edc718505482339315acd518f7175c8ce72db1104b",
         "f0aa75ebe9bf6549e75dfa924ea5ec79b5a5e0189b7e92f1516be41ff317d09b",
         "f5c9448d505520ffcbbd76e58eb8eeee4ed4676733bd05ba720520975e28c2d6",
         "f694a4488cef72564d5c79c4118dccf2783b6d7247e92da328b0507020095b2b",
         "f886d8b2ef0213745a426bed1e2927eadc8eee12f0c68efd227783f7e2f7004b",
         "fc365ed3f54fb8ffb717fd25963839c9e1d8c7dc480b64654fe893bc27f9e54c",
         "fd1a6796d45ab31a169e27077a8d40faad046e059d0410b33dfbbfb1e6c847dc",
         "ff708502fa6498ad38e759dc7271f6599e9f381030d6805b803c9096625a2e8e"
      };
      SQLiteSongDatabaseAccessor var1 = (SQLiteSongDatabaseAccessor)MainLoader.getScoreDatabaseAccessor();
      var1.addPlugin((var0x, var1x) -> {
         if ("Konami".equals(var0x.getValues().get("GROUP"))) {
            var1x.setContent(65536);
            MainLoader.putIllegalSong(var1x.getSha256());
         }
      });

      for (SongData var5 : var1.getSongDatas("content", "65536")) {
         MainLoader.putIllegalSong(var5.getSha256());
      }

      for (SongData var14 : var1.getSongDatas(var0)) {
         MainLoader.putIllegalSong(var14.getSha256());
      }

      if (MainLoader.getBMSPath() != null) {
         try {
            BMSModel var9 = BMSDecoder.getDecoder(MainLoader.getBMSPath()).decode(MainLoader.getBMSPath());
            if ("Konami".equals(var9.getValues().get("GROUP"))) {
               MainLoader.putIllegalSong(var9.getSHA256());
            }

            for (String var6 : var0) {
               if (var6.equals(var9.getSHA256())) {
                  MainLoader.putIllegalSong(var6);
               }
            }
         } catch (Throwable var7) {
         }
      }
   }

   static final class Chart {
      String hash;
      String md5;
      String name;
      String artist;
      String genre;
      float minBpm;
      float maxBpm;
      int noteCount;
      int defExRank;
      Integer total;
      int level;
      String modeHint;
      String url;
      String sabunUrl;
      String chartHash;
      int type;

      private void prepare() {
         try {
            MessageDigest var1 = MessageDigest.getInstance("sha-256");
            var1.update((this.hash + "," + this.name + "," + this.modeHint).getBytes("utf-8"));
            this.chartHash = bms.player.beatoraja.ir.MochaIRConnection.convertHexString(var1.digest());
         } catch (Exception var2) {
         }
      }
   }

   static class ChromaIRResponse<T> implements IRResponse<T> {
      boolean success;
      String reason = "";
      T data;

      public boolean isSucceeded() {
         return this.success;
      }

      public String getMessage() {
         return this.reason;
      }

      public T getData() {
         return this.data;
      }
   }

   static class Collection {
      int id;
      String name;
      bms.player.beatoraja.ir.MochaIRConnection.Folder[] folders;
      bms.player.beatoraja.ir.MochaIRConnection.Course[] courses;
      bms.player.beatoraja.ir.MochaIRConnection.Course[] classCourses;
   }

   static final class Course {
      String hash;
      String name;
      String[] type;
      bms.player.beatoraja.ir.MochaIRConnection.Chart[] charts;
   }

   static class Folder {
      int id;
      String name;
      bms.player.beatoraja.ir.MochaIRConnection.Chart[] charts;
   }

   static class GetPlay {
      int id;
      String key;
      String chartHash;
      String courseHash;
      String mechSet;
      int playerId;
      String lnType;
   }

   static class GetPlayResponse extends bms.player.beatoraja.ir.MochaIRConnection.ChromaIRResponse<IRScoreData[]> {
      int perfectGreatCount;
      int greatCount;
      int goodCount;
      int badCount;
      int poorCount;
      int missCount;
      int maxCombo;
      String clearType;
      int exScore;
      int avgJudge = Integer.MAX_VALUE;
      int minMissCount = Integer.MAX_VALUE;
      String ghost;
      String lnType;
      String gaugeType;
      String randomOption;
      String[] assistOptions;
      String inputMode;
      String dts;
   }

   static class GetProfileResponse<T> extends bms.player.beatoraja.ir.MochaIRConnection.ChromaIRResponse<T> {
      String email;
      String name;
      String classRank;
      bms.player.beatoraja.ir.MochaIRConnection.Rival[] rivals;
      bms.player.beatoraja.ir.MochaIRConnection.Collection[] collections;
   }

   static class LogIn {
      String email;
      String password;
      String name;
      String body;
      int type;
   }

   static class LogInResponse extends bms.player.beatoraja.ir.MochaIRConnection.ChromaIRResponse<IRPlayerData> {
      String key;
      bms.player.beatoraja.ir.MochaIRConnection.Rival profile;
   }

   static final class PostPlay {
      int id;
      String key;
      bms.player.beatoraja.ir.MochaIRConnection.Chart chart;
      bms.player.beatoraja.ir.MochaIRConnection.Course course;
      String mechSet;
      int notes;
      int passNotes;
      int perfectGreatCount = Integer.MIN_VALUE;
      int greatCount = Integer.MIN_VALUE;
      int goodCount = Integer.MIN_VALUE;
      int badCount = Integer.MIN_VALUE;
      int poorCount = Integer.MIN_VALUE;
      int missCount = Integer.MIN_VALUE;
      int avgJudge = Integer.MAX_VALUE;
      String clearType;
      int maxCombo = Integer.MIN_VALUE;
      String ghost;
      String skin;
      String lnType;
      String gaugeType;
      String randomOption;
      long randomSeed;
      String randomOption2;
      String doubleOption;
      String[] assistOptions;
      String inputMode;
      String scoreHash;

      private void prepare() {
         try {
            MessageDigest var1 = MessageDigest.getInstance("sha-256");
            var1.update((this.key + ",").getBytes());
            var1.update(((this.course != null ? this.course.hash : this.chart.hash) + ",").toUpperCase().getBytes());
            var1.update(
               (Math.abs(Math.multiplyExact(this.notes, Math.min(this.notes, Math.decrementExact(Math.subtractExact(this.passNotes, this.passNotes))))) + ",")
                  .getBytes()
            );
            var1.update((this.passNotes + ",").getBytes());
            var1.update((Math.incrementExact(Math.incrementExact(Math.incrementExact(Math.incrementExact(this.perfectGreatCount)))) + ",").getBytes());
            var1.update((Math.decrementExact(Math.incrementExact(Math.incrementExact(Math.incrementExact(this.greatCount)))) + ",").getBytes());
            var1.update((Math.decrementExact(Math.incrementExact(this.goodCount)) + ",").getBytes());
            var1.update((Math.incrementExact(Math.incrementExact(Math.decrementExact(Math.incrementExact(this.badCount)))) + ",").getBytes());
            var1.update((Math.incrementExact(Math.incrementExact(Math.incrementExact(Math.incrementExact(this.poorCount)))) + ",").getBytes());
            var1.update(
               (Math.abs(Math.multiplyExact(Math.incrementExact(Math.subtractExact(this.passNotes, this.passNotes)), -this.missCount)) + ",").getBytes()
            );
            var1.update((this.avgJudge + ",").getBytes());
            var1.update((this.clearType + ",").getBytes());
            var1.update((this.maxCombo + ",").getBytes());
            var1.update(
               ((this.lnType.toLowerCase().toUpperCase() + this.lnType.toUpperCase() + this.key).substring(this.lnType.length(), this.lnType.length() * 2)
                     + ",")
                  .getBytes()
            );
            var1.update((this.gaugeType + ",").getBytes());
            var1.update(this.randomOption.getBytes());
            this.scoreHash = bms.player.beatoraja.ir.MochaIRConnection.convertHexString(var1.digest());
         } catch (Exception var2) {
         }
      }
   }

   static class PostProfile {
      int id;
      String key;
      bms.player.beatoraja.ir.MochaIRConnection.Collection collection;
   }

   static class Rival {
      int id;
      String name;
      String classRank;
   }

   static class Score {
      String hash;
      String lnType;
      int rank;
      String djName;
      String clearType;
      int exScore;
      int perfectGreatCount;
      int greatCount;
      int goodCount;
      int badCount;
      int poorCount;
      int missCount;
      int avgJudge = Integer.MAX_VALUE;
      int maxCombo;
      int minMissCount = Integer.MAX_VALUE;
      int maxScore;
      String randomOption;
      long randomSeed = -1L;
      String randomOption2;
      String doubleOption;
      String dts;
   }

   static class ScoreArray extends bms.player.beatoraja.ir.MochaIRConnection.ChromaIRResponse<IRScoreData[]> {
      bms.player.beatoraja.ir.MochaIRConnection.Score[] chartScores;
   }

   static class SendResponse extends bms.player.beatoraja.ir.MochaIRConnection.ChromaIRResponse<Object> {
   }
}
