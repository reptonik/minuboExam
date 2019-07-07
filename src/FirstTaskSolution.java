import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Or Kucher
 *
 * Build sentence from arrays of words.
 *
 * Each row contain at position 0 command describe how words need to be treated.
 */
class FirstTaskSolution {

    private static final String[][] WORDS = new String[][]{
            new String[]{"reverse", "none", "While"},
            new String[]{"sort", "work", "of", "the"},
            new String[]{"lowerCase", "wE"},
            new String[]{"sort", "is", "very", "do"},
            new String[]{"lowerCase", "iMPoRTaNT,"},
            new String[]{"reverse", "is", "it"},
            new String[]{"sort", "we", "important", "that"},
            new String[]{"lowerCase", "Do"},
            new String[]{"reverse", "of", "deal", "great", "a"},
            new String[]{"lowerCase", "It."}};

    public static void main(String[] args) {
        final String sentence = new SentenceBuilder().buildSentence(WORDS);
        System.out.println(sentence);
        System.out.println("Correct: [" + (sentence.hashCode() == -627956530) + "]");
    }


    public static class SentenceBuilder {

        String buildSentence(String[][] words) {
            return Arrays.stream(words)
                    .map(line -> {
                        Builder builder = BuilderFactory.getBuilder(line[0]);
                        String[] sentence = Arrays.stream(line, 1, line.length)
                                .toArray(String[]::new);
                        return builder.buildSentence(sentence);
                    })
                    .collect(Collectors.joining(" "));
        }
    }

    public enum Action {
        REVERSE("reverse"),
        SORT("sort"),
        LOWER("lowerCase");


        private static class Holder {
            static final Map<String, Action> ACTION_MAP = new HashMap<>();
        }

        final String strAction;

        Action(String strAction) {
            this.strAction = strAction;
            Holder.ACTION_MAP.put(strAction, this);
        }
        //Can be done with stream also but this is more elegant
        static Action getActionByValue(String value) {
            return Holder.ACTION_MAP.get(value);
        }
    }


    /**
     * Factory class for sentence builder
     */
    static class BuilderFactory {

        static Builder getBuilder(String action) {
            Action builderAction = Action.getActionByValue(action);

            Builder builder;

            switch (builderAction) {
                case SORT:
                    builder = new SortBuilder();
                    break;
                case LOWER:
                    builder = new LowerCaseBuilder();
                    break;
                case REVERSE:
                    builder = new ReverseBuilder();
                    break;
                default:
                    throw new IllegalStateException(String.format("Unsupported action %s.", action));
            }
            return builder;
        }
    }

    /**
     * Builder interface
     */
    interface Builder {
        String buildSentence(String[] words);
    }

    /**
     * Reverse Builder implementation, Build sentence from words represented by Array
     * in reverse order.
     */
    private static class ReverseBuilder implements Builder {

        @Override
        public String buildSentence(String[] words) {
            return String.join(" ", invertArray(words));
        }

        private String[] invertArray(String[] array) {
            List<String> list = Arrays.asList(array);
            Collections.reverse(list);
            return list.toArray(array);
        }
    }

    /**
     * Lower Case builder implementation, Put all words to lower case.
     */
    private static class LowerCaseBuilder implements Builder {
        @Override
        public String buildSentence(String[] words) {
            String sentence = String.join(" ", words);
            return sentence.toLowerCase();
        }
    }

    /**
     * Sort builder implementation, Sort all words in native order before.
     */
    private static class SortBuilder implements Builder {
        @Override
        public String buildSentence(String[] words) {
            Arrays.sort(words);
            return String.join(" ", words);
        }
    }
}