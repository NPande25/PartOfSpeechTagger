import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author nikhilpande
 * @author nathanmcallister
 */

public class Sudi {
    static Map<String,HashMap<String,Double>> observationScore = new HashMap<>(); // map of maps to hold the observation score table
    static Map<String,HashMap<String,Double>> transitionScore = new HashMap<>(); // map of maps to hold the transition score table
    static String currentData;
    static int pen = 50;

    public Sudi(Map<String,HashMap<String,Double>> observationScore, Map<String,HashMap<String,Double>> transitionScore){
        Sudi.observationScore = observationScore;
        Sudi.transitionScore = transitionScore;
    }
    public Sudi(String tags, String words) {
        try {
            MarkovReader mReader = new MarkovReader(tags, words);
            Sudi.transitionScore = MarkovReader.transReader();
            Sudi.observationScore = MarkovReader.obsReader();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * uses the viterbi algorithm to calculate the most likely path of tags for a line
     *
     * @param line sentence in string form
     * @return returns a list of the tags, in order
     */
    public static List<String> decodeLine(String line) {

        String[] words = line.split("\\s+"); // split line into words

        // create array to keep track of current states
        ArrayList<String> currStates = new ArrayList<>();
        currStates.add("#"); // add start state
        // create map to keep tack of current scores
        HashMap<String,Double> currScores = new HashMap<>();
        currScores.put("#", (double)0); // add initial score = 0

        // nested map to keep track of paths
        HashMap<Integer,HashMap<String,String>> paths = new HashMap<>();

        for (int i = 0; i < words.length; i++) { // for each word in the input line

            String word = words[i].toLowerCase();

            HashMap<String,String> transitions = new HashMap<>();

            ArrayList<String> nextStates = new ArrayList<>();
            HashMap<String,Double> nextScores = new HashMap<>();
            for (String currState : currStates) {   // for each possible current state
                if (transitionScore.containsKey(currState)) {
                    for (String nextState : transitionScore.get(currState).keySet()) { // for each possible next state
                        if (!nextState.equals("TOT")) { // exclude total from the checked states

                            if (!nextStates.contains(nextState))
                                nextStates.add(nextState); // add next state to next states

                            double nextScore;
                            if (!observationScore.get(nextState).containsKey(word)) { // update score with subtracted unseen penalty
                                nextScore = currScores.get(currState) + transitionScore.get(currState).get(nextState) - pen;
                            } else { // else update score with observation score
                                nextScore = currScores.get(currState) + transitionScore.get(currState).get(nextState) + observationScore.get(nextState).get(word);
                            }

                            // if it is the largest score for that state, override the old score
                            if (!nextScores.containsKey(nextState) || (nextScore > nextScores.get(nextState))) {
                                nextScores.put(nextState, nextScore);
                                transitions.put(nextState, currState); // remember the transitions
                            }
                        }
                    }
                }
            }
            // keep track of paths
            paths.put(i, transitions);

            // move to next state
            currStates = nextStates;
            currScores = nextScores;
        }

        // set the max score and state vars to arbitrary values...
        Double max = currScores.get(currStates.get(0));
        String maxState = currStates.get(0);

        // iterate through the final states and find the one with the largest score
        for (Map.Entry<String,Double> entry: currScores.entrySet()) {
            if (entry.getValue() > max) {
                maxState = entry.getKey();
                max = entry.getValue();
            }
        }

        // start from the state we just found
        String currState = maxState;

        // build path
        ArrayList<String> path = new ArrayList<>();
        path.add(currState);

        for (int i = words.length - 1; i >= 1; i--) {
            currState = paths.get(i).get(currState);
            path.add(0, currState);
        }

        return path;
    }

    /**
     * Uses viterbi to decode a console inputted sentence, model trained via brown training
     */

    public static void quit() {
        System.exit(0); // quit
    }

    /**
     * Assembles a list of all the tags in the file so that accuracy method can use them to check
     *
     * @param sentenceFileName the name of the file with the sentences to check and determine tags for
     * @return a list of all the tags in that file
     * @throws Exception if file cannot be found/read
     */
    public static List<String> assembleTags(String sentenceFileName) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(sentenceFileName));
        String line;
        List<String> allTags = new ArrayList<String>(); // list to hold all the tags for accuracy checking

        while ((line = in.readLine()) != null) {
            allTags.addAll(decodeLine(line));
        }
        in.close();
        return allTags;
    }

