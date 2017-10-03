package edu.utexas.libra.translator.block;

import soot.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeqCodeBlock implements CodeBlock {
    private List<Unit> elems;
    private Terminator terminator = null;

    public SeqCodeBlock() {
        this.elems = new ArrayList();
    }
    public SeqCodeBlock(List elems) {
        this.elems = elems;
    }

    public void add(Unit unit) {
        elems.add(unit);
    }
    public void dropLast() {
        if (!elems.isEmpty())
            elems.remove(elems.size() - 1);
    }
    public void setTerminator(Terminator term) {
        this.terminator = term;
    }

    public List<Unit> getElements() {
        return Collections.unmodifiableList(elems);
    }

    public Terminator getTerminator() {
        return terminator;
    }

    @Override
    public <T> T accept(CodeBlockVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
