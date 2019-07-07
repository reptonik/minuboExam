import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SecondTaskSolution {

    /**
     * This task deals with string manipulation using the example of IPv6
     * address representation.
     *
     * The task is to implement the method getSimplifiedAddress in the class
     * IPv6Address. To test your code run the main method.
     *
     * Format of an IPv6 address: 8 hexadecimal numbers separated by colons.
     *
     * For convenience, an IPv6 address may be simplified to shorter notations
     * by application of the following rules:
     *
     * 1. Leading zeros of each hexadecimal number are suppressed.
     * For example, 2001:0db8::0001 is rendered as 2001:db8::1.
     *
     * 2. Two or more consecutive groups containing zeros only may be replaced
     * with a single empty group, using two consecutive colons (::).
     * For example, 2001:db8:0:0:0:0:2:1 is shortened to 2001:db8::2:1
     * (but 2001:db8:0:1:1:1:1:1 is rendered as 2001:db8:0:1:1:1:1:1).
     *
     * 3. Representations are shortened as much as possible.
     * The longest sequence of consecutive all-zero fields is replaced with
     * double-colon.
     * If there are multiple longest runs of all-zero fields, then it is the
     * leftmost that is compressed.
     * E.g., 2001:db8:0:0:1:0:0:1 is rendered as 2001:db8::1:0:0:1 rather than
     * as 2001:db8:0:0:1::1.
     */

    // Test examples - a map of full IPv6 addresses and their simplified representation
    public static final List<Entry<String, String>> IPV6_ADDRESSES = new ArrayList<Entry<String, String>>() {
        private static final long serialVersionUID = 7170906660651650870L;
        {
            add(new SimpleEntry<>("1111:2222:3333:4444:5555:0ab9:0e0f:0010", "1111:2222:3333:4444:5555:ab9:e0f:10")); // rule 1
            add(new SimpleEntry<>("1111:2222:3333:4444:0000:0006:0070:0800", "1111:2222:3333:4444:0:6:70:800")); // rule 1 - checking that a single group of all zeroes is replaced by 0
            add(new SimpleEntry<>("1111:2222:0000:0000:0000:0000:7777:8888", "1111:2222::7777:8888")); // rule 1+2
            add(new SimpleEntry<>("1111:2222:3330:0000:0000:0666:7777:8888", "1111:2222:3330::666:7777:8888")); // rule 1+2
            add(new SimpleEntry<>("1111:2222:0000:0001:0001:0001:0001:0001", "1111:2222:0:1:1:1:1:1")); // rule 1+2 - checking that consecutive groups of 1 are not replaced with ::
            add(new SimpleEntry<>("1111:2222:0000:0000:5555:0000:0000:0000", "1111:2222:0:0:5555::")); // rule 3
            add(new SimpleEntry<>("1111:2222:0000:0000:5555:0000:0000:8888", "1111:2222::5555:0:0:8888")); // rule 3
            add(new SimpleEntry<>("0000:0000:3333:4444:5555:6666:7777:8888", "::3333:4444:5555:6666:7777:8888")); // rule 3
        }
    };

    //Validation section - not asked but just in case.
    private static Pattern VALID_IPV6_PATTERN = null;
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try{
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        }catch (PatternSyntaxException e){
            System.err.println("Unable to compile validation pattern: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        for (Entry<String, String> addressEntry : IPV6_ADDRESSES) {
            final IPv6Address address = new IPv6Address(addressEntry.getKey());
            final String expectedSimplified = addressEntry.getValue();
            final String actualSimplified = address.getSimplifiedRepresentation();
            if (expectedSimplified.equals(actualSimplified)) {
                System.out.println("Correct! Original: [" + address + "] Expected: [" + expectedSimplified + "] Actual: [" + actualSimplified + "] ");
            } else {
                System.err.println("INCORRECT! Original: [" + address + "] Expected: [" + expectedSimplified + "] Actual: [" + actualSimplified + "] ");
            }
        }
    }

    public static class IPv6Address {

        private final String address;

        public IPv6Address(final String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return address;
        }

        public String getSimplifiedRepresentation() {

            // Rules for simplifying IPv6 addresses:
            // 1. Leading zeros in each 16-bit field are suppressed.
            //    For example, 2001:0db8::0001 is rendered as 2001:db8::1.
            // 2. Two or more consecutive groups containing zeros only may be replaced with a single empty group, using two consecutive colons (::).
            //    For example, 2001:db8:0:0:0:0:2:1 is shortened to 2001:db8::2:1
            //    (but 2001:db8:0:1:1:1:1:1 is rendered as 2001:db8:0:1:1:1:1:1).
            // 3. Representations are shortened as much as possible.
            //    The longest sequence of consecutive all-zero fields is replaced with double-colon.
            //    If there are multiple longest runs of all-zero fields, then it is the leftmost that is compressed.
            //    E.g., 2001:db8:0:0:1:0:0:1 is rendered as 2001:db8::1:0:0:1 rather than as 2001:db8:0:0:1::1.


            //validation (addition not required for this task)
            validateIpv6();

            //remove leading 0 and trim sequence of 0000... to single 0 for each segment
            List<String> segments = Arrays
                    .stream(address.split(":"))
                    .map(segment ->{
                        StringBuilder sb = new StringBuilder(segment);
                        while (sb.length() > 1 && sb.charAt(0) == '0') {
                            sb.deleteCharAt(0);
                        }
                        return sb.toString();
                    })
                    .collect(Collectors.toList());

            boolean zero = false;
            int max = 0;
            int startIndex = 0;
            int endIndex = 0;

            //Calculate longest sequence of consecutive all-zero fields
            for (int i = 0; i < segments.size()-1; i++){
                if(segments.get(i).equals("0") && segments.get(i + 1).equals("0")){
                    int currentStart = i;
                    int currentCount = 0;
                    zero = true;
                    while (i < segments.size() && segments.get(i).equals("0")){
                        i ++;
                        currentCount ++;
                    }
                    if(currentCount > max){
                        max = currentCount;
                        startIndex = currentStart;
                        endIndex = i - 1;
                    }
                }
            }
            if(max <= 1){
                zero = false;
            }


            boolean finalZero = zero;
            int finalStart = startIndex;
            int finalEnd = endIndex;

            return IntStream
                    .range(0, segments.size())
                    .mapToObj(i -> {
                        String simplifiedSegment;
                        if(finalZero){
                            if(i >= finalStart && i < finalEnd){
                                //Special case for longest sequence of consecutive all-zero at the beginning of IP
                                if(finalStart == 0 && i == 0){
                                    simplifiedSegment =  "";
                                }
                                //Mark for remove
                                else {
                                    simplifiedSegment = "****";
                                }
                            }
                            else if(i == finalEnd){
                                //Special case for longest sequence of consecutive all-zero at the end of IP
                                if(finalEnd == segments.size() - 1) {
                                    simplifiedSegment = ":";
                                //longest sequence of consecutive all-zero in the middle no need to print char
                                //:: will be printed by joining delimiter.
                                }else {
                                    simplifiedSegment = "";
                                }
                            }else{
                                simplifiedSegment = segments.get(i);
                            }
                        }else{
                            simplifiedSegment = segments.get(i);
                        }
                        return simplifiedSegment;
                    })
                    .filter(i -> !i.equals("****"))
                    .collect(Collectors.joining(":"));
        }

        public String getAddress() {
            return address;
        }

        //Done with regular expression as addition to the task. Can be done also as simple validation
        // to count Segments (must be 8) for example
        private void validateIpv6(){
            if(!VALID_IPV6_PATTERN.matcher(address).matches())
                throw new RuntimeException("Address not in valid IPV6 format");
        }
    }
}