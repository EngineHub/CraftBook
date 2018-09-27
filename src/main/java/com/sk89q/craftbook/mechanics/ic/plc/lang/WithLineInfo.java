package com.sk89q.craftbook.mechanics.ic.plc.lang;

class WithLineInfo<CodeT> {

    public final LineInfo[] lineInfo;
    public final CodeT code;

    WithLineInfo(LineInfo[] lineInfo, CodeT code) {

        this.lineInfo = lineInfo;
        this.code = code;
    }
}
