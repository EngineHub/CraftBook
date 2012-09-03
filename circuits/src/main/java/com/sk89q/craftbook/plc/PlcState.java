package com.sk89q.craftbook.plc;

import java.io.*;

public interface PlcState {
    void dumpTo(DataOutputStream out) throws IOException;
    void loadFrom(DataInputStream in) throws IOException;
}
