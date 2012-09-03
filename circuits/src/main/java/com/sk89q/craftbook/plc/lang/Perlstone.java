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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Stack;

public class Perlstone implements PlcLanguage<boolean[], String[]> {
    private static final int MAX_INSTRUCTION_COUNT = 100000;

    @Override
    public String getName() {
        return "Perlstone-1.1";
    }

    @Override
    public boolean[] initState() {
        return new boolean[32];
    }

    @Override
    public String[] compile(String code) throws ICVerificationException {
        return code.replace("\n","").replace(" ","").split(":");
    }

    @Override
    public void writeState(boolean[] state, DataOutputStream out) throws IOException {
        out.writeInt(state.length);
        for(int i=0;i<state.length;i++) out.writeBoolean(state[i]);
    }

    @Override
    public void loadState(boolean[] state, DataInputStream in) throws IOException {
        if(state.length!=in.readInt()) throw new IOException("size mismatch!");
        for(int i=0;i<state.length;i++) state[i] = in.readBoolean();
    }

    @Override
    public void execute(ChipState chip, boolean[] state, String[] code) throws PlcException {
        try {
            boolean[] tt = new boolean[32];
            boolean a = chip.getInputCount()>0 && chip.getInput(0);
            boolean b = chip.getInputCount()>1 && chip.getInput(1);
            boolean c = chip.getInputCount()>2 && chip.getInput(2);

            for(int i=0;i<chip.getOutputCount();i++) {
                if(i<code.length) {
                    Boolean r = executeFunction(i, state, tt, code,
                                                a, b, c, new boolean[0],
                                                new int[1]);
                    if(r==null) chip.setOutput(i, false);
                    else        chip.setOutput(i, r);
                } else {
                    chip.setOutput(i, false);
                }
            }
        } catch(EmptyStackException e) {
            throw new PlcException("empty stack");
        }
    }

    private int mod(int a, int b) {
        return ((a%b)+b)%b;
    }
    private int decodeAddress(char c, int shift) throws PlcException {
        if(c >= '0' && c <= '9') return mod(c-'0'+shift, 32);
        if(c >= 'a' && c <= 'v') return mod(c-'a'+10+shift, 32);
        throw new PlcException("invalid address");
    }
    private int parseNumber(char c) throws PlcException {
        if(c >= '0' && c <= '9') return c-'0';
        else throw new PlcException("not a number");
    }
    private boolean parseTableChar(char c) throws PlcException {
        switch(c) {
            case '+': return true;
            case '-': return false;
            case '1': return true;
            case '0': return false;
            default: throw new PlcException("invalid table");
        }
    }

    // Use wrapper type to be able to express "no return" as null
    private Boolean executeFunction(int fno, boolean[] pt, boolean[] tt, String[] funs,
                                    boolean a, boolean b, boolean c, boolean[] args,
                                    int[] opc) throws PlcException {
        // Wrap in an while(true) to allow the t opcode to be a tail call.
        outer: while(true) {
            if(fno<0 || fno>=funs.length) throw new PlcException("func not found");
            String fn = funs[fno];
            char[] code = fn.toCharArray();
            int[] jt = new int[code.length]; // Jump table so that [ and ] aren't that messy
            /* scope */ {
                Stack<Integer> bracketStack = new Stack<Integer>();
                for(int i=0;i<code.length;i++) {
                    char ch = code[i];
                    if(ch=='[') {
                        bracketStack.push(i);
                    } else if (ch==']') {
                        if(bracketStack.size()==0) throw new PlcException("mismatched []");

                        int j = bracketStack.pop();
                        jt[i] = j;
                        jt[j] = i;
                    }
                }
                if(bracketStack.size()!=0) throw new PlcException("mismatched []");
            }

            int ip = 0;
            Stack<Boolean> executionStack = new Stack<Boolean>();
            for(int i=0;i<args.length;i++)
                executionStack.push(args[i]);
            boolean[] lt = new boolean[32];
            int pshift = 0;
            int tshift = 0;
            int lshift = 0;
            while(ip<code.length) {
                opc[0]++;
                if(opc[0]==MAX_INSTRUCTION_COUNT) throw new PlcException("ran too long");
                switch(code[ip]) {
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

                            default: throw new PlcException("internal error");
                        }

                        switch(code[++ip]) {
                            case 'p':case 'P': pshift = mul * pshift + add; break;
                            case 't':case 'T': tshift = mul * tshift + add; break;
                            case 'l':case 'L': lshift = mul * lshift + add; break;

                            default: throw new PlcException("bad modifier "+code[ip]);
                        }
                    } break;

                    case 'S':case 'L': {
                        boolean store = code[ip] == 'S';

                        boolean[] table = null;
                        int shift = 0;

                        switch(code[++ip]) {
                            case 'p': shift = pshift; case 'P': table = pt; break;
                            case 't': shift = tshift; case 'T': table = tt; break;
                            case 'l': shift = lshift; case 'L': table = lt; break;

                            default: throw new PlcException("bad modifier "+code[ip]);
                        }

                        int add = decodeAddress(code[++ip], shift);
                        if(store) {
                            table[add] = executionStack.pop();
                        } else {
                            executionStack.push(table[add]);
                        }
                    } break;

                    case 'd': executionStack.push(executionStack.peek()); break;
                    case 'p': executionStack.pop(); break;
                    case 'v': executionStack.push(executionStack.peek()); break;
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
                        char op = code[ip];
                        int n = parseNumber(code[++ip])*10 + parseNumber(code[++ip]);
                        int nArgs = parseNumber(code[++ip]);
                        boolean[] arg = new boolean[nArgs];

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

                    default: throw new PlcException("invalid op "+code[ip]);
                }
                ip++;
            }
            return null;
        }
    }
}
