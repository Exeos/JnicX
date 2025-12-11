package me.exeos.jnicx.jnic.is;

public class LiteralSubCoder extends LiteralSubCoderBase {
    final LiteralDecoder parent;

    private LiteralSubCoder(LiteralDecoder parent) {
        this.parent = parent;
    }

    LiteralSubCoder(LiteralDecoder parent, byte dummy) {
        this(parent);
    }
}
