package omc;

import omc.corba.OMCInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OmcExecuter {
  private final String omcExecutable;
  private final String locale;
  private final Logger log;

  public OmcExecuter(String omcExecutable, String locale) {
    this.omcExecutable = omcExecutable;
    this.locale = locale;
    this.log = LoggerFactory.getLogger(OmcExecuter.class);
  }

  public Process startOmc(String... arguments) {
    List<String> cmd = new LinkedList<>();
    cmd.add(omcExecutable);
    cmd.addAll(List.of(arguments));

    ProcessBuilder pb = new ProcessBuilder(cmd);
    //set environment
    Map<String,String> env = pb.environment();
    env.put(OMCInterface.localeEnvVariable, locale);

    Path omcWorkingDir = Global.tmpDir.resolve("omc_home");
    Path logFile = omcWorkingDir.resolve("omc.log");
    try {
      //setup working directory & log file
      Files.createDirectories(omcWorkingDir);
      Files.deleteIfExists(logFile);
      Files.createFile(logFile);
    } catch (IOException e) {
      log.error("Couldn't create working directory or logfile for omc", e);
      throw new IllegalStateException("Couldn't create working directory or logfile for omc");
    }

    pb.directory(omcWorkingDir.toFile());
    pb.redirectErrorStream(true); //merge stderr into stdin
    pb.redirectOutput(logFile.toFile());

    try {
      Process process = pb.start();
      log.info("started {} {} - locale {} - output redirecting to: {}",
        cmd, locale, logFile);
      return process;
    } catch (IOException e) {
      log.error("Couldn't start {} {} as subprocess in {}", cmd, omcWorkingDir,  e);
      throw new IllegalStateException("couldn't start omc!");
    }
  }
}