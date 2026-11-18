import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Java Assignment 2 - consolidated source code in one file.
 *
 * Compile: javac Assignment2.java
 * Run:     java Assignment2
 */
public class Assignment2 {

    public static void main(String[] args) {
        System.out.println("===== Q1: String, StringBuilder and StringBuffer =====");
        q1StringPerformance();

        System.out.println("\n===== Q2: ArrayList, LinkedList and ArrayDeque =====");
        q2ListComparison();

        System.out.println("\n===== Q3: hashCode and equals =====");
        q3Attendance();

        System.out.println("\n===== Q4: HashMap, TreeMap and LinkedHashMap =====");
        q4PharmacyInventory();

        System.out.println("\n===== Q5: Generic Leaderboard =====");
        q5Leaderboard();

        System.out.println("\n===== Q6: Patient Priority Queue =====");
        q6Triage();

        System.out.println("\n===== Q7: String concatenation redesign =====");
        q7StringBuilderRedesign();
    }

    // ---------------------------------------------------------------------
    // Q1 - Building text using String, StringBuilder and StringBuffer
    // ---------------------------------------------------------------------

    static String usingString(String[] fields) {
        String result = "";
        for (String field : fields) {
            result = result + field + "|";
        }
        return result;
    }

    static String usingStringBuilder(String[] fields) {
        StringBuilder result = new StringBuilder();
        for (String field : fields) {
            result.append(field).append("|");
        }
        return result.toString();
    }

    static String usingStringBuffer(String[] fields) {
        StringBuffer result = new StringBuffer();
        for (String field : fields) {
            result.append(field).append("|");
        }
        return result.toString();
    }

    static void q1StringPerformance() {
        String[] smallFields = createFields(6);
        String[] largeFields = createFields(50);
        String[] additionalFields = createFields(200);

        runStringWorkload("A - Small", smallFields, 1);
        runStringWorkload("B - Large", largeFields, 10000);
        runStringWorkload("C - Additional", additionalFields, 1000);
    }

    private static String[] createFields(int count) {
        String[] fields = new String[count];
        for (int i = 0; i < count; i++) {
            fields[i] = "Field" + (i + 1);
        }
        return fields;
    }

