package com.github.alantr7.codebots.language.compiler.bnf;

import java.util.Deque;
import java.util.LinkedList;

public class GrammarRule {

    private final Grammar grammar;

    private final String name;

    private final TokenGroup tokens;

    private Deque<CurrentParse> queue = new LinkedList<>();

    record CurrentParse(Token[] branch, String input) {
    }

    public GrammarRule(Grammar grammar, String name, TokenGroup tokens) {
        this.grammar = grammar;
        this.name = name;
        this.tokens = tokens;
    }

    // TODO: For some reason "if" requires a space after it
    // TODO: For some reason, multiple parameters don't work

    // TODO: If doesn't work if there's no space before it (other things as well): D(3) tT.P(24)if   (59){   }

    // TODO: For loop with a var that's not assigned, doesn't work
    //       # because optional argument inside of a non-optional group isn't properly parsed)

    // TODO: Use LL(1) instead.
    public ResultNode compile(String input) {
        var root = new ResultNode();
        root.setName(name);

        var result = testBranches(root, input, false, false, 0);
        if (!result.success)
            return new ResultNode();

        return root;
    }

    private TestResult testBranches(ResultNode node, String input, boolean partial, boolean isGroup, int indent) {
        var result = testBranches(node, this.tokens.getBranches(), input, partial, isGroup, indent);
        return result;
    }

    private TestResult testBranches(ResultNode parent, Token[][] branches, String input, boolean partial, boolean isGroup, int indent) {
        var node = parent;

        for (int branchId = 0; branchId < branches.length; branchId++) {
            var branch = branches[branchId];

            if (queue.contains(new CurrentParse(branch, input))) {
//                throw new RuntimeException("INFINITE LOOP DETECTED!");
                continue;
            }
            queue.add(new CurrentParse(branch, input));

            System.out.println(" ".repeat(indent) + "Testing branch " + this.name + "#" + branchId);

            TestResult result = testBranch(node, branch, null, input, isGroup, indent);
            System.out.println(" ".repeat(indent) + "Branch " + this.name + "#" + branchId + " result: " + result + " for '" + input + "', Match: " + result.matched + ", Node: " + result.node);

            TestResult originResult = result;

            int j = 0;

            // TODO: Sometimes enters an infinite loop.
            //       Temporarily disabled. (during testing was only needed for math)

            while (result.success && !result.remaining.isBlank() && result.matched != null && j++ < 2) {
                var result2 = testBranch(node, branch, result.matched, result.remaining, isGroup, indent);
                System.out.println("Result#" + (++j) + ": " + result2);

                if (result2.success && !result2.remaining.isBlank() && result2.matched != null) {
                    result = result2;
                    continue;
                }

                if (result2.success && (partial || result2.remaining.isBlank()))
                    return new TestResult(true, result2.remaining, this);

                break;
            }
/*
            if (result.success && (partial || result.remaining.isBlank())) {
                queue.remove(new CurrentParse(branch, input));
                if (!isGroup) {
                    node.getChildren().add(result.node);
                }
                return new TestResult(true, result.remaining, this, result.node);
            }
            if (!result.success) {
                System.out.println("Failed.");
            }*/

            // TODO: No idea why originResult is used instead of result

            if (originResult.success && (partial || originResult.remaining.isBlank())) {
                queue.remove(new CurrentParse(branch, input));
                if (!isGroup) {
                    node.getChildren().add(result.node);
                }
                return new TestResult(true, originResult.remaining, this, result.node);
            }
            if (!originResult.success) {
                System.out.println("Failed.");
            }

            queue.remove(new CurrentParse(branch, input));
        }

        return new TestResult(false, input, this, node);
    }

