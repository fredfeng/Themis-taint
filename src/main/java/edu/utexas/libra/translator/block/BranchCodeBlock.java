package edu.utexas.libra.translator.block;

import soot.Unit;
import soot.Value;

import java.util.List;

public class BranchCodeBlock implements CodeBlock {

    private Condition cond;
    private List<CodeBlock> trueBlock, falseBlock;

    public BranchCodeBlock(Condition cond, List<CodeBlock> trueBlock, List<CodeBlock> falseBlock) {
        this.cond = cond;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }

    public Condition getBranchCondition() { return cond; }
    public List<CodeBlock> getTrueBlocks() {
        return trueBlock;
    }
    public List<CodeBlock> getFalseBlocks() {
        return falseBlock;
    }

    @Override
    public <T> T accept(CodeBlockVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
