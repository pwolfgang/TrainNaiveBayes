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
package edu.temple.cla.papolicy.wolfgang.texttool.trainnaivebayes;

import edu.temple.cla.papolicy.wolfgang.texttools.util.CommonFrontEnd;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Util;
import edu.temple.cla.papolicy.wolfgang.texttools.util.Vocabulary;
import edu.temple.cla.papolicy.wolfgang.texttools.util.WordCounter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Program to train a Naive Bayes Classifier.
 * @author Paul Wolfgang
 */
public class Main implements Callable<Void> {
    @CommandLine.Option(names = "--output_vocab", description = "File where vocabulary is written")
    private String outputVocab;
    
    @CommandLine.Option(names = "--model", description = "Directory where model files are written")
    private String modelOutput = "SVM_Model_Dir";

    private final String[] args;
    
    public Main(String [] args) {
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
            List<String> ids = new ArrayList<>();
            List<String> ref = new ArrayList<>();
            List<WordCounter> counts = new ArrayList<>();
            Map<String, WordCounter> trainingSets = new TreeMap<>();
            Vocabulary vocabulary = new Vocabulary();
            Map<String, Integer> docsInTrainingSet = new TreeMap<>();
            Map<String, Double> prior = new TreeMap<>();
            Map<String, Map<String, Double>> condProb = new TreeMap<>();
            CommonFrontEnd commonFrontEnd = new CommonFrontEnd();
            CommandLine commandLine = new CommandLine(commonFrontEnd);
            commandLine.setUnmatchedArgumentsAllowed(true);
            commandLine.parse(args);
            commonFrontEnd.loadData(ids, ref, vocabulary, counts);
            if (outputVocab != null) {
                vocabulary.writeVocabulary(outputVocab);
            }
            File modelParent = new File(modelOutput);
            Util.delDir(modelParent);
            modelParent.mkdirs();
            File vocabFile = new File(modelParent, "vocab.bin");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(vocabFile))) {
                oos.writeObject(vocabulary);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            buildTrainingSets(ref, trainingSets, counts, docsInTrainingSet);
            Set<String> cats = trainingSets.keySet();
            computePrior(cats, ref, docsInTrainingSet, prior);
            computeConditionalProbs(vocabulary, cats, trainingSets, condProb);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void computeConditionalProbs(Vocabulary vocabulary, 
            Set<String> cats, Map<String, WordCounter> trainingSets, 
            Map<String, Map<String, Double>> condProb) {
        vocabulary.getWordList().forEach(word -> {
            int countOfWord = vocabulary.getWordCount(word);
            Map<String, Double> probsForCat = condProb.get(word);
            if (probsForCat == null) {
                probsForCat = new TreeMap<>();
                condProb.put(word, probsForCat);
            }
            for (String cat : cats) {
                WordCounter countsForCat = trainingSets.get(cat);
                int countOfWordInCat = countsForCat.getCount(word);
                double condProbOfWordInCat =
                        (double)(countOfWordInCat + 1)/(countOfWord + 1);
                probsForCat.put(cat, condProbOfWordInCat);
            }
        });
    }

    public void computePrior(Set<String> cats,
            List<String> ref, Map<String, Integer> docsInTrainingSet, 
            Map<String, Double> prior) {
        int docCount = ref.size();
        cats.forEach(cat -> {
            double priorForCat = (double)docsInTrainingSet.get(cat)/docCount;
            prior.put(cat, priorForCat);
        });
    }

    public void buildTrainingSets(List<String> ref, 
            Map<String, WordCounter> trainingSets, List<WordCounter> counts, 
            Map<String, Integer> docsInTrainingSet) {
        for (int i = 0; i < ref.size(); i++) {
            String cat = ref.get(i);
            WordCounter countsForCat = trainingSets.get(cat);
            if (countsForCat == null) {
                countsForCat = new WordCounter();
                trainingSets.put(cat, countsForCat);
            }
            countsForCat.updateCounts(counts.get(i));
            int numDocs = docsInTrainingSet.getOrDefault(cat, 0);
            numDocs++;
            docsInTrainingSet.put(cat,numDocs);
        }
    }
    
    
}

