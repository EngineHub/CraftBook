/*    
Craftbook

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

package com.sk89q.craftbook.mech.ic.plc.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.ChipState;
import com.sk89q.craftbook.mech.ic.plc.PlcLang;
import com.sk89q.craftbook.util.BlockVector;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Language for CraftBook's PLC system. Hardcoded for VIVO or 3I3O layouts.
 * 
 * @author olegbl
 */
public final class Perlstone32_1 implements PlcLang {
    /**
     * Debugging switch (verbose mode).
     */
    private final boolean debug;
    
    public Perlstone32_1() {
        debug = false;
    }
    public Perlstone32_1(boolean debug) {
        this.debug = debug;
    }

    /**
     * Permanent shared storage.
     */
    private Map<String, int[]> publicPersistentStorage = new HashMap<String, int[]>();
    private Map<String, HashMap<BlockVector, int[]>> privatePersistentStorage = new HashMap<String, HashMap<BlockVector, int[]>>();

    public final String getName() {
        return "PS32 1.1.3";
    }

    private static int getPersistentStorageType(ChipState chip) {
        String type = chip.getText().getLine4().replace(" ", "");
        if (type.equalsIgnoreCase("private")) return 1; // Private
        else if (type.equals("")) return 2; // None
        return 0; // Public
    }

    public final boolean[] tick(ChipState chip, String program) throws PerlstoneException {
        // checkSyntax(program); // Already done when placing sign.
        // (validateEnvironment)

        String[] staticFunctions = getStaticFunctions(program);

        int persistentStorageType = getPersistentStorageType(chip);

        String worldName = chip.getWorld().getUniqueIdString();

        if (!privatePersistentStorage.containsKey(worldName)) 
            privatePersistentStorage.put(worldName, new HashMap<BlockVector, int[]>());
        Map<BlockVector, int[]> privatePersistentStorage = 
            this.privatePersistentStorage.get(worldName);
        
        if (persistentStorageType == 0) { // Public
            if (!publicPersistentStorage.containsKey(chip.getText().getLine4())) {
                publicPersistentStorage.put(chip.getText().getLine4(), new int[32]);
            }
        } else if (persistentStorageType == 1) { // Private
            if (!privatePersistentStorage.containsKey(chip.getBlockPosition())) {
                privatePersistentStorage.put(chip.getBlockPosition(), new int[32]);
            }
        }

        int[] gvt = new int[0]; // None
        if (persistentStorageType == 0) // Public
        gvt = publicPersistentStorage.get(chip.getText().getLine4());
        else if (persistentStorageType == 1) // Private
            gvt = privatePersistentStorage.get(chip.getBlockPosition());
        int[] tvt = new int[32];
        boolean[] output = new boolean[3];

        for (int i = 0; i < output.length && i < staticFunctions.length; i++) {
            output[i] = (callFunction(staticFunctions[i], new int[0], chip, gvt, tvt, staticFunctions, new int[] { 0 }) != 0);
        }

        if (persistentStorageType == 0) { // Public
            publicPersistentStorage.put(chip.getText().getLine4(), gvt);
        } else if (persistentStorageType == 1) { // Private
            privatePersistentStorage.put(chip.getBlockPosition(), gvt);
        }

        return output;
    }

