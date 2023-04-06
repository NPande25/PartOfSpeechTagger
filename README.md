# Nikhil Pande
## CS10 23W - Problem Set 5
### Hidden Markov Models and the Viterbi Algorithm
In Problem Set 5, we build a machine learning-based bot named Sudi, who labels each word in a sentence with its part-of-speech, based on its neighbors and the word itself. The program reads large training sets of sentences and tags, then uses a map of maps to build an extensive hidden markov model. Using the Viterbi algorithm, which calculates scores based on obeservation and transition probability to decipher the most likely traversal of the automaton, the program outputs its predicted part-of-speech for every word based on the calculated Viterbi scores. The program can tag an entire testing file of sentences using Viterbi, and it can calculate its accuracy based on a corresponding file of correct tags.
![image](https://user-images.githubusercontent.com/103916802/230431715-a66d0638-fa36-46c9-a519-f7d519969c44.png)
