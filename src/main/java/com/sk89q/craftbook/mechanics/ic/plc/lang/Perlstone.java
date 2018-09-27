// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.mechanics.ic.plc.lang;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.ChatColor;

import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.plc.PlcException;
import com.sk89q.craftbook.mechanics.ic.plc.PlcLanguage;

public class Perlstone implements PlcLanguage<boolean[], WithLineInfo<String>[]> {

    private static final int MAX_INSTRUCTION_COUNT = 10000;
    private static final int MAX_STACK_SIZE = 64;
    private static final int MAX_RECURSION = 16;
    private static final int PERLSTONE_STORE_VERSION = 0;

    @Override
    public String getName() {

        return "Perlstone-1.1";
    }

    @Override
    public boolean[] initState() {

        return new boolean[32];
    }

    private WithLineInfo<char[]> markLines(String code) {

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
        return new WithLineInfo<>(lines.toArray(new LineInfo[lines.size()]), chars);
    }

    private char[] fixArray(Character[] c) {

        char[] o = new char[c.length];
        for (int i = 0; i < c.length; i++) {
            o[i] = c[i];
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    private WithLineInfo<String>[] splitFunctions(WithLineInfo<char[]> chars) {

        ArrayList<WithLineInfo<String>> lines = new ArrayList<>();
        ArrayList<Character> current = new ArrayList<>();
        ArrayList<LineInfo> currentLineInfo = new ArrayList<>();

        char[] data = chars.code;
        for (int i = 0; i < data.length; i++) {
            switch (data[i]) {
                case ':':
                    lines.add(new WithLineInfo<>(currentLineInfo.toArray(new LineInfo[currentLineInfo.size()]),
                            new String(fixArray(current
                                    .toArray(new Character[current.size()])))));
                    current.clear();
                    currentLineInfo.clear();
                    break;
                case '\n':
                case ' ':
                    break;
                default:
                    current.add(data[i]);
                    currentLineInfo.add(chars.lineInfo[i]);
            }
        }
        lines.add(new WithLineInfo<>(currentLineInfo.toArray(new LineInfo[currentLineInfo.size()]),
                new String(fixArray(current
                        .toArray(new Character[current.size()])))));
        return lines.toArray(new WithLineInfo[lines.size()]);
    }

    @Override
    public WithLineInfo<String>[] compile(String code) throws ICVerificationException {

        WithLineInfo<String>[] functions = splitFunctions(markLines(code));
        for (int l = 0; l < functions.length; l++) {
            WithLineInfo<String> line = functions[l];
            char[] chars = line.code.toCharArray();
            LineInfo[] li = line.lineInfo;
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
                                throw new ICVerificationException("Too many closing braces " + "on line " + li[i]
                                        .line + " at column " + li[i].col);
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
                                    throw new ICVerificationException("Unknown modifier " + chars[i] + " to opcode "
                                            + c + " " + "on line "
                                            + li[i].line + " at column " + li[i].col);
                            }
                            if (c == 'S' || c == 'L') {
                                char p = chars[++i];
                                if (!(p >= '0' || p <= '9') && !(p >= 'a' || p <= 'v'))
                                    throw new ICVerificationException("Bad table index " + chars[i] + " for opcode "
                                            + c + " " + "on line "
                                            + li[i].line + " at column " + li[i].col);
                            }
                            break;

                        case 'v': {
                            char n = chars[++i];
                            if (!(n >= '0' || n <= '9'))
                                throw new ICVerificationException("Bad peek depth " + chars[i] + " " + "on line " +
                                        li[i].line + " at column "
                                        + li[i].col);
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
                                        throw new ICVerificationException("Bad logic table value " + chars[i] + " " +
                                                "on line " + li[i].line
                                                + " at column " + li[i].col);
                                }
                            }
                            break;

                        case 'c':
                        case 't':
                            for (int j = 0; j < 2; j++) {
                                char n = chars[++i];
                                if (!(n >= '0' || n <= '9'))
                                    throw new ICVerificationException("Invalid character " + chars[i] + " in function" +
                                            " number " + "on line "
                                            + li[i].line + " at column " + li[i].col);
                            }
                            {
                                char n = chars[++i];
                                if (!(n >= '0' || n <= '9'))
                                    throw new ICVerificationException("Invalid character " + chars[i] + " in argument " +
                                            "count " + "on line "
                                            + li[i].line + " at column " + li[i].col);
                            }
                            break;

                        default:
                            throw new ICVerificationException("Unknown opcode " + c + " " + "on line " + li[i].line +
                                    " at column " + li[i].col);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    CraftBookBukkitUtil.printStacktrace(e);
                    i = li.length - 1;
                    throw new ICVerificationException("Unexpected function end " + "around line " + li[i].line);
                }
            }
            if (bracketCount != 0)
                throw new ICVerificationException("Missing closing braces in function #" + l + " " + "               " +
                        "           starting on line "
                        + li[0].line + " and ending on line " + li[li.length - 1].line);
        }
        return functions;
    }

    @Override
    public void writeState(boolean[] state, DataOutputStream out) throws IOException {

        out.writeInt(PERLSTONE_STORE_VERSION);
        out.writeInt(state.length);
        for (boolean aState : state) {
            out.writeBoolean(aState);
        }
    }

    @Override
    public void loadState(boolean[] state, DataInputStream in) throws IOException {

        if (in.readInt() != PERLSTONE_STORE_VERSION) throw new IOException("incompatible save version");
        if (state.length != in.readInt()) throw new IOException("size mismatch!");
        for (int i = 0; i < state.length; i++) {
            state[i] = in.readBoolean();
        }
    }

    @Override
    public void execute(ChipState chip, boolean[] state, WithLineInfo<String>[] code) throws PlcException {

        boolean[] tt = new boolean[32];
        boolean a = chip.getInputCount() > 0 && chip.getInput(0);
        boolean b = chip.getInputCount() > 1 && chip.getInput(1);
        boolean c = chip.getInputCount() > 2 && chip.getInput(2);

        for (int i = 0; i < chip.getOutputCount(); i++) {
            if (i < code.length) {
                Boolean r = executeFunction(i, state, tt, code, a, b, c, new boolean[0], new int[1], 0);
                if (r == null) {
                    chip.setOutput(i, false);
                } else {
                    chip.setOutput(i, r);
                }
            } else {
                chip.setOutput(i, false);
            }
        }
    }

    @Override
    public boolean supports(String lang) {

        return false;
    }

    private int mod(int a, int b) {

        return (a % b + b) % b;
    }

    private int decodeAddress(char c, int shift) {

        if (c >= '0' && c <= '9') return mod(c - '0' + shift, 32);
        else return mod(c - 'a' + 10 + shift, 32);
    }

    private int parseNumber(char c) {

        return c - '0';
    }

    private boolean parseTableChar(char c) throws PlcException {

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

    private String errmsg(String err, int fno, char opcode, LineInfo li, boolean[] pt, boolean[] tt, boolean[] lt,
            int pshift, int tshift,
            int lshift, Stack<Boolean> stack, int tc) {

        String errm = "";
        if (!err.startsWith(ChatColor.RED + "Detailed Error Message: ")) {
            errm += ChatColor.RED + "Detailed Error Message: " + ChatColor.RESET + err + "\n";
            errm += ChatColor.RED + "Persistent Variable Table: \n " + ChatColor.RESET + dumpStateText(pt) + "\n";
            errm += ChatColor.RED + " - Shift: " + ChatColor.RESET + pshift + "\n";
            errm += ChatColor.RED + "Temp Variable Table: \n " + ChatColor.RESET + dumpStateText(tt) + "\n";
            errm += ChatColor.RED + " - Shift: " + ChatColor.RESET + tshift + "\n";
        } else {
            errm += err + "\n";
        }
        errm += ChatColor.RED + "====\n";
        if (tc > 0) {
            errm += "(" + tc + " tail call" + (tc > 1 ? "s" : "") + " omitted)\n====\n";
        }
        errm += ChatColor.RED + "Location: " + ChatColor.RESET + "Opcode " + opcode + " at line " + li.line + ", " +
                "column " + li.col + " in function #"
                + fno + "\n";
        errm += ChatColor.RED + "Local Variable Table: \n " + ChatColor.RESET + dumpStateText(lt) + "\n";
        errm += ChatColor.RED + " - Shift: " + ChatColor.RESET + lshift + "\n";
        errm += ChatColor.RED + "Function Stack: " + ChatColor.RESET + dumpStateText(stack.toArray(new Boolean[stack
                                                                                                               .size()]));
        return errm;
    }

    // Use wrapper type to be able to express "no return" as null
    private Boolean executeFunction(int fno, boolean[] pt, boolean[] tt, WithLineInfo<String>[] funs, boolean a,
            boolean b, boolean c,
            boolean[] args, int[] opc, int rec) throws PlcException {

        int tailcalls = 0;

        // Wrap in an while(true) to allow the t opcode to be a tail call.
        outer:
            while (true) {
                String fn = funs[fno].code;
                LineInfo[] lis = funs[fno].lineInfo;
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
                boolean[] lt = new boolean[32];
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
                                throw new PlcException("stack too big", "Aborted due to too many values pushed onto stack" +
                                        ".");
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
                                    boolean[] table = null;
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
                                        table[add] = executionStack.pop();
                                    else
                                        executionStack.push(table[add]);
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
                                        throw new PlcException("bad stack pos", "Attempted to call peek on too small a " +
                                                "stack.");
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
                                        throw new PlcException("func not found", "Attempted to call nonexistent function " +
                                                "#" + n);

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
                            tshift, lshift, executionStack,
                            tailcalls));
                }
                return null;
            }
    }

    private String dumpStateText(boolean[] state) {

        char[] c = new char[state.length];
        for (int i = 0; i < state.length; i++) {
            c[i] = state[i] ? '1' : '0';
        }
        return new String(c);
    }

    private String dumpStateText(Boolean[] state) {

        char[] c = new char[state.length];
        for (int i = 0; i < state.length; i++) {
            c[i] = state[i] ? '1' : '0';
        }
        return new String(c);
    }

    @Override
    public String dumpState(boolean[] state) {

        return ChatColor.RED + "Persistent Variable Table: \n " + ChatColor.RESET + dumpStateText(state);
    }
}