    private final int callFunction(String function, int[] args, ChipState chip, int[] pvt, int[] tvt, String[] staticf, int[] numOpcodes) throws PerlstoneException {
        boolean previousOpcode = false;

        try {
            Stack<Integer> stack = new Stack<Integer>();
            int[] lvt = new int[32];
            int[] jumpTable = buildJumpTable(function);

            if (debug) {
                System.out.println(" - Jump Table: " + Arrays.toString(jumpTable));
                logger.log(Level.INFO, "[CraftBook] Perlstone32 - Jump Table: " + Arrays.toString(jumpTable));
            }

            for (int b : args)
                stack.push(b);

            String[] tokens = function.split("[ ;]");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];

                String errorLocation = "§2" + ((i >= 2) ? (tokens[i - 2] + " ") : "") + ((i >= 1) ? (tokens[i - 1] + " ") : "") + "§4" + tokens[i] + "§e" + " " + ((i < tokens.length - 1) ? (tokens[i + 1] + " ") : "") + ((i < tokens.length - 2) ? (tokens[i + 2]) : "") + "§c";

                numOpcodes[0]++;
                if (numOpcodes[0] == 25000) throw new PerlstoneException(errorLocation + ": Too many opcodes. Check for infinite loop/recursion.");

                if (debug) {
                    if (previousOpcode) {
                        System.out.println(" - PVT: " + Arrays.toString(pvt));
                        System.out.println(" - TVT: " + Arrays.toString(tvt));
                        System.out.println(" - LVT: " + Arrays.toString(lvt));
                        System.out.println(" - Stack: " + stack);
                        logger.log(Level.INFO, "[CraftBook] Perlstone32 - PVT: " + Arrays.toString(pvt));
                        logger.log(Level.INFO, "[CraftBook] Perlstone32 - TVT: " + Arrays.toString(tvt));
                        logger.log(Level.INFO, "[CraftBook] Perlstone32 - LVT: " + Arrays.toString(lvt));
                        logger.log(Level.INFO, "[CraftBook] Perlstone32 - Stack: " + stack);
                    }
                    System.out.println("Opcode " + numOpcodes[0] + ": token # " + i + ": " + token + " in " + new String(function));
                    previousOpcode = true;
                }

                boolean tokenIsInt = false;
                int tokenInt = 0;
                try {
                    tokenInt = Integer.parseInt(token);
                    tokenIsInt = true;
                } catch (NumberFormatException e) {
                }
                ;

                /* Contingencies */
                if (token.equals("")) continue;
                /* Variable Functions */
                else if (token.equals("A")) stack.push(chip.getIn(1).is() ? 1 : 0);
                else if (token.equals("B")) stack.push(chip.getIn(2).is() ? 1 : 0);
                else if (token.equals("C")) stack.push(chip.getIn(3).is() ? 1 : 0);
                else if (token.equals("At")) stack.push(chip.getIn(1).isTriggered() ? 1 : 0);
                else if (token.equals("Bt")) stack.push(chip.getIn(2).isTriggered() ? 1 : 0);
                else if (token.equals("Ct")) stack.push(chip.getIn(3).isTriggered() ? 1 : 0);
                else if (token.startsWith("L")) {
                    char tT = (token.length() > 1) ? token.charAt(1) : (char) (stack.pop() + 0);
                    int[] table = null;
                    if (tT == 0 || tT == 'p') {
                        table = pvt;
                        if (getPersistentStorageType(chip) == 2) throw new PerlstoneException(errorLocation + ": Accessing persistent storage when none was specified.");
                    } else if (tT == 1 || tT == 't') table = tvt;
                    else if (tT == 2 || tT == 'l') table = lvt;
                    else if (tT == 'e') {
                        table = publicPersistentStorage.get(token.substring(2));
                        if (table == null) throw new PerlstoneException(errorLocation + ": External storage specified does not exist.");
                    } else throw new PerlstoneException(errorLocation + ": Illegal table specified.");
                    int index = (tT != 3 && tT != 'e' && token.length() > 2) ? Integer.parseInt(token.substring(2)) : stack.pop();
                    stack.push(table[index]);
                } else if (token.startsWith("S")) {
                    char tT = (token.length() > 1) ? token.charAt(1) : (char) (stack.pop() + 0);
                    int[] table = null;
                    if (tT == 0 || tT == 'p') {
                        table = pvt;
                        if (getPersistentStorageType(chip) == 2) throw new PerlstoneException(errorLocation + ": Accessing persistent storage when none was specified.");
                    } else if (tT == 1 || tT == 't') table = tvt;
                    else if (tT == 2 || tT == 'l') table = lvt;
                    else if (tT == 'e') {
                        table = publicPersistentStorage.get(token.substring(2));
                        if (table == null) throw new PerlstoneException(errorLocation + ": External storage specified does not exist.");
                    } else throw new PerlstoneException(errorLocation + ": Illegal table specified.");
                    int index = (tT != 3 && tT != 'e' && token.length() > 2) ? Integer.parseInt(token.substring(2)) : stack.pop();
                    table[index] = stack.pop();
                    if (tT == 3 || tT == 'e') publicPersistentStorage.put(token.substring(2), table);
                }
                /* Stack Functions */
                else if (tokenIsInt) stack.push(tokenInt);
                else if (token.startsWith("d")) {
                    int num = token.length() > 1 ? Integer.parseInt(token.substring(1)) : stack.pop();
                    for (int k = 0; k < num; k++)
                        stack.push(stack.peek());
                } else if (token.startsWith("p")) {
                    int num = token.length() > 1 ? Integer.parseInt(token.substring(1)) : stack.pop();
                    for (int k = 0; k < num; k++)
                        stack.pop();
                } else if (token.startsWith("v")) {
                    int num = token.length() > 1 ? Integer.parseInt(token.substring(1)) : stack.pop();
                    if (stack.size() < num + 1) throw new PerlstoneException(errorLocation + ": Stack too small.");
                    stack.push(stack.get(stack.size() - 1 - num));
                }
                /* Mutator Functions */
                else if (token.equals("+")) stack.push(stack.pop() + stack.pop());
                else if (token.equals("-")) {
                    int top = stack.pop();
                    stack.push(stack.pop() - top);
                } else if (token.equals("*")) stack.push(stack.pop() * stack.pop());
                else if (token.equals("/")) {
                    int top = stack.pop();
                    stack.push(stack.pop() / top);
                } else if (token.equals("%")) {
                    int top = stack.pop();
                    stack.push(stack.pop() % top);
                } else if (token.equals("^")) {
                    int top = stack.pop();
                    stack.push(stack.pop() ^ top);
                } else if (token.equals(">>")) {
                    int top = stack.pop();
                    stack.push(stack.pop() >> top);
                } else if (token.equals("<<")) {
                    int top = stack.pop();
                    stack.push(stack.pop() << top);
                } else if (token.equals("++")) stack.push(stack.pop() + 1);
                else if (token.equals("--")) stack.push(stack.pop() - 1);
                /* Comparator Functions */
                else if (token.equals("==")) {
                    int top = stack.pop();
                    stack.push((stack.pop() == top) ? 1 : 0);
                } else if (token.equals("!=")) {
                    int top = stack.pop();
                    stack.push((stack.pop() != top) ? 1 : 0);
                } else if (token.equals(">")) {
                    int top = stack.pop();
                    stack.push((stack.pop() > top) ? 1 : 0);
                } else if (token.equals("<")) {
                    int top = stack.pop();
                    stack.push((stack.pop() < top) ? 1 : 0);
                } else if (token.equals(">=")) {
                    int top = stack.pop();
                    stack.push((stack.pop() >= top) ? 1 : 0);
                } else if (token.equals("<=")) {
                    int top = stack.pop();
                    stack.push((stack.pop() <= top) ? 1 : 0);
                }
                /* Logical Functions */
                else if (token.equals("!")) stack.push(stack.pop() == 0 ? 1 : 0);
                else if (token.equals("&")) {
                    int top = stack.pop();
                    stack.push((stack.pop() != 0 && top != 0) ? 1 : 0);
                } else if (token.equals("|")) {
                    int top = stack.pop();
                    stack.push((stack.pop() != 0 || top != 0) ? 1 : 0);
                } else if (token.equals("x")) {
                    int top = stack.pop();
                    int ptop = stack.pop();
                    stack.push(((ptop != 0 && top == 0) || (ptop == 0 && top != 0)) ? 1 : 0);
                }
                /* Flow Control Functions */
                else if (token.equals("R")) return 0;
                else if (token.equals("r")) return stack.pop();
                else if (token.startsWith("f")) {
                    int functionId = (token.length() > 1) ? Integer.parseInt(token.substring(1, 3)) : stack.pop();
                    int numArgs = (token.length() > 4) ? Integer.parseInt(token.substring(4)) : stack.pop();
                    int[] fArgs = new int[numArgs];
                    for (int j = numArgs - 1; j >= 0; j--)
                        fArgs[j] = stack.pop();
                    stack.push(callFunction(staticf[functionId], fArgs, chip, pvt, tvt, staticf, numOpcodes));
                } else if (token.equals("[")) {
                    if (stack.pop() == 0) i = jumpTable[i];
                } else if (token.equals("]")) {
                    if (stack.pop() != 0) i = jumpTable[i];
                }
                /* Unknown */
                else {
                    if (debug) {
                        System.out.println(i + " " + token + " " + new String(function));
                        logger.log(Level.INFO, "[CraftBook] Perlstone32 " + i + " " + token + " " + new String(function));
                    }
                    throw new PerlstoneException(errorLocation + ": Unknown opcode.");
                }
            }
            return 0;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new PerlstoneException("Premature end.", e);
        } catch (EmptyStackException e) {
            throw new PerlstoneException("Read empty stack.", e);
        }
    }

    public final String validateEnvironment(WorldInterface w, Vector v, SignText t, String code) {
        privatePersistentStorage.remove(v.toBlockVector());
        try {
            checkSyntax(code);
        } catch (PerlstoneException e) {
            return e.getMessage();
        }
        return null;
    }

    public final void checkSyntax(String program) throws PerlstoneException {
        String[] staticFunctions = getStaticFunctions(program);
        for (String f : staticFunctions) {
            checkFunctionSyntax(f);
            buildJumpTable(f);
        }
    }

    private static final void checkFunctionSyntax(String function) throws PerlstoneException {
        try {
            String[] tokens = function.split("[ ;]");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];

                boolean tokenIsInt = false;
                try {
                    Integer.parseInt(token);
                    tokenIsInt = true;
                } catch (NumberFormatException e) {
                }
                ;

                String errorLocation = "§2" + ((i >= 2) ? (tokens[i - 2] + " ") : "") + ((i >= 1) ? (tokens[i - 1] + " ") : "") + "§4" + tokens[i] + "§e" + " " + ((i < tokens.length - 1) ? (tokens[i + 1] + " ") : "") + ((i < tokens.length - 2) ? (tokens[i + 2]) : "") + "§c";

                /* Contingencies */
                if (token.equals(""))
                ;
                /* Variable Functions */
                else if (token.equals("A"))
                ;
                else if (token.equals("B"))
                ;
                else if (token.equals("C"))
                ;
                else if (token.equals("At"))
                ;
                else if (token.equals("Bt"))
                ;
                else if (token.equals("Ct"))
                ;
                else if (token.startsWith("L")) {
                    if (token.charAt(1) == 'e' && token.length() == 2) throw new PerlstoneException(errorLocation + ": No external storage specified.");
                } else if (token.startsWith("S")) {
                    if (token.charAt(1) == 'e' && token.length() == 2) throw new PerlstoneException(errorLocation + ": No external storage specified.");
                }
                /* Stack Functions */
                else if (tokenIsInt)
                ;
                else if (token.startsWith("d")) {
                    try {
                        if (token.length() > 1) Integer.parseInt(token.substring(1));
                    } catch (NumberFormatException e) {
                        throw new PerlstoneException(errorLocation + ": Operand must be an integer.");
                    }
                } else if (token.startsWith("p")) {
                    try {
                        if (token.length() > 1) Integer.parseInt(token.substring(1));
                    } catch (NumberFormatException e) {
                        throw new PerlstoneException(errorLocation + ": Operand must be an integer.");
                    }
                } else if (token.startsWith("v")) {
                    try {
                        if (token.length() > 1) Integer.parseInt(token.substring(1));
                    } catch (NumberFormatException e) {
                        throw new PerlstoneException(errorLocation + ": Operand must be an integer.");
                    }
                }
                /* Mutator Functions */
                else if (token.equals("+"))
                ;
                else if (token.equals("-"))
                ;
                else if (token.equals("*"))
                ;
                else if (token.equals("/"))
                ;
                else if (token.equals("%"))
                ;
                else if (token.equals("^"))
                ;
                else if (token.equals(">>"))
                ;
                else if (token.equals("<<"))
                ;
                else if (token.equals("++"))
                ;
                else if (token.equals("--"))
                ;
                /* Comparator Functions */
                else if (token.equals("=="))
                ;
                else if (token.equals("!="))
                ;
                else if (token.equals(">"))
                ;
                else if (token.equals("<"))
                ;
                else if (token.equals(">="))
                ;
                else if (token.equals("<="))
                ;
                /* Logical Functions */
                else if (token.equals("!"))
                ;
                else if (token.equals("&"))
                ;
                else if (token.equals("|"))
                ;
                else if (token.equals("x"))
                ;
                /* Flow Control Functions */
                else if (token.equals("R"))
                ;
                else if (token.equals("r"))
                ;
                else if (token.startsWith("f")) {
                    try {
                        if (token.length() > 1) Integer.parseInt(token.substring(1, 3));
                    } catch (NumberFormatException e) {
                        throw new PerlstoneException(errorLocation + ": First operand must be a 2-digit integer.");
                    }
                    try {
                        if (token.length() > 3) Integer.parseInt(token.substring(4));
                    } catch (NumberFormatException e) {
                        throw new PerlstoneException(errorLocation + ": Second operand must be an integer.");
                    }
                } else if (token.equals("["))
                ;
                else if (token.equals("]"))
                ;
                /* Unknown */
                else throw new PerlstoneException(errorLocation + ": Unknown opcode.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new PerlstoneException("Array index out of bounds.", e);
        } catch (EmptyStackException e) {
            throw new PerlstoneException("Read empty stack.", e);
        }
    }

    private static String[] getStaticFunctions(String program) throws PerlstoneException {
        String[] staticFunctionStrings;
        /* scope */{
            staticFunctionStrings = program.replace("\n", "").replaceAll("  *", " ").split(":");
            if (staticFunctionStrings.length > 100) throw new PerlstoneException("Excess functions.");
        }
        return staticFunctionStrings;
    }

    private static int[] buildJumpTable(String function) throws PerlstoneException {
        String[] tokens = function.split("[ ;]");
        int length = tokens.length;
        int[] jumpTable = new int[length];
        Stack<Integer> stack = new Stack<Integer>();
        for (int i = 0; i < length; i++) {
            jumpTable[i] = -1;
            if (tokens[i].equals("[")) {
                stack.push(i);
            }
            if (tokens[i].equals("]")) {
                if (stack.isEmpty()) {
                    String errorLocation = "§2" + ((i >= 2) ? (tokens[i - 2] + " ") : "") + ((i >= 1) ? (tokens[i - 1] + " ") : "") + "§4" + tokens[i] + "§e" + " " + ((i < tokens.length - 1) ? (tokens[i + 1] + " ") : "") + ((i < tokens.length - 2) ? (tokens[i + 2]) : "") + "§c";
                    throw new PerlstoneException(errorLocation + ": Unmatched ] brace.");
                }
                int location = stack.pop();
                jumpTable[location] = i;
                jumpTable[i] = location;
            }
        }
        if (stack.size() != 0) {
            int i = stack.pop();
            String errorLocation = "§2" + ((i >= 2) ? (tokens[i - 2] + " ") : "") + ((i >= 1) ? (tokens[i - 1] + " ") : "") + "§4" + tokens[i] + "§e" + " " + ((i < tokens.length - 1) ? (tokens[i + 1] + " ") : "") + ((i < tokens.length - 2) ? (tokens[i + 2]) : "") + "§c";
            throw new PerlstoneException(errorLocation + ": Unmatched [ brace.");
        }
        return jumpTable;
    }
    
    /*------------------------------------------------------------------------\
    |State code                                                               |
    \------------------------------------------------------------------------*/
    public void writeCommonData(DataOutput out) throws IOException {
        out.write(0);
        out.writeInt(publicPersistentStorage.size());
        for (String key : publicPersistentStorage.keySet()) {
            out.writeUTF(key);
            int[] data = publicPersistentStorage.get(key);
            out.writeInt(data.length);
            for (int i : data)
                if (i == 0) out.writeBoolean(false);
                else {
                    out.writeBoolean(true);
                    out.writeInt(i);
                }
        }
    }
    public void readCommonData(DataInput in) throws IOException {
        Map<String, int[]> publicPersistentStorage = 
            new HashMap<String, int[]>();

        if (in.readByte() != 0) throw new IOException("wrong version");
            
        int l = in.readInt();
        for (int j = 0; j < l; j++) {
            String name = in.readUTF();
            int[] data = new int[in.readInt()];
            for (int k = 0; k < data.length; k++)
                if (in.readBoolean()) data[k] = in.readInt();
            publicPersistentStorage.put(name, data);
        }

        this.publicPersistentStorage = publicPersistentStorage;
    }
    public void resetCommonData() {
        publicPersistentStorage = new HashMap<String, int[]>();
    }

    
    public void writeWorldData(WorldInterface w, DataOutput out) throws IOException {
        String worldName = w.getUniqueIdString();
        
        HashMap<BlockVector, int[]> worldData = 
            privatePersistentStorage.get(worldName);
        
        out.write(0);
        out.writeUTF(worldName);
        for (BlockVector key : worldData.keySet()) {
            out.writeInt(key.getBlockX());
            out.writeInt(key.getBlockY());
            out.writeInt(key.getBlockZ());
            int[] data = worldData.get(key);
            out.writeInt(data.length);
            for (int i : data)
                if (i == 0) out.writeBoolean(false);
                else {
                    out.writeBoolean(true);
                    out.writeInt(i);
                }
        }
    }
    public void readWorldData(WorldInterface w, DataInput in) throws IOException {
        String worldName = w.getUniqueIdString();
        
        HashMap<BlockVector, int[]> worldData = 
            new HashMap<BlockVector, int[]>();

        if (in.readByte() != 0) throw new IOException("wrong version");

        int l = in.readInt();
        for (int j = 0; j < l; j++) {
            BlockVector v = new BlockVector(in.readInt(), in.readInt(), in.readInt());
            int[] data = new int[in.readInt()];
            for (int k = 0; k < data.length; k++)
                if (in.readBoolean()) data[k] = in.readInt();
            worldData.put(v, data);
        }
        privatePersistentStorage.put(worldName, worldData);
    }
    public void resetWorldData(WorldInterface w) {
        privatePersistentStorage.remove(w.getUniqueIdString());
    }
}
