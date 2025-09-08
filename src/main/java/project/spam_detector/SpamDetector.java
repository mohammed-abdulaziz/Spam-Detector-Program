
package project.spam_detector;

import java.io.*;
import java.util.*;

public class SpamDetector {
    private Map<String, Integer> trainHamFreq;
    private Map<String, Integer> trainSpamFreq;
    private Map<String, Double> hamWordProb;
    private Map<String, Double> spamWordProb;

    public SpamDetector() {
        trainHamFreq = new TreeMap<>();
        trainSpamFreq = new TreeMap<>();
    }

    public boolean train(File dataFolder) {

        trainHamFreq.clear();
        trainSpamFreq.clear();

        File trainFolder = new File(dataFolder, "train");
        if (!trainFolder.exists() || !trainFolder.isDirectory()) {
            return false;
        }

        File hamFolder = new File(trainFolder, "ham");
        File[] hamFiles = hamFolder.listFiles((dir, name) -> !name.equals("cmds"));
        if (hamFiles == null || hamFiles.length == 0) {
            return false;
        }
        trainFolders(hamFiles, trainHamFreq);

        File hamFolder2 = new File(trainFolder, "ham2");
        File[] hamFiles2 = hamFolder2.listFiles((dir, name) -> !name.equals("cmds"));
        if (hamFiles2 != null) {
            trainFolders(hamFiles2, trainHamFreq);
        }

        File spamFolder = new File(trainFolder, "spam");
        File[] spamFiles = spamFolder.listFiles((dir, name) -> !name.equals("cmds"));
        if (spamFiles == null || spamFiles.length == 0) {
            return false;
        }
        trainFolders(spamFiles, trainSpamFreq);

        int hamFileCount = (hamFiles != null ? hamFiles.length : 0) + (hamFiles2 != null ? hamFiles2.length : 0);
        int spamFileCount = spamFiles != null ? spamFiles.length : 0;


        hamWordProb = wordProbability(trainHamFreq, hamFileCount);
        spamWordProb = wordProbability(trainSpamFreq, spamFileCount);

        return true;
    }

    public List<TestFile> classifyEmails(File dataFolder) {
        List<TestFile> results = new ArrayList<>();

        File testFolder = new File(dataFolder, "test");
        if (!testFolder.exists() || !testFolder.isDirectory()) {
            return results;
        }

        File hamTestFolder = new File(testFolder, "ham");
        if (hamTestFolder.exists() && hamTestFolder.isDirectory()) {
            File[] hamFiles = hamTestFolder.listFiles((dir, name) -> !name.equals("cmds"));
            if (hamFiles != null) {
                for (File file : hamFiles) {
                    if (file.isFile()) {
                        Map<String, Double> fileProb = fileSpamWordProb(file, hamWordProb, spamWordProb);
                        double spamProb = spamFileProb(file, fileProb);
                        results.add(new TestFile(file.getName(), spamProb, "ham"));
                    }
                }
            }
        }

        File spamTestFolder = new File(testFolder, "spam");
        if (spamTestFolder.exists() && spamTestFolder.isDirectory()) {
            File[] spamFiles = spamTestFolder.listFiles((dir, name) -> !name.equals("cmds"));
            if (spamFiles != null) {
                for (File file : spamFiles) {
                    if (file.isFile()) {
                        Map<String, Double> fileProbs = fileSpamWordProb(file, hamWordProb, spamWordProb);
                        double spamProb = spamFileProb(file, fileProbs);
                        results.add(new TestFile(file.getName(), spamProb, "spam"));
                    }
                }
            }
        }

        return results;
    }

    public void trainFolders(File[] files, Map<String, Integer> freqMap){
        for (File file : files) {
            String path = file.getAbsolutePath();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                Set<String> uniqueWords = new HashSet<>();
                while ((line = br.readLine()) != null) {
                    String[] words = line.toLowerCase().split("[^a-zA-Z']+");
                    uniqueWords.addAll(Arrays.asList(words));
                }
                for (String word : uniqueWords) {
                    freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Double> wordProbability(Map<String, Integer> freqMap, int fileCount) {
        Map<String, Double> probMap = new TreeMap<>();
        for (String word : freqMap.keySet()){
            probMap.put(word, (double) freqMap.get(word) / fileCount);
        }
        return probMap;
    }

    public Map<String, Double> fileSpamWordProb(File file, Map<String, Double> hamProbMap, Map<String, Double> spamProbMap) {
        Map<String, Double> probMap = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            Set<String> uniqueWords = new HashSet<>();
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^a-zA-Z']+");
                uniqueWords.addAll(Arrays.asList(words));
            }
            for (String word : uniqueWords) {
                double spamCount = spamProbMap.getOrDefault(word, 0.0);
                double hamCount = hamProbMap.getOrDefault(word, 0.0);
                probMap.put(word, (double) (spamCount + 1)/ (spamCount + hamCount + 2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return probMap;
    }

    public double spamFileProb(File file, Map<String, Double> fileSpamWordProb){
        double prob = 0.0;
        double eta = 0.0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            Set<String> uniqueWords = new HashSet<>();
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^a-zA-Z']+");
                uniqueWords.addAll(Arrays.asList(words));
            }
            for (String word : uniqueWords) {
                double spamProb = fileSpamWordProb.getOrDefault(word, 0.4);
                spamProb = Math.min(0.999, Math.max(spamProb, 0.001));
                eta += (Math.log(1 - spamProb) - Math.log(spamProb));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        prob = 1 / (1 + Math.pow(Math.E, eta));
        return prob;
    }
}