    /**
     * calculates the accuracy of the viterbi model on a set of testing sentences and tags
     *
     * @param sentenceFile filename for test sentences
     * @param tagFile filename for test tags
     * @return percent accuracy of the viterbi model
     * @throws Exception if cannot read/find files
     */
    public static double accuracy(String sentenceFile, String tagFile) throws Exception {
        List<String> tags = assembleTags(sentenceFile);
        BufferedReader tagRead = new BufferedReader(new FileReader(tagFile));
        String line;
        double counter = 0;
        double wrong = 0;

        while ((line = tagRead.readLine()) != null) {
            String[] tagLine = line.split(" ");
            for (String s : tagLine) { // loop through tags in line
                if (!s.equals(tags.get((int) counter))) wrong++; // increment 'wrong' if they aren't equivalent to model
                counter++;
            }
        }

        System.out.println("Wrong: " + (int) wrong);
        System.out.println("Right: " + (int) (counter - wrong));
        System.out.println("Total: " + (int) counter);
        return 100 * (1 - (wrong / counter)); // return percent accuracy
    }

    public static void loadPdTest() {
        HashMap<String,HashMap<String,Double>> obsScore = new HashMap<>();
        HashMap<String,HashMap<String,Double>> transScore = new HashMap<>();

        // filling observation map
        obsScore.put("NP", new HashMap<>(){{
            put("chase", (double)10);
        }});
        obsScore.put("CNJ", new HashMap<>(){{
            put("and", (double)10);
        }});
        obsScore.put("N", new HashMap<>(){{
            put("cat", (double)4);
            put("dog", (double)4);
            put("watch", (double)2);
        }});
        obsScore.put("V", new HashMap<>(){{
            put("get", (double)1);
            put("chase", (double)3);
            put("watch", (double)6);
        }});

        // filling transition map
        transScore.put("#", new HashMap<>(){{
            put("NP", (double)3);
            put("N", (double)7);
        }});
        transScore.put("NP", new HashMap<>(){{
            put("CNJ", (double)2);
            put("V", (double)8);
        }});
        transScore.put("CNJ", new HashMap<>(){{
            put("NP", (double)2);
            put("N", (double)4);
            put("V", (double)4);
        }});
        transScore.put("N", new HashMap<>(){{
            put("V", (double)8);
            put("CNJ", (double)2);
        }});
        transScore.put("V", new HashMap<>(){{
            put("NP", (double)4);
            put("N", (double)4);
            put("CNJ", (double)2);
        }});

        observationScore = obsScore;
        transitionScore = transScore;

        currentData = "pd-7 example data";
        System.out.println("pd-7 example loaded");
    }

    //
    public static void loadBrownCorpus() {
        try {
            MarkovReader brownC = new MarkovReader("inputs/brown-train-tags.txt", "inputs/brown-train-sentences.txt");
            transitionScore = brownC.transReader();
            observationScore = brownC.obsReader();

            currentData = "brown corpus data";
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public static void solicitInput() throws Exception {
        System.out.println("data currently loaded: " + currentData);
        System.out.println("choose an option:");
        System.out.println("[i]nput line to be tagged");
        System.out.println("load [p]d-7 example");
        System.out.println("test [a]ccuracy of brown");
        System.out.println("[q]uit");

        Scanner in = new Scanner(System.in);
        String input = in.nextLine();

        switch (input){
            case "p" -> {
                loadPdTest();
                solicitInput();
            }

            case "a" -> {
                try {
                    System.out.println("Brown Testing Data Results:");
                    System.out.println("Accuracy (%): " + accuracy("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt") + " trained on " + currentData + "\n");
                    solicitInput();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            case "i" -> {
                System.out.println("input a sentence: ");

                input = in.nextLine();
                String[] wordList = input.split(" ");

                List<String> tags = decodeLine(input); // what the model predicts

                String out = "";
                for (int i = 0; i < tags.size(); i++) {
                    out += (wordList[i] + "/" + tags.get(i) + " "); // concatenate into word/tag sentence
                }
                System.out.println(out + "\n");
                solicitInput();
            }
            case "q" -> quit();
            default -> {
                System.out.println("invalid input, try again");
                solicitInput();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        loadBrownCorpus();
        solicitInput();
    }
}