    private static void runStringWorkload(String label, String[] fields, int tickets) {
        long start = System.nanoTime();
        String result = "";
        for (int i = 0; i < tickets; i++) {
            result = usingString(fields);
        }
        long stringTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < tickets; i++) {
            result = usingStringBuilder(fields);
        }
        long builderTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < tickets; i++) {
            result = usingStringBuffer(fields);
        }
        long bufferTime = System.nanoTime() - start;

        System.out.println(label + " (" + tickets + " ticket(s), "
                + fields.length + " fields)");
        System.out.println("String time:        " + stringTime + " ns");
        System.out.println("StringBuilder time: " + builderTime + " ns");
        System.out.println("StringBuffer time:  " + bufferTime + " ns");
        System.out.println("Output length:      " + result.length());
    }

    // ---------------------------------------------------------------------
    // Q2 - ArrayList, LinkedList and ArrayDeque comparison
    // ---------------------------------------------------------------------

    static void q2ListComparison() {
        int size = 10000;

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        Deque<Integer> arrayDeque = new ArrayDeque<>();

        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long arrayListTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        long linkedListTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayDeque.addLast(i);
        }
        long arrayDequeTime = System.nanoTime() - start;

        System.out.println("ArrayList append time: " + arrayListTime + " ns");
        System.out.println("LinkedList append time: " + linkedListTime + " ns");
        System.out.println("ArrayDeque append time: " + arrayDequeTime + " ns");
        System.out.println("ArrayList size: " + arrayList.size());
        System.out.println("LinkedList size: " + linkedList.size());
        System.out.println("ArrayDeque size: " + arrayDeque.size());

        // Demonstrate operations at both ends for an Up Next queue.
        arrayDeque.addFirst(-1);
        arrayDeque.addLast(size);
        System.out.println("ArrayDeque first item: " + arrayDeque.peekFirst());
        System.out.println("ArrayDeque removed from front: " + arrayDeque.removeFirst());
    }

    // ---------------------------------------------------------------------
    // Q3 - Correct hashCode() and equals() contract
    // ---------------------------------------------------------------------

    static class Student {
        private final String regNo;
        private final String name;

        Student(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Student)) {
                return false;
            }
            Student other = (Student) o;
            return Objects.equals(regNo, other.regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }

        @Override
        public String toString() {
            return regNo + " " + name;
        }
    }

    static void q3Attendance() {
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun"));
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun"));

        System.out.println("Unique students: " + present.size());
        for (Student student : present) {
            System.out.println(student);
        }
    }

    // ---------------------------------------------------------------------
    // Q4 - Choosing a Map for a Pharmacy Inventory
    // ---------------------------------------------------------------------

    static void q4PharmacyInventory() {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("MED103", 40);
        stock.put("MED101", 25);
        stock.put("MED102", 60);

        System.out.println("Code order:");
        Map<String, Integer> sorted = new TreeMap<>(stock);
        sorted.forEach((code, quantity) ->
                System.out.println(code + " : " + quantity));

        System.out.println("Insertion order:");
        Map<String, Integer> insertion = new LinkedHashMap<>();
        insertion.put("MED103", 40);
        insertion.put("MED101", 25);
        insertion.put("MED102", 60);
        insertion.forEach((code, quantity) ->
                System.out.println(code + " : " + quantity));

        System.out.println("HashMap lookup MED102: " + stock.get("MED102"));
    }

    // ---------------------------------------------------------------------
    // Q5 - Generic Leaderboard with Comparator
    // ---------------------------------------------------------------------

    static class Team {
        final String name;
        final int points;
        final int goalDiff;

        Team(String name, int points, int goalDiff) {
            this.name = name;
            this.points = points;
            this.goalDiff = goalDiff;
        }

        @Override
        public String toString() {
            return name + "(" + points + "," + goalDiff + ")";
        }
    }

    static class Leaderboard<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;

        Leaderboard(Comparator<? super T> order) {
            this.order = order;
        }

        void add(T item) {
            items.add(item);
        }

        List<T> ranking() {
            List<T> sorted = new ArrayList<>(items);
            sorted.sort(order);
            return sorted;
        }
    }

    static void q5Leaderboard() {
        Comparator<Team> byRank =
                Comparator.comparingInt((Team t) -> t.points).reversed()
                        .thenComparing(
                                Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                        .thenComparing(t -> t.name);

        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));

        System.out.println(board.ranking());
    }

    // ---------------------------------------------------------------------
    // Q6 - Priority, Order and Fairness in a Queue
    // ---------------------------------------------------------------------

    static class Patient {
        final String name;
        final int severity;
        final long arrival;

        Patient(String name, int severity, long arrival) {
            this.name = name;
            this.severity = severity;
            this.arrival = arrival;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ")";
        }
    }

    static class Triage {
        private long counter = 0;

        private final PriorityQueue<Patient> queue =
                new PriorityQueue<>(
                        Comparator.comparingInt((Patient p) -> p.severity)
                                .thenComparingLong(p -> p.arrival));

        void admit(String name, int severity) {
            queue.add(new Patient(name, severity, counter++));
        }

        Patient next() {
            return queue.poll();
        }
    }

    static void q6Triage() {
        Triage er = new Triage();
        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);

        StringBuilder order = new StringBuilder();
        Patient patient;
        while ((patient = er.next()) != null) {
            if (order.length() > 0) {
                order.append(' ');
            }
            order.append(patient);
        }
        System.out.println(order);
    }

    // ---------------------------------------------------------------------
    // Q7 - Diagnose and redesign repeated String concatenation
    // ---------------------------------------------------------------------

    static String buildTicket(String[] fields) {
        StringBuilder ticket = new StringBuilder();
        for (String field : fields) {
            ticket.append(field).append("|");
        }
        return ticket.toString();
    }

    static void q7StringBuilderRedesign() {
        String[] fields = createFields(200);
        int repetitions = 1000;

        long start = System.nanoTime();
        String original = "";
        for (int i = 0; i < repetitions; i++) {
            original = usingString(fields);
        }
        long originalTime = System.nanoTime() - start;

        start = System.nanoTime();
        String redesigned = "";
        for (int i = 0; i < repetitions; i++) {
            redesigned = buildTicket(fields);
        }
        long redesignedTime = System.nanoTime() - start;

        System.out.println("Original String time: " + originalTime + " ns");
        System.out.println("StringBuilder time:   " + redesignedTime + " ns");
        System.out.println("Output identical:     " + original.equals(redesigned));
        System.out.println("Ticket length:        " + redesigned.length());
    }
}
