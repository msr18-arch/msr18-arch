package architecture.main;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import architecture.commons.Compilable;
import architecture.commons.files.FileHandler;
import architecture.database.AbstractDatabase;

public class Main {
	private AbstractDatabase database;

	// private CommitToArchitecture comToArc;
	private CompareAndSave compSave;

	private static String project;

	private final static String BASE_FOLDER = "extracted/";

	private final String PROJECT_FOLDER;
	private final String ARC_FOLDER;
	private final String COMPILABLE_JSON_PATH;
	private final String DIFF_JSON;
	private final String JOB_LOG_FOLDER;

	static Logger log = Logger.getLogger(Main.class);

	public Main() {
		JOB_LOG_FOLDER = BASE_FOLDER + project + "/buildLogs/";
		PROJECT_FOLDER = BASE_FOLDER + project + "/projects/";
		ARC_FOLDER = BASE_FOLDER + project + "/architectures/";
		DIFF_JSON = BASE_FOLDER + project + "/versionDiff.json";
		COMPILABLE_JSON_PATH = BASE_FOLDER + project + "/compilable.json";
	}

	public static void main(String[] args) {
		File log4jfile = new File("cfg/log4j.properties");
		PropertyConfigurator.configure(log4jfile.getAbsolutePath());

		int numbuilds;
		if (args.length != 2) {
			log.error("Wrong parameters. Use [Project] [NumBuilds] - Use [NumBuilds]=0 for all -1 for creatingFile");
			return;
		} else {
			project = args[0];
			numbuilds = Integer.parseInt(args[1]);

		}

		if (numbuilds == -1) {
			Factory.createDatabaseFile(project);
			return;
		}

		Main main = new Main();
		main.init();

		if (numbuilds == 0) {
			numbuilds = main.database.getBuildList().size();
		}

		log.info("Start Computation on " + numbuilds + " commits");
		main.extractAndCompare(numbuilds);

		main.finish();
	}

	private void init() {
		database = Factory.createDatabase(project);

		// comToArc = Factory.createCommitToArchitecture(PROJECT_FOLDER,
		// ARC_FOLDER, PROJECT);
		compSave = Factory.createCompareAndSave(DIFF_JSON);

	}

	private void finish() {
		try {
			log.info("Storing results");
			compSave.storeJSON();
		} catch (IOException e) {
			log.warn("Couldn't write Diff JSON");
		}
	}

	private void extractAndCompare(int numberVersions) {
		List<Integer> builds = database.getBuildList();
		Map<String, Optional<File[]>> architectures = new ConcurrentHashMap<String, Optional<File[]>>();

		if (builds.size() < numberVersions) {
			log.warn("Not enough Versions in Database");
		}

		log.info("Check if Build compiles");
		CompilableList buildsCompilable = Factory.createCompilableList(COMPILABLE_JSON_PATH);
		IntStream.range(0, numberVersions).parallel().forEach(i -> {
			int build = builds.get(i);
			String commit = database.getCommit(build);
			String jobID = database.getJobID(build);
			
			if(!buildsCompilable.contains(commit)) {
				BuildResultAnalyzer brAnalyzer = Factory.createBuildResultAnalyzer(JOB_LOG_FOLDER, project);

				int errorType = brAnalyzer.getFailReason(jobID);
				//System.out.println(errorType);
				buildsCompilable.add(new Compilable(commit, errorType));
			} else {
				log.info("Build already checked for compilation");
			}
		});
		
		try {
			Factory.createCompilableList(COMPILABLE_JSON_PATH).storeJSON();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		IntStream.range(0, numberVersions).parallel().forEach(i -> {
			// for(int i = 0; i < numberVersions; i++) {
			String commit = database.getCommit(builds.get(i));

			if ((i == 0 && compSave.nextCalclulated(builds.get(i))) || compSave.metricsCalculated(builds.get(i))
					|| (i == numberVersions - 1 && compSave.prevCalclulated(builds.get(i)))) {
				log.info("Metrics for " + commit + " already calculated, skipping reconstruct");
				// Empty is acceptable, because the value is never going to be
				// used
				architectures.put(commit, Optional.empty());
				// continue;
				return; // For Each only returns this iteration
			}

			try {
				CommitToArchitecture comToArc = Factory.createCommitToArchitecture(PROJECT_FOLDER, ARC_FOLDER,
						COMPILABLE_JSON_PATH, project);
				architectures.put(commit, Optional.of(comToArc.downloadAndReconstruct(commit, true)));
			} catch (IOException e) {
				System.err.println("Could't extract Version " + commit);
				e.printStackTrace();
			}
			// }
		});

		if (architectures.size() < numberVersions) {
			log.error("At least one version was not downloaded. Aborting comparison");
			return;
		}

		IntStream.range(1, numberVersions).parallel().forEach(i -> {
			// for(int i = 1; i < numberVersions; i++) {
			int first = builds.get((i - 1));
			int second = builds.get(i);

			String firstCommit = database.getCommit(first);
			String secondCommit = database.getCommit(second);

			Optional<File[]> firstFiles = architectures.get(firstCommit);
			Optional<File[]> secondFiles = architectures.get(secondCommit);

			if (firstFiles.isPresent() && secondFiles.isPresent()) {

				if (i % 2 == 0) {
					synchronized (firstFiles.get()) {
						synchronized (secondFiles.get()) {
							compare(first, firstFiles.get(), second, secondFiles.get());
						}
					}
				} else {
					synchronized (secondFiles.get()) {
						synchronized (firstFiles.get()) {
							compare(first, firstFiles.get(), second, secondFiles.get());
						}
					}
				}

			}
			// }
		});

		log.info("Delete Files");
		for (int i = 0; i < numberVersions; i++) {
			int build = builds.get(i);
			Optional<File[]> files = architectures.get(database.getCommit(build));
			if (files.isPresent()) {
				if (((i == 0 && compSave.nextCalclulated(build)) || compSave.metricsCalculated(build)
						|| (i == numberVersions - 1 && compSave.prevCalclulated(build)))) {
					try {
						FileHandler.remove(files.get()[0].getParentFile());
					} catch (IOException e) {
						log.warn("Could not remove Architecture Files for " + database.getCommit(build));
						e.printStackTrace();
					} catch (NullPointerException e) {
						// Intended behaviour
					}
				}
			}
		}
		

		log.info("Done");

	}

	private void compare(int first, File[] firstFiles, int second, File[] secondFiles) {
		ImmutablePair<Integer, File[]> arcOne = new ImmutablePair<Integer, File[]>(first, firstFiles);
		ImmutablePair<Integer, File[]> arcTwo = new ImmutablePair<Integer, File[]>(second, secondFiles);

		compSave.compare(arcOne, arcTwo);
	}

}
