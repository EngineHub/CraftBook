// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.plc.lang;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.plc.*;
import org.bukkit.ChatColor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Perlstone implements PlcLanguage<boolean[], WithLineInfo<String[]>> {
    private static final int MAX_INSTRUCTION_COUNT = 100000;
    private static final int PERLSTONE_STORE_VERSION = 0;

    @Override
    public String getName() {
        return "Perlstone-1.1";
    }

    @Override
    public boolean[] initState() {
        return new boolean[32];
    }

    @Override
    public WithLineInfo<String[]> compile(String code) throws ICVerificationException {
        char[] chars = code.toCharArray();
        ArrayList<String> funcs = new ArrayList<String>();
        ArrayList<LineInfo[]> infos = new ArrayList<LineInfo[]>();
        String current = "";
        ArrayList<LineInfo> currentLineInfo = new ArrayList<LineInfo>();
        int line = 1;
        int col  = 1;
        int bracketCount = 0;
        for(int i=0;i<chars.length;i++) {
            char c = chars[i];
            switch(c) {
                case ':':
                    if(bracketCount!=0)
                        throw new ICVerificationException("Missing closing braces in function ending on "+line);
                    funcs.add(current);
                    infos.add(currentLineInfo.toArray(new LineInfo[0]));

                    funcs.clear();
                    infos.clear();
                    break;
                case '\n':
                    line++;
                    col=0;
                    break;
                case '[':
                    bracketCount++;
                    break;
                case ']':
                    if(bracketCount==0)
                        throw new ICVerificationException("Too many closing braces on line "+line+" at column "+col);
                    bracketCount--;
                    break;
                case '+':case '-':
                case 'A':case 'B':case 'C':
                case 'd':case 'p':case 'x':
                case '!':case '|':case '=':case '&':case '^':
                case 's':case 'r':
                    break;

                case '<':case '>':case 'e':case 'S':case 'L':
                    current += chars[i];
                    currentLineInfo.add(new LineInfo(line, col));
                    col++;
                    switch(chars[++i]) {
                        case 'p':case 'r':case 'l':
                        case 'P':case 'R':case 'L':
                            break;
                        default:
                            throw new ICVerificationException(
                                    "Unknown modifier "+chars[i]+" to opcode "+c+" on line "+line+" at column "+col);
                    }
                    if(c == 'S' || c == 'L') {
                        current += chars[i];
                        currentLineInfo.add(new LineInfo(line, col));
                        col++;
                        char p = chars[++i];
                        if(!(p>='0' || p<='9') &&
                           !(p>='a' || p<='v'))
                            throw new ICVerificationException(
                                    "Bad table index "+chars[i]+" for opcode "+c+" on line "+line+" at column "+col);
                    }
                    break;

                case 'v':
                    {
                        current += chars[i];
                        currentLineInfo.add(new LineInfo(line, col));
                        col++;
                        char n = chars[++i];
                        if(!(n>='0' || n<='9'))
                            throw new ICVerificationException(
                                    "Bad peek depth "+chars[i]+" on line "+line+" at column "+col);
                    }
                    break;

                case '.':
                    for(int j=0;j<4;j++) {
                        current += chars[i];
                        currentLineInfo.add(new LineInfo(line, col));
                        col++;
                        switch(chars[++i]) {
                            case '+':case '-':
                            case '1':case '0':
                                break;

                            default:
                                throw new ICVerificationException(
                                        "Bad logic table value "+chars[i]+" on line "+line+" at column "+col);
                        }
                    }
                    break;

                case 'c':case 't':
                    for(int j=0;j<4;j++) {
                        current += chars[i];
                        currentLineInfo.add(new LineInfo(line, col));
                        col++;
                        char n = chars[++i];
                        if(!(n>='0' || n<='9'))
                            throw new ICVerificationException(
                                    "Invalid character "+chars[i]+" in function number "+
                                    "on line "+line+" at column "+col);
                    }
                    {
                        current += chars[i];
                        currentLineInfo.add(new LineInfo(line, col));
                        col++;
                        char n = chars[++i];
                        if(!(n>='0' || n<='9'))
                            throw new ICVerificationException(
                                    "Invalid character "+chars[i]+" in argument count "+
                                    "on line "+line+" at column "+col);
                    }
                    break;

                default:
                    throw new ICVerificationException("Unknown opcode "+c+" on line "+line+" at column "+col);
            }
            if(c!=':' && c!='\n') {
                current += chars[i];
                currentLineInfo.add(new LineInfo(line, col));
            }
            col++;
        }

        if(bracketCount!=0)
            throw new ICVerificationException("Missing closing braces in function ending on "+line);
        funcs.add(current);
        infos.add(currentLineInfo.toArray(new LineInfo[0]));

        return new WithLineInfo<String[]>(infos.toArray(new LineInfo[0][0]), funcs.toArray(new String[0]));
    }

    @Override
    public void writeState(boolean[] state, DataOutputStream out) throws IOException {
        out.writeInt(PERLSTONE_STORE_VERSION);
        out.writeInt(state.length);
        for(int i=0;i<state.length;i++) out.writeBoolean(state[i]);
    }

    @Override
    public void loadState(boolean[] state, DataInputStream in) throws IOException {
        if(in.readInt()!=PERLSTONE_STORE_VERSION) throw new IOException("incompatible save version");
        if(state.length!=in.readInt()) throw new IOException("size mismatch!");
        for(int i=0;i<state.length;i++) state[i] = in.readBoolean();
    }

    @Override
    public void execute(ChipState chip, boolean[] state, WithLineInfo<String[]> code) throws PlcException {
        boolean[] tt = new boolean[32];
        boolean a = chip.getInputCount()>0 && chip.getInput(0);
        boolean b = chip.getInputCount()>1 && chip.getInput(1);
        boolean c = chip.getInputCount()>2 && chip.getInput(2);

        for(int i=0;i<chip.getOutputCount();i++) {
            if(i<code.code.length) {
                Boolean r = executeFunction(i, state, tt, code,
                                            a, b, c, new boolean[0],
                                            new int[1]);
                if(r==null) chip.setOutput(i, false);
                else        chip.setOutput(i, r);
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
        return ((a%b)+b)%b;
    }
    private int decodeAddress(char c, int shift) {
        if(c >= '0' && c <= '9') return mod(c-'0'+shift, 32);
        else                     return mod(c-'a'+10+shift, 32);
    }
    private int parseNumber(char c) {
        return c-'0';
    }
    private boolean parseTableChar(char c) throws PlcException {
        switch(c) {
            case '+': return true;
            case '-': return false;
            case '1': return true;
            case '0': return false;

            // Shouldn't happen because of validation.
            default: throw new PlcException("invalid table", "Invalid character in logic table.");
        }
    }

    private String errmsg(String err,
                          int fno, char opcode, LineInfo li,
                          boolean[] pt, boolean[] tt, boolean[] lt,
                          int pshift, int tshift, int lshift,
                          Stack<Boolean> stack) {
        String errm = "";
        errm += ChatColor.RED+"Detailed Error Message: "+ChatColor.RESET+err+"\n";
        errm += ChatColor.RED+"Error Location: "+ChatColor.RESET+"Line "+li.line+", column "+li.col+"\n";
        errm += ChatColor.RED+"Executing Opcode: "+ChatColor.RESET+opcode+"\n";
        errm += ChatColor.RED+"Persistent Variable Table: \n "+ChatColor.RESET+dumpStateText(pt)+"\n";
        errm += ChatColor.RED+" - Shift: "+ChatColor.RESET+pshift+"\n";
        errm += ChatColor.RED+"Temp Variable Table: \n "+ChatColor.RESET+dumpStateText(tt)+"\n";
        errm += ChatColor.RED+" - Shift: "+ChatColor.RESET+tshift+"\n";
        errm += ChatColor.RED+"Local Variable Table: \n "+ChatColor.RESET+dumpStateText(lt)+"\n";
        errm += ChatColor.RED+" - Shift: "+ChatColor.RESET+lshift+"\n";
        errm += ChatColor.RED+"Current Stack: "+ChatColor.RESET+dumpStateText(stack.toArray(new Boolean[0]));
        return errm;
    }

    // Use wrapper type to be able to express "no return" as null
    private Boolean executeFunction(int fno, boolean[] pt, boolean[] tt, WithLineInfo<String[]> funs,
                                    boolean a, boolean b, boolean c, boolean[] args,
                                    int[] opc) throws PlcException {
        // Wrap in an while(true) to allow the t opcode to be a tail call.
        outer: while(true) {
            String fn = funs.code[fno];
            LineInfo[] lis = funs.lineInfo[fno];
            char[] code = fn.toCharArray();
            int[] jt = new int[code.length]; // Jump table so that [ and ] aren't that messy
            /* scope */ {
                Stack<Integer> bracketStack = new Stack<Integer>();
                for(int i=0;i<code.length;i++) {
                    char ch = code[i];
                    if(ch=='[') {
                        bracketStack.push(i);
                    } else if (ch==']') {
                        int j = bracketStack.pop();
                        jt[i] = j;
                        jt[j] = i;
                    }
                }
            }

            int ip = 0;
            Stack<Boolean> executionStack = new Stack<Boolean>();
            for(int i=0;i<args.length;i++)
                executionStack.push(args[i]);
            boolean[] lt = new boolean[32];
            int pshift = 0;
            int tshift = 0;
            int lshift = 0;
            char op = '?';
            LineInfo li = new LineInfo(0, 0);
            try {
                try {
                    while(ip<code.length) {
                        opc[0]++;
                        if(opc[0]==MAX_INSTRUCTION_COUNT) throw new PlcException("ran too long",
                                "Aborted due to running too many instructions");
                        op = code[ip];
                        li = lis[ip];
                        switch(op) {
                            case '+': executionStack.push(true); break;
                            case '-': executionStack.push(false); break;

                            case 'A': executionStack.push(a); break;
                            case 'B': executionStack.push(b); break;
                            case 'C': executionStack.push(c); break;

                            case '<':case '>':case 'e': {
                                int mul = 1;
                                int add = 0;
                                switch(code[ip]) {
                                    case '<': add = -1; break;
                                    case '>': add = +1; break;
                                    case 'e': mul =  0; break;
                                }

                                switch(code[++ip]) {
                                    case 'p':case 'P': pshift = mul * pshift + add; break;
                                    case 't':case 'T': tshift = mul * tshift + add; break;
                                    case 'l':case 'L': lshift = mul * lshift + add; break;
                                }
                            } break;

                            case 'S':case 'L': {
                                boolean[] table = null;
                                int shift = 0;

                                switch(code[++ip]) {
                                    case 'p': shift = pshift; case 'P': table = pt; break;
                                    case 't': shift = tshift; case 'T': table = tt; break;
                                    case 'l': shift = lshift; case 'L': table = lt; break;
                                }

                                int add = decodeAddress(code[++ip], shift);
                                if(op == 'S') {
                                    table[add] = executionStack.pop();
                                } else {
                                    executionStack.push(table[add]);
                                }
                            } break;

                            case 'd': executionStack.push(executionStack.peek()); break;
                            case 'p': executionStack.pop(); break;
                            case 'v': try {
                                int level = parseNumber(code[++ip]);
                                executionStack.push(executionStack.get(executionStack.size()-1-level));
                            } catch(ArrayIndexOutOfBoundsException e) {
                                throw new PlcException("bad stack pos",
                                        "Attempted to call peek on too small a stack.");
                            }
                            break;
                            case 'x': {
                                boolean x = executionStack.pop();
                                boolean y = executionStack.pop();
                                executionStack.push(x);
                                executionStack.push(y);
                            } break;

                            case '!': executionStack.push(!executionStack.pop()); break;

                            // Using the short-circuiting versions would cause it to sometimes pop one less
                            // value than it should.
                            case '^': executionStack.push(executionStack.pop()^ executionStack.pop()); break;
                            case '&': executionStack.push(executionStack.pop()& executionStack.pop()); break;
                            case '|': executionStack.push(executionStack.pop()| executionStack.pop()); break;
                            case '=': executionStack.push(executionStack.pop()==executionStack.pop()); break;

                            case '.': {
                                boolean ta = parseTableChar(code[++ip]);
                                boolean tb = parseTableChar(code[++ip]);
                                boolean tc = parseTableChar(code[++ip]);
                                boolean td = parseTableChar(code[++ip]);

                                boolean e = executionStack.pop();
                                boolean f = executionStack.pop();

                                if(!e&&!f)     executionStack.push(ta);
                                else if(!e&&f) executionStack.push(tb);
                                else if(e&&!f) executionStack.push(tc);
                                else           executionStack.push(td);
                            } break;

                            case 'c':case 't': {
                                int n = parseNumber(code[++ip])*10 + parseNumber(code[++ip]);
                                int nArgs = parseNumber(code[++ip]);
                                boolean[] arg = new boolean[nArgs];

                                if(n<0 || n>=funs.code.length) throw new PlcException("func not found",
                                        "Attempted to call nonexistent function");

                                if(op=='c') {
                                    for(int i=nArgs-1;i>=0;i--)
                                        arg[i] = executionStack.pop();
                                    Boolean v = executeFunction(n, pt, tt, funs, a, b, c, arg, opc);
                                    if(v!=null)
                                        executionStack.push(v);
                                    break;
                                } else {
                                    fno  = n;
                                    args = arg;
                                    continue outer;
                                }
                            }

                            case '[': if(!executionStack.pop()) ip = jt[ip]; break;
                            case ']': if( executionStack.pop()) ip = jt[ip]; break;

                            case 's': return null;
                            case 'r': return executionStack.pop();
                        }
                        ip++;
                    }
                } catch(EmptyStackException e) {
                    throw new PlcException("empty stack", "Popped while stack was empty.");
                } catch(StackOverflowError e) {
                    throw new PlcException("stack overflow", "Java stack overflow.");
                }
            } catch(PlcException e) {
                throw new PlcException(e.getMessage(), errmsg(e.detailedMessage,
                        fno, op, li,
                        pt, tt, lt,
                        pshift, tshift, lshift,
                        executionStack));
            }
            return null;
        }
    }

    private String dumpStateText(boolean[] state) {
        char[] c = new char[state.length];
        for(int i=0;i<state.length;i++)
            c[i] = state[i] ? '1' : '0';
        return new String(c);
    }
    private String dumpStateText(Boolean[] state) {
        char[] c = new char[state.length];
        for(int i=0;i<state.length;i++)
            c[i] = state[i] ? '1' : '0';
        return new String(c);
    }

    @Override
    public String dumpState(boolean[] state) {
        return ChatColor.RED+"Persistent Variable Table: \n "+ChatColor.RESET+dumpStateText(state);
    }
}
