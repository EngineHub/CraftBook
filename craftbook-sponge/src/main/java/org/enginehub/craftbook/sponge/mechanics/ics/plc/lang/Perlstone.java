/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.craftbook.sponge.mechanics.ics.plc.lang;

import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.InvalidICException;
import org.enginehub.craftbook.sponge.mechanics.ics.plc.PlcException;
import org.enginehub.craftbook.sponge.mechanics.ics.plc.PlcLanguage;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class Perlstone implements PlcLanguage {
    private static final int MAX_INSTRUCTION_COUNT = 10000;
    private static final int MAX_STACK_SIZE = 64;
    private static final int MAX_RECURSION = 16;

    @Override
    public String getName() {
        return "Perlstone-1.1";
    }

    @Override
    public List<Boolean> initState() {
        List<Boolean> initState = new ArrayList<>(32);
        for (int i = 0; i < initState.size(); i++) {
            initState.add(false);
        }
        return initState;
    }

    private static WithLineInfo markLines(String code) {
        char[] chars = code.toCharArray();
        ArrayList<LineInfo> lines = new ArrayList<>();
        int line = 1;
        int col = 1;
        for (char aChar : chars) {
            lines.add(new LineInfo(line, col));
            switch (aChar) {
                case '\n':
                    line++;
                    col = 0;
                    break;
                default:
                    break;
            }
            col++;
        }
        return new WithLineInfo(lines.toArray(new LineInfo[lines.size()]), new String(chars));
    }

    private static char[] fixArray(Character[] c) {
        char[] o = new char[c.length];
        for (int i = 0; i < c.length; i++) {
            o[i] = c[i];
        }
        return o;
    }

    private static WithLineInfo[] splitFunctions(WithLineInfo chars) {
        ArrayList<WithLineInfo> lines = new ArrayList<>();
        ArrayList<Character> current = new ArrayList<>();
        ArrayList<LineInfo> currentLineInfo = new ArrayList<>();

        char[] data = chars.getCode().toCharArray();
        for (int i = 0; i < data.length; i++) {
            switch (data[i]) {
                case ':':
                    lines.add(new WithLineInfo(currentLineInfo.toArray(new LineInfo[currentLineInfo.size()]),
                            new String(fixArray(current.toArray(new Character[current.size()])))));
                    current.clear();
                    currentLineInfo.clear();
                    break;
                case '\n':
                case ' ':
                    break;
                default:
                    current.add(data[i]);
                    currentLineInfo.add(chars.getLineInfo()[i]);
            }
        }
        lines.add(new WithLineInfo(currentLineInfo.toArray(new LineInfo[currentLineInfo.size()]),
                new String(fixArray(current.toArray(new Character[current.size()])))));
        return lines.toArray(new WithLineInfo[lines.size()]);
    }

    @Override
    public WithLineInfo[] compile(String code) throws InvalidICException {
        WithLineInfo[] functions = splitFunctions(markLines(code));
        for (int l = 0; l < functions.length; l++) {
            WithLineInfo line = functions[l];
            char[] chars = line.getCode().toCharArray();
            LineInfo[] li = line.getLineInfo();
            int bracketCount = 0;
            if (chars.length == 0) {
                continue;
            }
            for (int i = 0; i < chars.length; i++) {
                try {
                    char c = chars[i];
                    switch (c) {
                        case '[':
                            bracketCount++;
                            break;
                        case ']':
                            if (bracketCount == 0)
                                throw new InvalidICException("Too many closing braces " + "on line " + li[i].getLine() + " at column " + li[i].getColumn());
                            bracketCount--;
                            break;
                        case '+':
                        case '-':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'd':
                        case 'p':
                        case 'x':
                        case '!':
                        case '|':
                        case '=':
                        case '&':
                        case '^':
                        case 's':
                        case 'r':
                            break;

                        case '<':
                        case '>':
                        case 'e':
                        case 'S':
                        case 'L':
                            switch (chars[++i]) {
                                case 'p':
                                case 'r':
                                case 'l':
                                case 'P':
                                case 'R':
                                case 'L':
                                    break;
                                default:
                                    throw new InvalidICException("Unknown modifier " + chars[i] + " to opcode " + c + " on line "
                                            + li[i].getLine() + " at column " + li[i].getColumn());
                            }
                            if (c == 'S' || c == 'L') {
                                char p = chars[++i];
                                if (!(p >= '0' || p <= '9') && !(p >= 'a' || p <= 'v'))
                                    throw new InvalidICException("Bad table index " + chars[i] + " for opcode "
                                            + c + " on line " + li[i].getLine() + " at column " + li[i].getColumn());
                            }
                            break;

                        case 'v': {
                            char n = chars[++i];
                            if (!(n >= '0' || n <= '9'))
                                throw new InvalidICException("Bad peek depth " + chars[i] + " on line " +
                                        li[i].getLine() + " at column " + li[i].getColumn());
                        }
                        break;

                        case '.':
                            for (int j = 0; j < 4; j++) {
                                switch (chars[++i]) {
                                    case '+':
                                    case '-':
                                    case '1':
                                    case '0':
                                        break;
                                    default:
                                        throw new InvalidICException("Bad logic table value " + chars[i] + " on line " + li[i].getLine()
                                                + " at column " + li[i].getColumn());
                                }
                            }
                            break;

                        case 'c':
                        case 't':
                            for (int j = 0; j < 2; j++) {
                                char n = chars[++i];
                                if (!(n >= '0' || n <= '9'))
                                    throw new InvalidICException("Invalid character " + chars[i] + " in function" +
                                            " number " + "on line "
                                            + li[i].getLine() + " at column " + li[i].getColumn());
                            }
                        {
                            char n = chars[++i];
                            if (!(n >= '0' || n <= '9'))
                                throw new InvalidICException("Invalid character " + chars[i] + " in argument " +
                                        "count " + "on line "
                                        + li[i].getLine() + " at column " + li[i].getColumn());
                        }
                        break;

                        default:
                            throw new InvalidICException("Unknown opcode " + c + " on line " + li[i].getLine() +
                                    " at column " + li[i].getColumn());
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    i = li.length - 1;
                    throw new InvalidICException("Unexpected function end " + "around line " + li[i].getLine());
                }
            }
            if (bracketCount != 0)
                throw new InvalidICException("Missing closing braces in function #" + l + "                           starting on line "
                        + li[0].getLine() + " and ending on line " + li[li.length - 1].getLine());
        }
        return functions;
    }

    @Override
    public void execute(IC ic, List<Boolean> state, WithLineInfo[] code) throws PlcException {
        List<Boolean> tt = initState();
        boolean a = ic.getPinSet().getInputCount() > 0 && ic.getPinSet().getInput(0, ic);
        boolean b = ic.getPinSet().getInputCount() > 1 && ic.getPinSet().getInput(1, ic);
        boolean c = ic.getPinSet().getInputCount() > 2 && ic.getPinSet().getInput(2, ic);

        for (int i = 0; i < ic.getPinSet().getOutputCount(); i++) {
            if (i < code.length) {
                Boolean result = executeFunction(i, state, tt, code, a, b, c, new boolean[0], new int[1], 0);
                if (result == null) {
                    ic.getPinSet().setOutput(i, false, ic);
                } else {
                    ic.getPinSet().setOutput(i, result, ic);
                }
            } else {
                ic.getPinSet().setOutput(i, false, ic);
            }
        }
    }

    @Override
    public boolean supports(String lang) {
        return false;
    }

    private static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    private static int decodeAddress(char c, int shift) {
        if (c >= '0' && c <= '9') return mod(c - '0' + shift, 32);
        else return mod(c - 'a' + 10 + shift, 32);
    }

    private static int parseNumber(char c) {
        return c - '0';
    }

    private static boolean parseTableChar(char c) throws PlcException {
        switch (c) {
            case '+':
                return true;
            case '-':
                return false;
            case '1':
                return true;
            case '0':
                return false;

            // Shouldn't happen because of validation.
            default:
                throw new PlcException("invalid table", "Invalid character in logic table.");
        }
    }

    private static String errmsg(String err, int fno, char opcode, LineInfo li, List<Boolean> pt, List<Boolean> tt, List<Boolean> lt,
            int pshift, int tshift, int lshift, Stack<Boolean> stack, int tc) {
        String errm = "";
        if (!err.startsWith("&4Detailed Error Message: ")) {
            errm += "&4Detailed Error Message: &r" + err + '\n';
            errm += "&4Persistent Variable Table: \n &r" + dumpStateText(pt) + '\n';
            errm += "&4 - Shift: &r" + pshift + '\n';
            errm += "&4Temp Variable Table: \n &r" + dumpStateText(tt) + '\n';
            errm += "&4 - Shift: &r" + tshift + '\n';
        } else {
            errm += err + '\n';
        }
        errm += "&4====\n";
        if (tc > 0) {
            errm += "(" + tc + " tail call" + (tc > 1 ? "s" : "") + " omitted)\n====\n";
        }
        errm += "&4Location: &rOpcode " + opcode + " at line " + li.getLine() + ", " +
                "column " + li.getColumn() + " in function #"
                + fno + '\n';
        errm += "&4Local Variable Table: \n &r" + dumpStateText(lt) + '\n';
        errm += "&4 - Shift: &r" + lshift + '\n';
        errm += "&4Function Stack: &r" + dumpStateText(stack);
        return errm;
    }

    // Use wrapper type to be able to express "no return" as null
    private Boolean executeFunction(int fno, List<Boolean> pt, List<Boolean> tt, WithLineInfo[] funs, boolean a,
            boolean b, boolean c, boolean[] args, int[] opc, int rec) throws PlcException {
        int tailcalls = 0;

        // Wrap in an while(true) to allow the t opcode to be a tail call.
        outer:
        while (true) {
            String fn = funs[fno].getCode();
            LineInfo[] lis = funs[fno].getLineInfo();
            char[] code = fn.toCharArray();
            int[] jt = new int[code.length]; // Jump table so that [ and ] aren't that messy
                /* scope */
            {
                Stack<Integer> bracketStack = new Stack<>();
                for (int i = 0; i < code.length; i++) {
                    char ch = code[i];
                    if (ch == '[') {
                        bracketStack.push(i);
                    } else if (ch == ']') {
                        int j = bracketStack.pop();
                        jt[i] = j;
                        jt[j] = i;
                    }
                }
            }

            int ip = 0;
            Stack<Boolean> executionStack = new Stack<>();
            for (boolean arg1 : args) {
                executionStack.push(arg1);
            }
            List<Boolean> lt = initState();
            int pshift = 0;
            int tshift = 0;
            int lshift = 0;
            char op = '?';

            LineInfo li = new LineInfo(0, 0);
            try {
                if (rec > MAX_RECURSION)
                    throw new PlcException("stack overflow", "Aborted due to too many recursive non-tail calls.");
                try {
                    while (ip < code.length) {
                        opc[0]++;
                        if (opc[0] == MAX_INSTRUCTION_COUNT)
                            throw new PlcException("ran too long", "Aborted due to running too many instructions in " +
                                    "one update");
                        if (executionStack.size() > MAX_STACK_SIZE)
                            throw new PlcException("stack too big", "Aborted due to too many values pushed onto stack.");
                        op = code[ip];
                        li = lis[ip];
                        switch (op) {
                            case '+':
                                executionStack.push(true);
                                break;
                            case '-':
                                executionStack.push(false);
                                break;

                            case 'A':
                                executionStack.push(a);
                                break;
                            case 'B':
                                executionStack.push(b);
                                break;
                            case 'C':
                                executionStack.push(c);
                                break;

                            case '<':
                            case '>':
                            case 'e': {
                                int mul = 1;
                                int add = 0;
                                switch (code[ip]) {
                                    case '<':
                                        add = -1;
                                        break;
                                    case '>':
                                        add = +1;
                                        break;
                                    case 'e':
                                        mul = 0;
                                        break;
                                    default:
                                        break;
                                }

                                switch (code[++ip]) {
                                    case 'p':
                                    case 'P':
                                        pshift = mul * pshift + add;
                                        break;
                                    case 't':
                                    case 'T':
                                        tshift = mul * tshift + add;
                                        break;
                                    case 'l':
                                    case 'L':
                                        lshift = mul * lshift + add;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;

                            case 'S':
                            case 'L': {
                                List<Boolean> table = null;
                                int shift = 0;

                                switch (code[++ip]) {
                                    case 'p':
                                        shift = pshift;
                                    case 'P':
                                        table = pt;
                                        break;
                                    case 't':
                                        shift = tshift;
                                    case 'T':
                                        table = tt;
                                        break;
                                    case 'l':
                                        shift = lshift;
                                    case 'L':
                                        table = lt;
                                        break;
                                    default:
                                        break;
                                }
                                if(table == null)
                                    break;

                                int add = decodeAddress(code[++ip], shift);
                                if (op == 'S')
                                    table.set(add, executionStack.pop());
                                else
                                    executionStack.push(table.get(add));
                            }
                            break;

                            case 'd':
                                executionStack.push(executionStack.peek());
                                break;
                            case 'p':
                                executionStack.pop();
                                break;
                            case 'v':
                                try {
                                    int level = parseNumber(code[++ip]);
                                    executionStack.push(executionStack.get(executionStack.size() - 1 - level));
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    throw new PlcException("bad stack pos", "Attempted to call peek on too small a stack.");
                                }
                                break;
                            case 'x':
                                boolean x = executionStack.pop();
                                boolean y = executionStack.pop();
                                executionStack.push(x);
                                executionStack.push(y);
                                break;
                            default:
                                break;

                            case '!':
                                executionStack.push(!executionStack.pop());
                                break;

                            // Using the short-circuiting versions would cause it to sometimes pop one less
                            // value than it should.
                            case '^':
                                executionStack.push(executionStack.pop() ^ executionStack.pop());
                                break;
                            case '&':
                                executionStack.push(executionStack.pop() & executionStack.pop());
                                break;
                            case '|':
                                executionStack.push(executionStack.pop() | executionStack.pop());
                                break;
                            case '=':
                                executionStack.push(executionStack.pop() == executionStack.pop());
                                break;

                            case '.':
                                boolean ta = parseTableChar(code[++ip]);
                                boolean tb = parseTableChar(code[++ip]);
                                boolean tc = parseTableChar(code[++ip]);
                                boolean td = parseTableChar(code[++ip]);

                                boolean e = executionStack.pop();
                                boolean f = executionStack.pop();

                                if (!e && !f) {
                                    executionStack.push(ta);
                                } else if (!e && f) {
                                    executionStack.push(tb);
                                } else if (e && !f) {
                                    executionStack.push(tc);
                                } else {
                                    executionStack.push(td);
                                }
                                break;

                            case 'c':
                            case 't':
                                int n = parseNumber(code[++ip]) * 10 + parseNumber(code[++ip]);
                                int nArgs = parseNumber(code[++ip]);
                                boolean[] arg = new boolean[nArgs];

                                if (n < 0 || n >= funs.length)
                                    throw new PlcException("func not found", "Attempted to call nonexistent function #" + n);

                                if (op == 'c') {
                                    for (int i = nArgs - 1; i >= 0; i--) {
                                        arg[i] = executionStack.pop();
                                    }
                                    Boolean v = executeFunction(n, pt, tt, funs, a, b, c, arg, opc, rec + 1);
                                    if (v != null) {
                                        executionStack.push(v);
                                    }
                                    break;
                                } else {
                                    fno = n;
                                    args = arg;
                                    tailcalls++;
                                    continue outer;
                                }

                            case '[':
                                if (!executionStack.pop()) {
                                    ip = jt[ip];
                                }
                                break;
                            case ']':
                                if (executionStack.pop()) {
                                    ip = jt[ip];
                                }
                                break;

                            case 's':
                                return null;
                            case 'r':
                                return executionStack.pop();
                        }
                        ip++;
                    }
                } catch (EmptyStackException e) {
                    throw new PlcException("empty stack", "Popped while stack was empty.");
                } catch (StackOverflowError e) {
                    throw new PlcException("stack overflow", "Java stack overflow.");
                }
            } catch (PlcException e) {
                throw new PlcException(e.getMessage(), errmsg(e.detailedMessage, fno, op, li, pt, tt, lt, pshift,
                        tshift, lshift, executionStack, tailcalls));
            }
            return null;
        }
    }

    private static String dumpStateText(List<Boolean> state) {
        char[] c = new char[state.size()];
        for (int i = 0; i < state.size(); i++) {
            c[i] = state.get(i) ? '1' : '0';
        }
        return new String(c);
    }

    @Override
    public String dumpState(List<Boolean> state) {
        return "&4Persistent Variable Table: \n &r" + dumpStateText(state);
    }
}
