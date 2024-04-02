package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

public class BnfRule {

    private final String name;

    private final TokenGroup tokens;

    private final Map<String, BnfRule> rules;

    private Deque<CurrentParse> queue = new LinkedList<>();

    record CurrentParse(Token[] branch, String input) {
    }

    public BnfRule(String name, TokenGroup tokens, Map<String, BnfRule> rules) {
        this.name = name;
        this.tokens = tokens;
        this.rules = rules;
    }

    public boolean test(String input) {
        var result = testBranches(input, false);
        return result.success;
    }

    private TestResult testBranches(String input, boolean partial) {
        return testBranches(this.tokens.getBranches(), input, partial);
    }

    private TestResult testBranches(Token[][] branches, String input, boolean partial) {
        for (int branchId = 0; branchId < branches.length; branchId++) {
            var branch = branches[branchId];

            if (queue.contains(new CurrentParse(branch, input))) {
//                throw new RuntimeException("INFINITE LOOP DETECTED!");
                continue;
            }
            queue.add(new CurrentParse(branch, input));

            System.out.println("Testing branch " + this.name + "#" + branchId);

            TestResult result = testBranch(branch, null, input);
            System.out.println("Branch " + this.name + "#" + branchId + " result: " + result + " for '" + input + "', Match: " + result.matched);

            TestResult originResult = result;

            int j = 0;/*
            while (result.success && !result.remaining.isBlank() && result.matched != null) {
                var result2 = testBranch(branch, result.matched, result.remaining);
                System.out.println("Result#" + (++j) + ": " + result2);

                if (result2.success && !result2.remaining.isBlank() && result2.matched != null) {
                    result = result2;
                    continue;
                }

                if (result2.success && (partial || result2.remaining.isBlank()))
                    return new TestResult(true, result.remaining, this);

                break;
            }*/

            if (originResult.success && (partial || originResult.remaining.isBlank())) {
                queue.remove(new CurrentParse(branch, input));
                return new TestResult(true, originResult.remaining, this);
            }
            if (!originResult.success) {
                System.out.println("Failed.");
            }

            queue.remove(new CurrentParse(branch, input));
        }

        return new TestResult(false, input, this);
    }

    private TestResult testBranch(Token[] branch, BnfRule lastMatched, String input) {
        int i = 0;
        System.out.println("Testing branch of " + name + " with input: " + input);
        for (; i < branch.length; i++) {
            var token = branch[i];
            if (token instanceof BnfNonTerminalToken inst) {
                if (i == 0 && lastMatched != null) {
                    if (!lastMatched.name.equals(inst.getName()) && inst.getCount() != TokenSpecial.ZERO_OR_MORE) {
                        return new TestResult(false, input, null);
                    }
                } else {
                    var result = rules.get(inst.getName()).testBranches(input, true);
                    if (!result.success && inst.getCount() != TokenSpecial.ZERO_OR_MORE)
                        return new TestResult(false, input, null);

                    input = result.remaining;
                }
            } else if (token instanceof BnfRegexToken inst) {
                var result = String.valueOf(input.charAt(0)).matches(inst.getRegex());
                if (!result)
                    return new TestResult(false, input, null);

                input = input.substring(1);
            } else if (token instanceof TokenGroup inst) {
                var result = testBranches(inst.getBranches(), input, true);
                if (!result.success && inst.count != TokenSpecial.ZERO_OR_MORE)
                    return new TestResult(false, input, null);

                input = result.remaining;
            } else if (token instanceof TokenSpecial inst) {
                var result = input.charAt(0) == inst.getSymbol();
                if (!result)
                    return new TestResult(false, input, null);

                input = input.substring(1);
            } else if (token instanceof BnfTerminalToken inst) {
                var result = input.startsWith(inst.getValue());
                if (!result && inst.count != TokenSpecial.ZERO_OR_MORE)
                    return new TestResult(false, input, null);

                if (!result && inst.count.getSymbol() == TokenSpecial.ZERO_OR_MORE.getSymbol())
                    continue;

                System.out.println("Terminal: '" + inst.getValue() + "': " + inst.count);

                input = input.substring(inst.getValue().length());
            } else {
                System.out.println("Unknown token: " + token);
            }
        }

        return new TestResult(true, input, this);
    }

    @Override
    public String toString() {
        return name;
    }

    record TestResult(boolean success, String remaining, BnfRule matched) {
    }

}
