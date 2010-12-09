/*    
Craftbook PLC component
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lymia.perlstone;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Stack;

import lymia.plc.PlcLang;
import lymia.plc.State;

/**
 * @author Lymia
 */
public final class Perlstone_1_0 implements PlcLang {
    private static boolean DEBUG = false;
    
    public final String getName() {
        return "PS v1.0";
    }

    public final boolean[] tick(State s, String program) throws PerlstoneException {
        checkSyntax(program);

        char[][] staticFunctions = getStaticFunctions(program);

        boolean[] gvt = readPresistantStorage(s.presistantStorage);
        boolean[] tvt = new boolean[32];
        boolean[] output = new boolean[3];

        for (int i = 0; i < output.length && i < staticFunctions.length; i++) {
            Boolean b = callFunction(staticFunctions[i], new boolean[0], s.input, gvt, tvt, staticFunctions, new int[]{0});
            if (b == null) continue;
            output[i] = b;
        }

        storePresistantStorage(s.presistantStorage, gvt);
        
        return output;
    }

    private static final Boolean callFunction(char[] function, boolean[] args, boolean[] it, boolean[] pvt, boolean[] tvt, char[][] staticf, int[] numOpcodes) throws PerlstoneException {
        boolean previousOpcode = false;
        
        try {
            outer: while(true) {
                Stack<Boolean> stack = new Stack<Boolean>();
                boolean[] lvt = new boolean[32];
                int[] jumpTable = buildJumpTable(function);
                
                for(boolean b:args) stack.push(b);
                
                for (int i = 0; i < function.length; i++) {
                    numOpcodes[0]++;
                    if(numOpcodes[0]==25000) throw new PerlstoneException("opcode limit");
                    
                    if(DEBUG) {
                        if(previousOpcode) {
                            System.out.println(" - PVT: "+Arrays.toString(pvt));
                            System.out.println(" - TVT: "+Arrays.toString(tvt));
                            System.out.println(" - LVT: "+Arrays.toString(lvt));
                            System.out.println(" - Stack: "+stack);
                        }
                        System.out.println("Opcode "+numOpcodes[0]+": "+"function["+i+"]="+function[i]+" in "+new String(function));
                        previousOpcode = true;
                    }
                    
                    switch (function[i]) {
                        case '+':
                            stack.push(true);
                            continue;
                        case '-':
                            stack.push(false);
                            continue;
    
                        case 'A':
                            stack.push(it[0]);
                            continue;
                        case 'B':
                            stack.push(it[1]);
                            continue;
                        case 'C':
                            stack.push(it[2]);
                            continue;
    
                        case 'S':
                        case 'L':
                            char action = function[i];
                            char c0 = function[++i];
                            char c1 = function[++i];
    
                            boolean[] table = (c0 == 'p' ? pvt : c0 == 't' ? tvt : c0 == 'l' ? lvt : null);
                            int index = Character.isDigit(c1) ? Integer.parseInt(new String(new char[] { c1 })) : c1 - 'a' + 10;
                            if (action == 'S') table[index] = stack.pop();
                            else stack.push(table[index]);
    
                            continue;
    
                        case 'd':
                            stack.push(stack.peek());
                            continue;
    
                        case 'p':
                            stack.pop();
                            continue;
    
                        case 'v':
                            c0 = function[++i];
                            index = Integer.parseInt(new String(new char[] { c0 }));
                            if (stack.size() < index + 1) throw new PerlstoneException("stack too small");
                            stack.push(stack.get(stack.size() - 1 - index));
                            continue;
    
                        case '!':
                            stack.push(!stack.pop());
                            continue;
    
                        case '^':
                            stack.push(stack.pop() ^ stack.pop());
                            continue;
                        case '&':
                            stack.push(stack.pop() & stack.pop());
                            continue;
                        case '|':
                            stack.push(stack.pop() | stack.pop());
                            continue;
                            
                        case '=':
                            stack.push(stack.pop() == stack.pop());
                            continue;
    
                        case '.':
                            stack.push(new boolean[]{
                                    function[++i] == '1', 
                                    function[++i] == '1', 
                                    function[++i] == '1', 
                                    function[++i] == '1',
                            }[(stack.pop()?1:0)|(stack.pop()?1:0)<<1]);
                            continue;
                            
                        case 'c':
                            int functionId = Integer.parseInt(new String(new char[]{function[++i],function[++i]}));
                            int numargs = Integer.parseInt(new String(new char[]{function[++i]}));
                            boolean[] fArgs = new boolean[numargs];
                            for(int j=numargs-1;j>=0;j--) fArgs[j] = stack.pop();
                            Boolean rv = callFunction(staticf[functionId],fArgs,it,pvt,tvt,staticf,numOpcodes);
                            if(rv!=null) stack.push(rv);
                            continue;
                            
                        case 't':
                            functionId = Integer.parseInt(new String(new char[]{function[++i],function[++i]}));
                            numargs = Integer.parseInt(new String(new char[]{function[++i]}));
                            
                            function = staticf[functionId];
                            
                            args = new boolean[numargs];
                            for(int j=numargs-1;j>=0;j--) args[j] = stack.pop();
                            
                            continue outer;
                            
                        case '[':
                            if(!stack.pop()) i = jumpTable[i];
                            continue;
                        case ']':
                            if(stack.pop()) i = jumpTable[i];
                            continue;
                            
                        case 's':
                            return null;
                        case 'r':
                            return stack.pop();
    
                        default:
                            if(DEBUG) System.out.println(i+" "+function[i]+" "+new String(function));
                            throw new PerlstoneException("unknown opcode");
                    }
                }
                return null;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new PerlstoneException("premature end",e);
        } catch (EmptyStackException e) {
            throw new PerlstoneException("read empty stack",e);
        }
    }

    public final void checkSyntax(String program) throws PerlstoneException {
        char[][] staticFunctions = getStaticFunctions(program);
        for (char[] f : staticFunctions)
            checkFunctionSyntax(f);
    }

    private static final void checkFunctionSyntax(char[] function) throws PerlstoneException {
        int[] jumpTable = buildJumpTable(function);

        try {
            loop: for (int i = 0; i < function.length; i++)
                switch (function[i]) {
                    // No argument opcodes
                    case '+':
                    case '-':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'd':
                    case 'p':
                    case '!':
                    case '^':
                    case '&':
                    case '|':
                    case '=':
                    case 's':
                    case 'r':
                        continue loop;

                        // One digit argument commands.
                    case 'v':
                        if (Character.isDigit(function[++i])) continue loop;
                        throw new PerlstoneException("bad arguments");

                        // Three digit argument commands.
                    case 'c':
                    case 't':
                        if (Character.isDigit(function[++i])&&
                            Character.isDigit(function[++i])&&
                            Character.isDigit(function[++i])) continue loop;
                        throw new PerlstoneException("bad arguments");

                        // table store/load commands. Takes an index and table.
                    case 'S':
                    case 'L':
                        char c;
                        if ((c = function[++i]) != 'p' && c != 't' && c != 'l') throw new PerlstoneException("bad arguments");
                        if (((c = function[++i]) >= '0' && c <= '9') || (c >= 'a' && c <= 'v')) continue loop;
                        throw new PerlstoneException("bad arguments");

                        // Custom logic gate command
                    case '.':
                        if ((c = function[++i]) != '0' && c != '1') throw new PerlstoneException("bad arguments");
                        if ((c = function[++i]) != '0' && c != '1') throw new PerlstoneException("bad arguments");
                        if ((c = function[++i]) != '0' && c != '1') throw new PerlstoneException("bad arguments");
                        if ((c = function[++i]) != '0' && c != '1') throw new PerlstoneException("bad arguments");
                        continue loop;

                        // Loops
                    case '[':
                        checkFunctionSyntax(sub(function, i + 1, i = jumpTable[i]));
                        continue loop;

                    default:
                        throw new PerlstoneException("unknown opcode");
                }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new PerlstoneException("premature end",e);
        } catch (NegativeArraySizeException e) {
            throw new PerlstoneException("mismatched brace",e);
        }
    }

    private static char[][] getStaticFunctions(String program) throws PerlstoneException {
        char[][] staticFunctions; // Java needs a map command...
        /* scope */{
            String[] staticFunctionStrings = program.replace(" ","").replace("\n", "").split(":");
            if (staticFunctionStrings.length > 100) throw new PerlstoneException("excess functions");
            staticFunctions = new char[staticFunctionStrings.length][];
            for (int i = 0; i < staticFunctionStrings.length; i++)
                staticFunctions[i] = staticFunctionStrings[i].toCharArray();
        }
        return staticFunctions;
    }

    private static int[] buildJumpTable(char[] function) throws PerlstoneException {
        int length = function.length;
        int[] jumpTable = new int[length];
        Stack<Integer> stack = new Stack<Integer>();
        for (int i = 0; i < length; i++) {
            jumpTable[i] = -1;
            switch (function[i]) {
                case '[':
                    stack.push(i);
                    break;
                case ']':
                    if (stack.isEmpty()) throw new PerlstoneException("unmatched brace");
                    int location = stack.pop();
                    jumpTable[location] = i;
                    jumpTable[i] = location;
                default:
                    continue;
            }
        }
        if (stack.size() != 0) throw new PerlstoneException("unmatched brace");
        return jumpTable;
    }

    private static char[] sub(char[] t, int s, int e) {
        char[] c = new char[e - s];
        for (int i = 0; i < c.length; i++)
            c[i] = t[s + i];
        return c;
    }

    private static boolean[] readPresistantStorage(byte[] d) {
        boolean[] pvt = new boolean[32];
        for (int i = 0; i < 4; i++)
            for (int b = 0; b < 8; b++)
                pvt[i * 8 + b] = ((d[i] >> (7-b)) & 1) == 1 ? true : false;
        if(DEBUG) System.out.println("Read presistant storage: "+Arrays.toString(d)+" -> "+Arrays.toString(pvt));
        return pvt;
    }

    private static void storePresistantStorage(byte[] d, boolean[] pvt) {
        byte[] data = new byte[4];
        for (int i = 0; i < 4; i++) 
            for (int b = 0; b < 8; b++)
                data[i] |= pvt[i * 8 + b] ? 1 << (7-b) : 0;
        if(DEBUG) System.out.println("Written presistant storage: "+Arrays.toString(pvt)+" -> "+Arrays.toString(d));
        System.arraycopy(data, 0, d, 0, 4);
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("Perlstone v1.0 Test Parser");
        System.out.print("Input program: ");
        
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String program = r.readLine();

        Perlstone_1_0 p = new Perlstone_1_0();
        p.checkSyntax(program);
        
        State s = new State();        
        s.presistantStorage = new byte[20];
        while(true) {
            System.out.print("Input: ");
            String input = r.readLine();
            if(!input.matches("[01][01][01]")) {
                System.out.println("Bad input!");
                continue;
            }
            s.input = new boolean[]{
                    input.charAt(0) == '1',
                    input.charAt(1) == '1',
                    input.charAt(2) == '1',
            };
            
            long time = System.nanoTime();
            boolean[] output = p.tick(s, program);
            System.out.println("Time taken: "+(System.nanoTime()-time)+" ns");
            
            System.out.println("Output: "+(output[0]?"1":"0")+(output[1]?"1":"0")+(output[2]?"1":"0"));
        }
    }
}
