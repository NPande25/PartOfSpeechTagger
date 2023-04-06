import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nikhilpande and nathanmcallister
 */
public class MarkovReader {
    private static String tags;
    private static String words;
    private static List<String> tagList = new ArrayList<String>(); // list of all tags so we can match them to words later on

    public MarkovReader(String tags, String words) {
        MarkovReader.tags = tags;
        MarkovReader.words = words;
    }
    /**
     * read the file with the tags and compute the training table with probabilities for each transition
     * from tag to tag
     *
     * @return a table (map of maps) with the ln of the probabilities for each transition
     * @throws IOException
     */
    public static Map<String, HashMap<String, Double>> transReader() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(tags));

        HashMap<String, Double> startTags = new HashMap<String, Double>(); // holds starter tags so we know
        Map<String, HashMap<String, Double>> transProb = new HashMap<String, HashMap<String, Double>>(); // table to hold transition probabilities

        String line;
        while ((line = in.readLine()) != null) {
            String[] tagLine = line.split(" ");
            for (int i = 0; i < tagLine.length; i++) {
                // declare current tag
                String tag = tagLine[i];
                tagList.add(tag); // add current tag to tagList

                if (i == 0) { // if this is the first tag in the line, add it to start tag
                    if (startTags.containsKey(tag)) {
                        startTags.put(tag, startTags.get(tag) + 1);

                    } else {
                        startTags.put(tag, (double) 1);
                    }

                    // increment the total value for the start map, creating a new key if this is the first word
                    if (startTags.containsKey("TOT")) { // if we already have a total value
                        startTags.put("TOT", startTags.get("TOT") + 1);
                    } else { // if it doesn't yet contain a total column
                        startTags.put("TOT", (double) 1);
                    }
                }

                if (i < tagLine.length - 1) { // make sure it's not the last tag, because there's no transition after that
                    String next = tagLine[i + 1]; // declare the next tag

                    if (transProb.containsKey(tag)) { // if this tag is in transitionsProb
                        if (!transProb.get(tag).containsKey(next)) { // if we've never seen this transition before
                            transProb.get(tag).put(next, (double) 1);
                        } else { // next tag is in current tag's submap
                            transProb.get(tag).put(next, transProb.get(tag).get(next) + 1);
                        }



                    } else { // transitionsProb doesn't have this tag yet
                        transProb.put(tag, new HashMap<String, Double>());
                        transProb.get(tag).put(next, (double) 1);
                    }

                    // since we just added 1 to the row, increment the total for the current tag
                    if (transProb.get(tag).containsKey("TOT")) {
                        transProb.get(tag).put("TOT", transProb.get(tag).get("TOT") + 1);
                    } else { // if no total yet (first time we're seeing this tag)
                        transProb.get(tag).put("TOT", (double) 1);
                    }
                }
            }
        }
        transProb.put("#", startTags); // add the start tags into the table with key = "#"


        // now, go through table, divide each by total, and change all to log probabilities
        for (String tag : transProb.keySet()) { // for all the tags (rows)
            for (String next : transProb.get(tag).keySet()) { // take each next tag (value)
                double total = transProb.get(tag).get("TOT");
                if (!next.equals("TOT")) { // overwrite value with ln of its probability
                    transProb.get(tag).put(next, Math.log(transProb.get(tag).get(next) / total));
                }
            }
        }

        return transProb;
    }

    /**
     * read the file with the words and compute a table with the ln of the probability of observing a
     * certain word in a certain part of speech
     *
     * @return table (map of maps) with the ln of the probability for each word in each part of speech
     * @throws IOException
     */
    public static Map<String, HashMap<String, Double>> obsReader() throws IOException {
        Map<String, HashMap<String, Double>> obsProb = new HashMap<String, HashMap<String, Double>>();

        BufferedReader wordIn = new BufferedReader(new FileReader(words));
        String line;
        int num = 0; // need counter so we can draw each word's corresponding tag from tagList

        while ((line = wordIn.readLine()) != null) {
            String[] wordLine = line.split(" ");

            for (int i = 0; i < wordLine.length; i++) {
                String word = wordLine[i].toLowerCase(); // current word
                String tag = tagList.get(num); // tag associated with current word

                if (!obsProb.containsKey(tag)) { // if we've never seen this tag before
                    obsProb.put(tag, new HashMap<String, Double>());
                    obsProb.get(tag).put(word, (double) 1);
                }
                else { // if word's tag is in the table
                    if (obsProb.get(tag).containsKey(word)) {
                        obsProb.get(tag).put(word, obsProb.get(tag).get(word) + 1);
                    }
                    else { // if the tag hasn't seen this word yet
                        obsProb.get(tag).put(word, (double) 1);
                    }
                }

                // since we just added 1 to the row, increment the total for the current tag
                if (obsProb.get(tag).containsKey("TOT")) {
                    obsProb.get(tag).put("TOT", obsProb.get(tag).get("TOT") + 1);
                } else { // if no total yet (first time we're seeing this tag)
                    obsProb.get(tag).put("TOT", (double) 1);
                }

                num++; // increment counter as we move to next word
            }
        }

        // now, go through table, divide each by total, and change all to log probabilities
        for (String tag : obsProb.keySet()) { // for all the tags (rows)
            for (String word : obsProb.get(tag).keySet()) { // take each next tag (value)
                double total = obsProb.get(tag).get("TOT");
                if (!word.equals("TOT")) { // overwrite the value with the ln of its probability
                    obsProb.get(tag).put(word, Math.log(obsProb.get(tag).get(word) / total));
                }
            }
        }
        return obsProb;
    }

    // testing to see the tables
    public static void main(String[] args) throws IOException {
        MarkovReader mReader = new MarkovReader("inputs/testtags.txt","inputs/testwords.txt");
        System.out.println(mReader.transReader());
        System.out.println(mReader.obsReader());
    }
}
