package edu.utexas.libra.translator.block;

import java.util.List;

public class LoopCodeBlock implements CodeBlock {
    private Condition loopCond;
    private List<CodeBlock> bodyBlocks;

    public LoopCodeBlock(Condition loopCond, List<CodeBlock> bodyBlocks) {
        this.loopCond = loopCond;
        this.bodyBlocks = bodyBlocks;
    }

    public List<CodeBlock> getBodyBlocks() {
        return bodyBlocks;
    }

    public Condition getLoopCondition() {
        return loopCond;
    }

    @Override
    public <T> T accept(CodeBlockVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
