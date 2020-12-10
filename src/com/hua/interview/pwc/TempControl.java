package com.hua.interview.pwc;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

import static com.hua.interview.pwc.TempControl.Status.*;

public class TempControl {

    private static String INPUT_FORMAT = "Please input %s: ";
    private static String INVALID_INPUT = "Invalid input, s";

    private static BigDecimal freezingThreshold;
    private static BigDecimal boilingThreshold;
    private static BigDecimal fluctuationValue;
    private static Set<Status> statusSet;
    private static Deque<BigDecimal> deque = new ArrayDeque<>();

    public static void main(String[] args) {
        statusSet = new HashSet<>();
        freezingThreshold = thresholdInput("freezing threshold");
        boilingThreshold = thresholdInput("boiling threshold");
        fluctuationValue = thresholdInput("fluctuation value");
        tempInput();
        alert();
    }


    private static BigDecimal thresholdInput(String message) {
        System.out.print(String.format(INPUT_FORMAT, message));
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextBigDecimal()) {
            return scanner.nextBigDecimal();
        } else {
            System.out.print(INVALID_INPUT);
            thresholdInput(message);
        }
        return null;
    }

    private static void tempInput() {
        System.out.println("Please input the temperatures, separate them with space:");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextLine()) {
            String[] split = scanner.nextLine().trim().split("\\s");
            try {
                Arrays.stream(split).forEach(i -> deque.offer(new BigDecimal(i)));
            } catch (NumberFormatException e) {
                System.out.print("Input contains invalid numbers, ");
                tempInput();
            }

        } else {
            tempInput();
        }
    }

    private static void alert() {
        BigDecimal previous = null;
        while (deque.size() != 0) {
            BigDecimal current = deque.poll();

            if (!checkNotNull(previous)) {
                previous = current;
            }

            System.out.print(current + " ");

            boolean freezingRange = checkWithinRange(current, freezingThreshold);
            boolean boilingRange = checkWithinRange(current, boilingThreshold);

            if ((checkNotNull(previous) && ls(previous, freezingThreshold) && le(current, freezingThreshold)) || (current.compareTo(freezingThreshold) <= 0)) {
                updateStatus(FREEZING);
            }
            if (checkNotNull(previous) && checkUnfreeze(previous, freezingThreshold) && ge(current, freezingThreshold) && !freezingRange) {
                updateStatus(UNFREEZING);
            }
            if ((checkNotNull(previous) && gt(previous, boilingThreshold) && ge(current, boilingThreshold)) || (current.compareTo(boilingThreshold) >= 0)) {
                updateStatus(BOILING);
            }
            if (checkNotNull(previous) && checkUnboiling(previous, boilingThreshold) && le(current, boilingThreshold) && !boilingRange) {
                updateStatus(UNBOILING);
            }

            previous = current;

        }
        System.exit(1);
    }

    private static <T> boolean checkNotNull(T t) {
        return t != null;
    }

    private static void updateStatus(Status status) {
        if (!statusSet.contains(status)) {
            System.out.print(status);
            statusSet.add(status);
            statusSet.remove(status.opposite());
        }
    }

    private static boolean checkUnfreeze(BigDecimal val, BigDecimal threshold) {
        return le(val, threshold, (x, y) -> x.add(y));
    }

    private static boolean ls(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) < 0;
    }

    private static boolean le(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) <= 0;
    }

    private static boolean le(BigDecimal val1, BigDecimal val2, BiFunction<BigDecimal, BigDecimal, BigDecimal> function) {
        return val1.compareTo(function.apply(val2, fluctuationValue)) <= 0;
    }

    private static boolean gt(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) > 0;
    }

    private static boolean ge(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) >= 0;
    }

    private static boolean ge(BigDecimal val1, BigDecimal val2, BiFunction<BigDecimal, BigDecimal, BigDecimal> function) {
        return val1.compareTo(function.apply(val2, fluctuationValue)) >= 0;
    }

    private static boolean checkUnboiling(BigDecimal val, BigDecimal threshold) {
        return ge(val, threshold, (x, y) -> x.subtract(y));
    }

    private static boolean checkWithinRange(BigDecimal val, BigDecimal threshold) {
        return threshold.add(fluctuationValue).compareTo(val) >= 0 && threshold.subtract(fluctuationValue).compareTo(val) <= 0;
    }

    enum Status {
        FREEZING("freezing"),
        UNFREEZING("unfreezing"),
        BOILING("boiling"),
        UNBOILING("unboiling");

        private String description;

        Status(String description) {
            this.description = description;
        }

        public Status opposite() {
            switch (this) {
                case FREEZING:
                    return UNFREEZING;
                case UNFREEZING:
                    return FREEZING;
                case BOILING:
                    return UNBOILING;
                case UNBOILING:
                    return BOILING;
                default:
                    throw new IllegalArgumentException("Invalid status, please check.");
            }
        }

        @Override
        public String toString() {
            return this.description + " ";
        }
    }
}
