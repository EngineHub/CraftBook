package com.sk89q.craftbook.plc.lang;

class WithLineInfo<CodeT> {

    public final LineInfo[] lineInfo;
    public final CodeT code;

    WithLineInfo (LineInfo[] lineInfo, CodeT code) {

        this.lineInfo = lineInfo;
        this.code = code;
    }
}