    private TestResult testBranch(ResultNode parent, Token[] branch, GrammarRule lastMatched, String input, boolean isGroup, int indent) {
        int i = 0;
        ResultNode node;

        if (isGroup) {
            node = parent;
        } else {
            node = new ResultNode();
            node.setName(name);
        }

        System.out.println(" ".repeat(indent) + "Testing branch of " + name + " with input: " + input);
        if (name.equals("var_assign")) {
            boolean a = true;
        }

        for (; i < branch.length; i++) {
            var token = branch[i];
            if (token instanceof BnfNonTerminalToken inst) {
                // TODO: This worked before adding the loop. After loop it might not but not sure?
                if (i == 0 && lastMatched != null) {
                    System.out.println("This thing.");
                    if (!lastMatched.name.equals(inst.getName()) && inst.getCount() != TokenSpecial.ZERO_OR_MORE && inst.getCount() != TokenSpecial.ZERO_OR_ONE) {
                        return new TestResult(false, input, null);
                    }
                } else {
                    TestResult result;
                    String previous = input;
                    while (!input.isEmpty()) {
                        result = grammar.getRule(inst.getName()).testBranches(node, input, true, false, indent + 2);
                        if (!result.success && inst.getCount() != TokenSpecial.ZERO_OR_MORE && inst.getCount() != TokenSpecial.ZERO_OR_ONE)
                            return new TestResult(false, input, null);

                        input = result.remaining;
                        if (input.equals(previous))
                            break;

                        previous = input;

                        if (inst.count == TokenSpecial.ONE || inst.count == TokenSpecial.ZERO_OR_ONE)
                            break;
                    }
                }
            } else if (token instanceof BnfRegexToken inst) {
                int matches = 0;
                String originalInput = input;
                while (!input.isEmpty() && String.valueOf(input.charAt(0)).matches(inst.getRegex())) {
                    matches++;
                    input = input.substring(1);
                }
                System.out.println("Regex matches: " + matches);

                if (matches == 0 && inst.count != TokenSpecial.ZERO_OR_MORE)
                    return new TestResult(false, input, null);

                node.getChildren().add(new ResultNode(inst.getRegex(), originalInput.substring(0, matches)));
            } else if (token instanceof TokenGroup inst) {
                /*
                var result = testBranches(node, inst.getBranches(), input, true, true, indent + 2);
                if (!result.success && inst.count != TokenSpecial.ZERO_OR_MORE)
                    return new TestResult(false, input, null);

                input = result.remaining;*/

                TestResult result = null;
                int matches = 0;

                String previous = input;

                while (!input.isEmpty()) {
                    result = testBranches(node, inst.getBranches(), input, true, true, indent + 2);
                    if (!result.success && inst.getCount() != TokenSpecial.ZERO_OR_MORE)
                        break;

                    input = result.remaining;
                    if (input.equals(previous))
                        break;

                    previous = input;
                    matches++;

                    // TODO: For some reason it doesn't work when line below is removed /shrug
                    if (inst.getCount() == TokenSpecial.ONE || inst.getCount() == TokenSpecial.ZERO_OR_ONE)
                        break;
                }

                if (matches == 0 && inst.getCount() != TokenSpecial.ZERO_OR_MORE && inst.getCount() != TokenSpecial.ZERO_OR_ONE) {
                    return new TestResult(false, input, null);
                }
            } else if (token instanceof TokenSpecial inst) {
                var result = input.charAt(0) == inst.getSymbol();
                if (!result)
                    return new TestResult(false, input, null);

                input = input.substring(1);
            } else if (token instanceof BnfTerminalToken inst) {
                int matches = 0;
                String originalInput = input;
                while (input.startsWith(inst.getValue())) {
                    matches++;

                    input = input.substring(inst.getValue().length());

                    if (inst.count == TokenSpecial.ONE)
                        break;
                }

                var result = matches != 0;
                System.out.println(" ".repeat(indent) + "Terminal: '" + inst.getValue() + "': " + inst.count + ", Matches: " + matches);

                if (!result && inst.count == TokenSpecial.ONE_OR_MORE)
                    return new TestResult(false, input, null);

                if (!result && inst.count != TokenSpecial.ZERO_OR_MORE && inst.count != TokenSpecial.ZERO_OR_ONE)
                    return new TestResult(false, input, null);

                node.getChildren().add(new ResultNode(inst.getValue()));
            } else {
                System.out.println("Unknown token: " + token);
            }
        }

        return new TestResult(true, input, this, node);
    }

    @Override
    public String toString() {
        return name;
    }

    record TestResult(boolean success, String remaining, GrammarRule matched, ResultNode node) {

        TestResult(boolean success, String remaining, GrammarRule matched) {
            this(success, remaining, matched, null);
        }

    }

}
