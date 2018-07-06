/* 
 * Copyright (c) 2018, Temple University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * All advertising materials features or use of this software must display 
 *   the following  acknowledgement
 *   This product includes software developed by Temple University
 * * Neither the name of the copyright holder nor the names of its 
 *   contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.temple.cla.papolicy.wolfgang.texttools.trainnaivebayes;

import edu.temple.cla.papolicy.wolfgang.texttools.util.CommonFrontEnd;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Util;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Vocabulary;
import edu.temple.cla.papolicy.wolfgang.texttools.util.WordCounter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Program to train a Naive Bayes Classifier. This is the Trainer for the Naive
 * Bayes classifier written from scratch that attempts to produce comparable
 * results to <a href="http://mallet.cs.umass.edu/">MALLET</a>. In testing with
 * the Bills_Data using the 2001-2013 sessions as training set and 2015 session
 * as test set, this classifier achieves 72% accuracy while MALLET achieved 75%.
 * It agrees with the MALLET classifier 82%. Of the 18% disagreements 4% where
 * the correct result.
 *
 * The code is based on
 * <a href="http://blog.datumbox.com/machine-learning-tutorial-the-naive-bayes-text-classifier/">
 * Machine Learning Tutorial: The Native Bayes Text Classifier </a> and
 * examination of the MALLET code.
 *
 * @author Paul Wolfgang
 */
public class Main implements Callable<Void> {

    @CommandLine.Option(names = "--output_vocab", description = "File where vocabulary is written")
    private String outputVocab;

    @CommandLine.Option(names = "--model", description = "Directory where model files are written")
    private String modelOutput = "Model_Dir";

    private final String[] args;

    public Main(String[] args) {
        this.args = args;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main(args);
        CommandLine commandLine = new CommandLine(main);
        commandLine.setUnmatchedArgumentsAllowed(true).parse(args);
        try {
            main.call();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Void call() throws Exception {
        try {
            List<Map<String, Object>> cases = new ArrayList<>();
            CommonFrontEnd commonFrontEnd = new CommonFrontEnd();
            CommandLine commandLine = new CommandLine(commonFrontEnd);
            commandLine.setUnmatchedArgumentsAllowed(true);
            commandLine.parse(args);
            Vocabulary vocabulary = commonFrontEnd.loadData(cases);
            if (outputVocab != null) {
                vocabulary.writeVocabulary(outputVocab);
            }
            File modelParent = new File(modelOutput);
            Util.delDir(modelParent);
            modelParent.mkdirs();
            Util.outputFile(modelParent, "vocab.bin", vocabulary);
            Map<String, WordCounter> trainingSets = buildTrainingSets(cases);
            Map<String, Double> prior = 
                    computePrior(cases.size(), trainingSets);
            Util.outputFile(modelParent, "prior.bin", prior);
            Map<String, Map<String, Double>> condProb = 
                    computeConditionalProbs(vocabulary, trainingSets);
            Util.outputFile(modelParent, "condProp.bin", condProb);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Compute the probability of each word in the vocabulary given that it 
     * occurred in a document of a given category.
     * 
     * @param vocabulary The total vocabulary of all training sets.
     * @param trainingSets The training sets grouped by category.
     * @return condProb A map from words to a map from categories to probabilities.
     */
    public Map<String, Map<String, Double>> computeConditionalProbs(Vocabulary vocabulary,
            Map<String, WordCounter> trainingSets) {
        Map<String, Map<String, Double>> condProb = new HashMap<>();
        vocabulary.getWordList().forEach(word -> {
            int countOfWord = vocabulary.getWordCount(word);
            Map<String, Double> probsForCat = condProb.get(word);
            if (probsForCat == null) {
                probsForCat = new TreeMap<>();
                condProb.put(word, probsForCat);
            }
            for (String cat : trainingSets.keySet()) {
                WordCounter countsForCat = trainingSets.get(cat);
                double probOfWordGivenCat = countsForCat.getLaplaseProb(word)
                        .orElse(vocabulary.getLaplaseProb(word));
                probsForCat.put(cat, probOfWordGivenCat);
            }
        });
        return condProb;
    }

    /**
     * Compute the probability that a document has a given category.
     *  
     * @param docCount The total count of documents in the training set
     * @param trainingSets The training sets
     * @return 
     */
    public Map<String, Double> computePrior(int docCount, Map<String, WordCounter> trainingSets) {
        Map<String, Double> prior = new HashMap<>();
        trainingSets.forEach((cat, count) -> {
            double priorProb = (double)count.getNumDocs() / (double)docCount;
            prior.put(cat, priorProb);
        });
        return prior;
    }

    /**
     * Method to build the training sets. This method scans the input training
     * cases and groups them by category. The word counts for all cases of a
     * given category are merged into a single word count.
     * @param cases The training cases
     * @return The training sets.
     */
    public Map<String, WordCounter> buildTrainingSets(List<Map<String, Object>> cases) {
        Map<String, WordCounter> trainingSets = new HashMap<>();
        cases.forEach((trainingCase) -> {
            String cat = trainingCase.get("theCode").toString();
            WordCounter countsForCat = trainingSets.get(cat);
            if (countsForCat == null) {
                countsForCat = new WordCounter();
                trainingSets.put(cat, countsForCat);
            }
            countsForCat.updateCounts((WordCounter)trainingCase.get("counts"));
        });
        return trainingSets;
    }

}
